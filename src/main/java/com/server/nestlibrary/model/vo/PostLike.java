package com.server.nestlibrary.model.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor @AllArgsConstructor @Data @Builder
public class PostLike {

	@Id
	@Column(name = "post_like_code")
	private int postLikeCode; // 게시글 추천 코드

	@Column(name = "post_code")
	private int postCode; // 대상 게시판 코드 FOREIGN_KEY

	@Column(name = "user_email")
	private String userEmail; // 추천을 누른 유저 이메일 FOREIGN_KEY
	
}
