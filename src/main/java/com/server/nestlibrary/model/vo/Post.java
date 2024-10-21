package com.server.nestlibrary.model.vo;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor @AllArgsConstructor @Data @Builder
public class Post {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
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

	// 문제 생기면 알려주세요 (2024.10.18)
	@ManyToOne
	@JoinColumn(name="channel_code")
	private Channel channel;

	@ManyToOne
	@JoinColumn(name="channel_tag_code")
	private ChannelTag channelTag;
	
}
