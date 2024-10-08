package com.server.nestlibrary.controller;

import com.server.nestlibrary.config.TokenProvider;
import com.server.nestlibrary.model.dto.UserDTO;
import com.server.nestlibrary.model.vo.User;
import com.server.nestlibrary.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/user/*")
@CrossOrigin(origins = {"*"}, maxAge = 6000)
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private TokenProvider tokenProvider;

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody User vo){
        User user = userService.login(vo.getUserEmail(), vo.getUserPassword());
        if(user != null){ // 회원이 있을시
            String token = tokenProvider.create(user); // 토큰 발행
            return ResponseEntity.ok(token);
        }
        return  ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();



    }
    @PostMapping("/register")
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
                .userImgUrl(fileUpload(dto.getUserImgUrl(), dto.getUserEmail()))
                .userInfo(dto.getUserInfo())
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

    @GetMapping("/nickname")
    public ResponseEntity<Boolean> nicknameCheck(@RequestParam(name = "nickname") String nickname) {
        log.info(nickname);
        User user = userService.findByNickname(nickname); // 있으면 중복 닉네임
        if (user == null) {

            return ResponseEntity.ok(true); // 중복이 아님
            // 아직 삐리함이거
//        } else if (userService.getLoginUser() != null) {  // 중복이지만 업데이트 상황 (로그인 유저가 있음)
//            if (userService.getLoginUser().getUserNickname().equals(nickname)) {// 로그인한 기존 회원의 닉네임과 변경사항이 같으면
//                return ResponseEntity.ok(true); // 기존 닉네임과 동일함
//            }
        }
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
}
