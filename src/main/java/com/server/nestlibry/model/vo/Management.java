package com.server.nestlibry.model.vo;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor @AllArgsConstructor @Data @Builder
public class Management {

	@Id
	@Column(name = "management_code")
	private int managementCode;  //채널 관리 코드

	@Column(name = "management_user_status")
	private String managementUserStatus;  // 유저의 상태 host ,admin ,ban ,sub

	@Column(name = "management_delete_at")
	private LocalDateTime managementDeleteAt; // 삭제 예정일 (벤관련)

	@Column(name = "user_email")
	private String userEmail; // 해당 유저 FOREIGN_KEY


}
