package com.server.nestlibrary.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.server.nestlibrary.config.TokenProvider;
import com.server.nestlibrary.controller.UserController;
import com.server.nestlibrary.model.dto.UserDTO;
import com.server.nestlibrary.model.vo.User;
import com.server.nestlibrary.repo.UserDAO;
import jakarta.servlet.http.HttpServletResponse;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

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


    public UserDTO getUserInfo(String accessToken ) throws IOException {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken); // Bearer 토큰 추가

        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange("https://kapi.kakao.com/v2/user/me", HttpMethod.GET, requestEntity, String.class);

        ObjectMapper objectMapper = new ObjectMapper();
        String JwtToken = null;
        UserDTO dto = null;
        try {
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            Long id = Long.valueOf(jsonNode.get("id").asText()); // 사용자 ID
            JsonNode properties = jsonNode.get("properties");
            String email = jsonNode.get("kakao_account").get("email").asText();
            String nickname = properties.get("nickname").asText(); // 사용자 닉네임
            String profileImage = properties.get("profile_image").asText(); // 프로필 이미지 URL


            // 필요한 정보 출력

            System.out.println("User ID: " + id);
            System.out.println("Nickname: " + nickname);
            System.out.println("Profile Image: " + profileImage);
            System.out.println("email : " + email);

            User user = userService.findUser(email);
            dto = new UserDTO();

            if (user == null) {

                Random random = new Random();
                int numeber = 100000 + random.nextInt(900000);
                User user1 = new User();
                user1.setUserNickname("KaKao" + numeber + nickname);
                user1.setUserEmail(email);
                user1.setUserImgUrl(null);
                dao.save(user1);

                JwtToken = tokenProvider.create(user1);


                dto.setToken(JwtToken);
                dto.setUserEmail(email);
                dto.setUserNickname("KaKao" + numeber + nickname);
                dto.setUserImgUrl(null);



            } else {

                JwtToken = tokenProvider.create(user);
                dto.setToken(JwtToken);
                dto.setUserEmail(user.getUserEmail());
                dto.setUserNickname(user.getUserNickname());
                dto.setImg(user.getUserImgUrl());


            }


        } catch (Exception e) {
            e.printStackTrace(); // 오류 발생 시 로그 출력
        }


        return dto;
    }



}
