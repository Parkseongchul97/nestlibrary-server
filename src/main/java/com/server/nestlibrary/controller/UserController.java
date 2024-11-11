package com.server.nestlibrary.controller;

import com.server.nestlibrary.config.TokenProvider;
import com.server.nestlibrary.model.dto.LoginUserDTO;
import com.server.nestlibrary.model.dto.UserDTO;
import com.server.nestlibrary.model.vo.User;
import com.server.nestlibrary.service.KakaoService;
import com.server.nestlibrary.service.PostService;
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
import java.net.Proxy;
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
            LoginUserDTO loginUser =  LoginUserDTO.builder()
                    .token(token)
                    .userEmail(user.getUserEmail())
                    .userNickname(user.getUserNickname())
                    .userImgUrl(user.getUserImgUrl())
                    .userInfo(user.getUserInfo())
                    .userPoint(user.getUserPoint())
                    .build();

            return  ResponseEntity.ok(loginUser
                    );
        }
        return  ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();



    }
    @PostMapping("/user/register")
    public ResponseEntity registerUser(UserDTO dto) throws Exception {
        // 폴더생성 완료
        Path directoryPath = Paths.get("\\\\\\\\192.168.10.51\\\\nest\\\\user\\" + dto.getUserEmail() + "\\");
        Files.createDirectories(directoryPath);
        // dto vo로 포장
        User vo = new User()
                .builder()
                .userEmail(dto.getUserEmail())
                .userPassword(dto.getUserPassword())
                .userNickname(dto.getUserNickname())
                .build();
        userService.registerUser(vo);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
    @GetMapping("/private/user/info")
    public ResponseEntity findUser(@RequestParam(name = "userEmail") String userEmail){

        User user =  userService.findUser(userEmail); // 있으면 중복 이메일
        if (user == null){
            // 해당 이메일 유저 X
            return  ResponseEntity.ok(null);
        }
        // 해당 이메일 유저 O
        return  ResponseEntity.ok(user);
    }

    @PutMapping("/private/user/update")
    public  ResponseEntity updateUser(UserDTO dto) throws Exception {
        User auth = userService.getLoginUser();
        User vo = new User()
                .builder()
                .userEmail( auth.getUserEmail())
                .userPassword(auth.getUserPassword())
                .userNickname(dto.getUserNickname())
                .userInfo(dto.getUserInfo())
                .userPoint( auth.getUserPoint())
                .build();
        // 이미지 변경여부 -1(변경X), 0(변경), 1(이미지 삭제)
        if(dto.getChangeImg()== 0){// 변경하는경우 기존 id의 저장된 파일 삭제후 새로운 파일 업로드하고 저장
            fileDelete(auth.getUserImgUrl(), auth.getUserEmail());
            vo.setUserImgUrl(fileUpload(dto.getUserImg(), auth.getUserEmail()));
            vo.setUserPoint(vo.getUserPoint()-100); // 이미지변경했으니 포인트 차감
        } else if (dto.getChangeImg()==  1) { // 이미지 삭제후 저장
            fileDelete(auth.getUserImgUrl(), auth.getUserEmail());
            vo.setUserImgUrl(null);
        }else{ // 변경 X 기존값 다시 추가
            vo.setUserImgUrl(auth.getUserImgUrl());
        }
        // 닉네임을 변경한 경우
        if(!vo.getUserNickname().equals(auth.getUserNickname())){
            vo.setUserPoint(vo.getUserPoint()-300); // 닉네임변경했으니  300 포인트 차감
        }
        if(vo.getUserPoint() >= 0){ // 포인트 차감후 포인트가 -로 안내려갈때
            userService.registerUser(vo);
            vo.setUserPassword(null);
            LoginUserDTO loginUser =  LoginUserDTO.builder()
                    .userEmail(vo.getUserEmail())
                    .userNickname(vo.getUserNickname())
                    .userImgUrl(vo.getUserImgUrl())
                    .userInfo(vo.getUserInfo())
                    .userPoint(vo.getUserPoint())
                    .build();
            return ResponseEntity.ok(loginUser);
        }
        return ResponseEntity.ok(null);

    }

    @GetMapping("/user/nickname")
    public ResponseEntity nicknameCheck(@RequestParam(name = "nickname") String nickname ,@RequestParam(name = "userEmail" ,required = false)String userEmail) {
        User user = userService.findByNickname(nickname); // 있으면 중복 닉네임
        try {
        if (user == null) {
            return ResponseEntity.ok(true); // 중복이 아님
        } else{
            User auth = userService.findUser(userEmail); // 로그인 유저 정보
        if (auth != null) {  // 중복이지만 업데이트 상황 (로그인 유저가 있음)
                if (auth.getUserNickname().equals(nickname)) {// 로그인한 기존 회원의 닉네임과 변경사항이 같으면

                    return ResponseEntity.ok(true); // 기존 닉네임과 동일함
                }
            }
        return ResponseEntity.ok(false); // 닉네임이 중복임
            }
        } catch (Exception e) {
            return ResponseEntity.ok(false); // 닉네임이 중복임
        }
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
    public ResponseEntity kakaoCode(@RequestBody Map<String, String> requestBody) throws IOException {
        String code = requestBody.get("code");
        String kakaoToken = kakaoService.getAccessToken(code);
       return ResponseEntity.ok(kakaoService.getUserInfo(kakaoToken));
    }

    // 비밀번호 변경
    @PutMapping("/private/user/password")
   public ResponseEntity passwordUpdate (@RequestParam(name = "userEmail") String userEmail, @RequestParam(name = "userPassword") String userPassword){
        User vo = userService.findUser(userEmail);
        vo.setUserPassword(userPassword);
        userService.decodingPassword(vo);
        return ResponseEntity.ok(null);

    }

    // 유저 페이지
    @GetMapping("/user/userInfo/{userEmail}")
    public  ResponseEntity userPage(@PathVariable(name = "userEmail")String userEmail){
        User user = userService.findUser(userEmail);
        user.setUserPassword(null);
        return ResponseEntity.ok(user);


    }
    // 유저탈퇴
    @DeleteMapping("/private/user/remove")
    public ResponseEntity removeUser (){
        User user = userService.getLoginUser();
        folderDelete(user.getUserEmail());
        userService.removeUser();
        return ResponseEntity.ok(null);

    }

    public boolean folderDelete(String userEmail) {
        String path = "\\\\192.168.10.51\\nest\\channel\\" + userEmail;
        File folder = new File(path); //
        try {
            while (folder.exists()) { // 폴더가 존재한다면
                File[] listFiles = folder.listFiles();

                for (File file : listFiles) { // 폴더 내 파일을 반복시켜서 삭제
                    file.delete();
                }
                if (listFiles.length == 0 && folder.isDirectory()) { // 하위 파일이 없는지와 폴더인지 확인 후 폴더 삭제
                    folder.delete();
                }
            }
        }
        catch (Exception e) {
            return false;
        }
        return true;
    }



}
