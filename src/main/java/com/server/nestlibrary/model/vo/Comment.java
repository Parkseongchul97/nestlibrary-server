package com.server.nestlibrary.model.vo;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor @AllArgsConstructor @Data @Builder
public class Comment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "comment_code")
	private int commentCode; // 댓글코드

	@Column(name = "comment_content")
	private String commentContent;  // 댓글 내용

	@Column(name = "comment_created_at")
	private LocalDateTime commentCreatedAt; // 댓글작성시간

	@Column(name = "comment_parents_code")
	private int commentParentsCode; // 부모댓글코드

	@Column(name = "user_email")
	private String userEmail; // 이메일  FOREIGN_KEY

	@Column(name = "post_code")
	private int postCode;  // 게시판 코드, FOREIGN_KEY
}
