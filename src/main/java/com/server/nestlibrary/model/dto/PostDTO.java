package com.server.nestlibrary.model.dto;

import com.server.nestlibrary.model.vo.User;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class PostDTO {

    private int postCode; // 게시글 코드

    private String postTitle; // 게시글 제목

    private LocalDateTime postCreatedAt; // 작성 시간

    private String postContent; // 게시글 내용

    private int postViews; // 조회수

    private UserDTO user; // 이메일 FOREIGN_KEY

    private int channelCode; // 채널 코드 FOREIGN_KEY

    private String channelTagCode; // 채널 세부 게시판 태그 FOREIGN_KEY
    
    private int likeCount; // 좋아요 숫자
    
    private int commentCount; // 댓글 숫자
}
