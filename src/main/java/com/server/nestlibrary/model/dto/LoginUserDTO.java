package com.server.nestlibrary.model.dto;

import com.server.nestlibrary.model.vo.User;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginUserDTO {
    private String token;

    private String userEmail; // 유저 이메일


    private String userNickname; // 닉네임

    private String userImgUrl; // 프로필 사진

    private int userPoint; // 유저 포인트

    private String userInfo; // 간단한 자기소개
}
