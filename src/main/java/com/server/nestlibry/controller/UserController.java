package com.server.nestlibry.controller;

import com.server.nestlibry.model.dto.UserRegisterDTO;
import com.server.nestlibry.model.vo.User;
import com.server.nestlibry.service.UserService;
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

@RestController
@RequestMapping("/api/*")
@CrossOrigin(origins = {"*"}, maxAge = 6000)
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/user")
    public ResponseEntity responseUser(UserRegisterDTO dto) throws Exception {
        // 서버만 있으면 회원가입 가능
        Path directoryPath = Paths.get("\\\\\\\\링크주소\\\\nestlibrary\\\\user\\" + dto.getUserEmail() + "\\");
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
        userService.responseUser(vo);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
    @GetMapping("/nickname")
    public ResponseEntity nicknameCheck(String nickname){
        
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    public String fileUpload(MultipartFile file, String email) throws IllegalStateException, Exception {
        if (file == null || file.getOriginalFilename() == "") {
            return null;
        }
        UUID uuid = UUID.randomUUID(); // 랜덤 파일명 부여
        String fileName = uuid.toString() + "_" + file.getOriginalFilename();
        File copyFile = new File("\\\\대충주소\\nestlibrary\\user\\" + email + "\\" + fileName);
        file.transferTo(copyFile);
        return fileName;
    }
    
    public void fileDelete(String file, String email) throws IllegalStateException, Exception {
        if (file != null) {
            String decodedString = URLDecoder.decode(file, StandardCharsets.UTF_8.name()); // 한글 디코딩 처리
            File f = new File("링크주소");
            f.delete();
        }
    }
}
