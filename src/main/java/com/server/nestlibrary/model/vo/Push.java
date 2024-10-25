package com.server.nestlibrary.model.vo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Entity
@AllArgsConstructor @NoArgsConstructor @Data @Builder
public class Push {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "push_code")
    private int pushCode;

    @Column(name ="userEmail")
    private String userEmail; // 알람 받는사람

    @Column(name ="push_massage")
    private String pushMassage; // 푸쉬알람 메시지

    @Column(name = "post_code")
    private int postCode;

    @Column(name ="push_created_at")
    private LocalDateTime pushCreatedAt; // 알림 생성 시간



}
