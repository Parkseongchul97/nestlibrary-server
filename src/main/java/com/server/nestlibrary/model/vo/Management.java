package com.server.nestlibrary.model.vo;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor @AllArgsConstructor @Data @Builder
public class Management {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "management_code")
	private int managementCode;  //채널 관리 코드

	@Column(name = "management_user_status")
	private String managementUserStatus;  // 유저의 상태 host ,admin ,ban ,sub

	@Column(name = "management_delete_at")
	private LocalDateTime managementDeleteAt; // 삭제 예정일 (벤관련)

	// 문제 생기면 알려주세요 (2024.10.18)
	@ManyToOne
	@JoinColumn(name="channel_code")
	private Channel channel;

	@Column(name = "user_email")
	private String userEmail; // 해당 유저 FOREIGN_KEY


}
