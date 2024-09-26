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
public class Comment {

	@Id
	@Column(name = "comment_code")
	private int commentCode; // 댓글코드

	@Column(name = "comment_content")
	private String commentContent;  // 댓글 내용

	@Column(name = "comment_created_at")
	private LocalDateTime commentCreatedAt; // 댓글작성시간

	@Column(name = "comment_parents_code")
	private String commentParentsCode; // 부모댓글코드

	@Column(name = "user_email")
	private String userEmail; // 이메일  FOREIGN_KEY

	@Column(name = "post_code")
	private int postCode;  // 게시판 코드, FOREIGN_KEY
}
