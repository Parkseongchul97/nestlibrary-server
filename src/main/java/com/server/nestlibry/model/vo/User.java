package com.server.nestlibry.model.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor @NoArgsConstructor @Data @Builder
public class User {

	@Id
	@Column(name = "user_email")
	private String userEmail; // 유저 이메일

	@Column(name = "user_password")
	private String userPassword; // 유저 비밀번호

	@Column(name = "user_nickname")
	private String userNickname; // 닉네임

	@Column(name = "user_img_url")
	private String userImgUrl; // 프로필 사진

	@Column(name = "user_point")
	private int userPoint; // 유저 포인트

	@Column(name = "user_info")
	private String userInfo; // 간단한 자기소개

}
