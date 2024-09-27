package com.server.nestlibry.controller;

import com.server.nestlibry.model.dto.UserRegisterDTO;
import com.server.nestlibry.model.vo.User;
import com.server.nestlibry.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RestController
@RequestMapping("/api/*")
@CrossOrigin(origins = {"*"}, maxAge = 6000)
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/user")
    public ResponseEntity changeUser(UserRegisterDTO dto) throws Exception {
        User vo = new User()
                .builder()
                .userEmail(dto.getUserEmail())
                .userPassword(dto.getUserPassword())
                .userNickname(dto.getUserNickname())
                .userImgUrl(fileUpload(dto.getUserImgUrl(), dto.getUserEmail()))
                .build();
        userService.changeUser(vo);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    public String fileUpload(MultipartFile file, String email) throws IllegalStateException, Exception {
        if (file == null || file.getOriginalFilename() == "") {
            return null;
        }
        UUID uuid = UUID.randomUUID(); // 랜덤 파일명 부여
        String fileName = uuid.toString() + "_" + file.getOriginalFilename();
        File copyFile = new File("\\\\링크주소\\nestlibrary\\user\\" + email + "\\" + fileName);
        file.transferTo(copyFile);
        return fileName;
    }
    
    public void fileDelete(String file, String email) throws IllegalStateException, Exception {
        if (file != null) { // 보내준 DB저장된 파일명이 null이 아니면
            String decodedString = URLDecoder.decode(file, StandardCharsets.UTF_8.name()); // 한글 디코딩 처리
            File f = new File("링크주소");
            f.delete(); // 파일 삭제
        }
    }
}
