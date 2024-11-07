package com.server.nestlibrary.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.server.nestlibrary.config.TokenProvider;
import com.server.nestlibrary.controller.UserController;
import com.server.nestlibrary.model.dto.LoginUserDTO;
import com.server.nestlibrary.model.dto.UserDTO;
import com.server.nestlibrary.model.vo.User;
import com.server.nestlibrary.repo.UserDAO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
@Slf4j
@Service
public class KakaoService {



    @Autowired
    private UserService userService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private UserDAO dao;

    @Autowired
    private TokenProvider tokenProvider;

    private final String KAKAO_TOKEN_URL = "https://kauth.kakao.com/oauth/token";
    private final String CLIENT_ID = "376abff8d82b23a39e57639e3f0760ad"; // 카카오 REST API 키
    private final String REDIRECT_URI = "http://localhost:3000/kakao"; // 리디렉션 URI

    public String getAccessToken(String code) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // 요청 파라미터 생성
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", CLIENT_ID);
        params.add("redirect_uri", REDIRECT_URI);
        params.add("code", code);


        // POST 요청
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);
        ResponseEntity<String> response = restTemplate.exchange(KAKAO_TOKEN_URL, HttpMethod.POST, requestEntity, String.class);

        // 응답에서 액세스 토큰 추출
        String accessToken = null;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            accessToken = jsonNode.get("access_token").asText(); // access_token 추출
        } catch (Exception e) {
            e.printStackTrace(); // 오류 발생 시 로그 출력
        }
        getUserInfo(accessToken );


        return accessToken; // 액세스 토큰 반환
    }


    public LoginUserDTO getUserInfo(String accessToken ) throws IOException {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken); // Bearer 토큰 추가

        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange("https://kapi.kakao.com/v2/user/me", HttpMethod.GET, requestEntity, String.class);
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            JsonNode properties = jsonNode.get("properties");
            String email = jsonNode.get("kakao_account").get("email").asText();
            String nickname = properties.get("nickname").asText(); // 사용자 닉네임
            // 필요한 정보 출력
            User user = userService.findUser(email);

            if (user == null) {
                boolean ck = true;
                String resultNickname = "";
                while (ck) {
                    Random random = new Random();
                    int number = 100000 + random.nextInt(900000);
                    resultNickname = "Kakao_" + number + "_" + nickname;
                    if (userService.findByNickname(resultNickname) == null) {
                        ck = false;
                    }
                }
                User localUser = User.builder()
                        .userNickname(resultNickname)
                        .userEmail(email)
                        .build();
                dao.save(localUser);
                Path directoryPath = Paths.get("\\\\\\\\192.168.10.51\\\\nest\\\\user\\" + email + "\\");
                Files.createDirectories(directoryPath);
                String JwtToken = tokenProvider.create(localUser);


                LoginUserDTO dto = LoginUserDTO.builder()
                        .token(JwtToken)
                        .userEmail(email)
                        .userNickname(resultNickname)
                        .build();
                log.info("첫 로그인 (가입)" + dto);
                return dto;

            } else {

              if(user.getUserPassword() == null) {
                  String JwtToken = tokenProvider.create(user);
                  // 빌드빌더로 변경만
                  LoginUserDTO dto = LoginUserDTO.builder()
                          .token(JwtToken)
                          .userEmail(user.getUserEmail())
                          .userNickname(user.getUserNickname())
                          .userInfo(user.getUserInfo())
                          .userImgUrl(user.getUserImgUrl())
                          .userPoint(user.getUserPoint()).build();
                  log.info("재 로그인 : " + dto);
                  return dto;
              }else {
                  return null;
              }
            }


        } catch (Exception e) {
            e.printStackTrace(); // 오류 발생 시 로그 출력
            return null;
        }


    }



}
