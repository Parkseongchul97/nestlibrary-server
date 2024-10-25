package com.server.nestlibrary.model.dto;

import com.server.nestlibrary.model.vo.Channel;
import jakarta.persistence.*;

import java.time.LocalDateTime;

public class ManagementDTO {

    private int managementCode;  //채널 관리 코드

    private String managementUserStatus;  // 유저의 상태 host ,admin ,ban ,sub

    private LocalDateTime managementDeleteAt; // 삭제 예정일 (벤관련)

    private int channelCode;

    private String userEmail; // 해당 유저 FOREIGN_KEY
}
