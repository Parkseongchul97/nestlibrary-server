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
public class Message {

	@Id
	@Column(name = "messages_code")
	private int messagesCode; // 쪽지 코드

	@Column(name = "messages_from_user")
	private User messagesFromUser; // 발신자

	@Column(name = "messages_to_user")
	private User messagesToUser; // 수신자

	@Column(name = "messages_sent_at")
	private LocalDateTime messagesSentAt; // 보낸시간

	@Column(name = "messages_title")
	private String messagesTitle; // 쪽지 제목

	@Column(name = "messages_content")
	private String messagesContent; // 쪽지 내용

	@Column(name = "messages_read")
	private boolean messagesRead; // 읽은 여부

}
