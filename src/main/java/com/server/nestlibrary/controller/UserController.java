package com.server.nestlibrary.controller;

import com.server.nestlibrary.config.TokenProvider;
import com.server.nestlibrary.model.dto.LoginUserDTO;
import com.server.nestlibrary.model.dto.UserDTO;
import com.server.nestlibrary.model.vo.User;
import com.server.nestlibrary.service.KakaoService;
import com.server.nestlibrary.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/*")
@CrossOrigin(origins = {"*"}, maxAge = 6000)
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private KakaoService kakaoService;

   
    @PostMapping("/user/login")
    public ResponseEntity login(@RequestBody User vo){
        User user = userService.login(vo.getUserEmail(), vo.getUserPassword());
        if(user != null){ // 회원이 있을시
            String token = tokenProvider.create(user); // 토큰 발행
            log.info("token : " + token);
            user.setUserPassword(null);
            return  ResponseEntity.ok(
                    LoginUserDTO.builder()
                            .token(token)
                            .userEmail(user.getUserEmail())
                            .userNickname(user.getUserNickname())
                            .userImgUrl(user.getUserImgUrl())
                            .userInfo(user.getUserInfo())
                            .userPoint(user.getUserPoint())
                            .build());
        }
        log.info("user : " + user);
        return  ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();



    }
    @PostMapping("/user/register")
    public ResponseEntity registerUser(UserDTO dto) throws Exception {
        // 폴더생성 완료
        Path directoryPath = Paths.get("\\\\\\\\192.168.10.51\\\\nest\\\\user\\" + dto.getUserEmail() + "\\");
        Files.createDirectories(directoryPath);
        // dto vo로 포장
        System.out.println(dto);
        User vo = new User()
                .builder()
                .userEmail(dto.getUserEmail())
                .userPassword(dto.getUserPassword())
                .userNickname(dto.getUserNickname())
                .build();
        System.out.println(vo);
        userService.registerUser(vo);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
    @GetMapping("/user")
    public ResponseEntity findUser(@RequestParam(name = "userEmail") String userEmail){
        log.info(userEmail);
        User user =  userService.findUser(userEmail); // 있으면 중복 닉네임
        log.info("입력값 : " + userEmail + ", 반환값 : " + (user == null));
        if (user == null){
            // 해당 이메일 유저 X
            return  ResponseEntity.ok(null);
        }
        // 해당 이메일 유저 O
        return  ResponseEntity.ok(user);
    }
    @PutMapping("/user/update")
    public  ResponseEntity updateUser(UserDTO dto) throws Exception {
        log.info("수정정보 : " + dto);
        User vo = new User()
                .builder()
                .userEmail( userService.getLoginUser().getUserEmail())
                .userPassword(userService.getLoginUser().getUserPassword())
                .userNickname(dto.getUserNickname())
                .userInfo(dto.getUserInfo())
                .userPoint( userService.getLoginUser().getUserPoint())
                .build();
        // 이미지 변경여부 -1(변경X), 0(변경), 1(이미지 삭제)
        if(dto.getChangeImg()== 0){// 변경하는경우 기존 id의 저장된 파일 삭제후 새로운 파일 업로드하고 저장
            fileDelete(userService.getLoginUser().getUserImgUrl(), userService.getLoginUser().getUserEmail());
            vo.setUserImgUrl(fileUpload(dto.getUserImgUrl(), userService.getLoginUser().getUserEmail()));
            vo.setUserPoint(vo.getUserPoint()-100); // 이미지변경했으니 포인트 차감
        } else if (dto.getChangeImg()== -1) { // 이미지 삭제후 저장
            fileDelete(userService.getLoginUser().getUserImgUrl(), userService.getLoginUser().getUserEmail());
            vo.setUserImgUrl(null);
        }else{ // 변경 X 기존값 다시 추가
            vo.setUserImgUrl(userService.getLoginUser().getUserImgUrl());
        }
        // 닉네임을 변경한 경우
        if(!vo.getUserNickname().equals(userService.getLoginUser().getUserNickname())){
            vo.setUserPoint(vo.getUserPoint()-300); // 닉네임변경했으니  300 포인트 차감
        }
        if(vo.getUserPoint() >= 0){ // 포인트 차감후 포인트가 -로 안내려갈때
            log.info("아마 터지는위치");
            userService.registerUser(vo);
            log.info("변경정보 : " + vo);
            vo.setUserPassword(null);
            log.info("비번날린 회우너정보 : " + vo);
            return ResponseEntity.ok(vo);
        }
        return ResponseEntity.ok(null);

    }

    @GetMapping("/user/nickname")
    public ResponseEntity nicknameCheck(@RequestParam(name = "nickname") String nickname) {
        log.info(nickname);
        User user = userService.findByNickname(nickname); // 있으면 중복 닉네임
        if (user == null) {
            log.info("true 리턴");
            return ResponseEntity.ok(true); // 중복이 아님
        } else if (userService.getLoginUser() != null) {  // 중복이지만 업데이트 상황 (로그인 유저가 있음)
                if (userService.getLoginUser().getUserNickname().equals(nickname)) {// 로그인한 기존 회원의 닉네임과 변경사항이 같으면
                    log.info("true 리턴");
                    return ResponseEntity.ok(true); // 기존 닉네임과 동일함
                }
            }
        log.info("false 리턴");
        return ResponseEntity.ok(false); // 닉네임이 중복임
    }
    public String fileUpload(MultipartFile file, String email) throws IllegalStateException, Exception {
        if (file == null || file.getOriginalFilename() == "") {
            return null;
        }
        UUID uuid = UUID.randomUUID(); // 랜덤 파일명 부여
        String fileName = uuid.toString() + "_" + file.getOriginalFilename();
        File copyFile = new File("\\\\192.168.10.51\\nest\\user\\" + email+ "\\" + fileName);
        file.transferTo(copyFile);
        return fileName;
    }
    
    public void fileDelete(String file, String email) throws IllegalStateException, Exception {
        if (file != null) {
            String decodedString = URLDecoder.decode(file, StandardCharsets.UTF_8.name()); // 한글 디코딩 처리
            File f = new File("\\\\192.168.10.51\\nest\\user\\" + email+ "\\" + decodedString);
            f.delete();
        }
    }

    @PostMapping("/user/kakaoLogin")
    public ResponseEntity kakaoCode(@RequestBody Map<String, String> requestBody, HttpServletResponse response) throws IOException {
        log.info("매핑확인");
        String code = requestBody.get("code");

    String kakaotoken = kakaoService.getAccessToken(code);
    System.out.println("카카오 토큰  : "  +kakaotoken);
    LoginUserDTO dto = kakaoService.getUserInfo(kakaotoken);











       return ResponseEntity.ok(dto);
    }









}
