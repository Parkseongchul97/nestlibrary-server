package com.server.nestlibrary.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UserDTO {

    private String userEmail; // 유저 이메일


    private String userPassword; // 유저 비밀번호


    private String userNickname; // 닉네임


    private MultipartFile userImgUrl; // 프로필 사진


    private int userPoint; // 유저 포인트


    private String userInfo; // 간단한 자기소개

    // 잠시 테스트용으로 추가
    private String token;

    private String img;


}
