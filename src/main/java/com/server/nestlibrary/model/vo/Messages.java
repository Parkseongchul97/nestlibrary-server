package com.server.nestlibrary.model.vo;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor @AllArgsConstructor @Data @Builder
public class Messages {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "messages_code")
	private int messagesCode; // 쪽지 코드

	@Column(name = "messages_from_user")
	private String messagesFromUser; // 발신자 이메일

	@Column(name = "messages_to_user")
	private String messagesToUser; // 수신자 이메일

	@Column(name = "messages_sent_at")
	private LocalDateTime messagesSentAt; // 보낸시간

	@Column(name = "messages_title")
	private String messagesTitle; // 쪽지 제목

	@Column(name = "messages_content")
	private String messagesContent; // 쪽지 내용

	@Column(name = "messages_read")
	private boolean messagesRead; // 읽은 여부
	
	@Column(name = "messages_is_delete")
	private int messagesIsDelete; // 삭제 여부(수신자 삭제, 발신자 삭제)

}
