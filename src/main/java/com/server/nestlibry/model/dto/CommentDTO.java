package com.server.nestlibry.model.dto;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import com.server.nestlibry.model.vo.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class CommentDTO {

		private int commentCode; // 댓글코드

		private int postCode;  // 게시판 코드

		private String commentContent;  // 댓글 내용

		private LocalDateTime commentCreatedAt; // 댓글작성시간

		private User user; // 댓글단 유저
	
		private List<CommentDTO> reCommentDTO; // 대댓글

	
}
