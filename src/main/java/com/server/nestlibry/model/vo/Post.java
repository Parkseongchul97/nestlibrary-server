package com.server.nestlibry.model.vo;

import java.time.LocalDateTime;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor @AllArgsConstructor @Data @Builder
public class Post {

	@Id
	@Column(name = "post_code")
	private int postCode; // 게시글 코드

	@Column(name = "post_title")
	private String postTitle; // 게시글 제목

	@Column(name = "post_created_at")
	private LocalDateTime postCreatedAt; // 작성 시간

	@Column(name = "post_content")
	private String postContent; // 게시글 내용

	@Column(name = "post_views")
	private int postViews; // 조회수

	@Column(name = "user_email")
	private String userEmail; // 이메일 FOREIGN_KEY

	@Column(name = "channel_code")
	private int channelCode; // 채널 코드 FOREIGN_KEY

	@Column(name = "channel_tag_code")
	private String channelTagCode; // 채널 세부 게시판 태그 FOREIGN_KEY
	
}
