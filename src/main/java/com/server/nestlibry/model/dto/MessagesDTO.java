package com.server.nestlibry.model.dto;

import com.server.nestlibry.model.vo.User;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class MessagesDTO {

	private int messagesCode; // 쪽지 코드

	private User messagesFromUser; // 발신자

	private User messagesToUser; // 수신자

	private LocalDateTime messagesSentAt; // 보낸시간

	private String messagesTitle; // 쪽지 제목

	private String messagesContent; // 쪽지 내용

	private boolean messagesRead; // 읽은 여부


	
}
