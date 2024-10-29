package com.server.nestlibrary.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class UserRoleDTO {


    private String userEmail;
    private String managementUserStatus;
    private String userNickname;
    private int channelCode;
    private LocalDateTime managementDeleteAt;
    private int banDate;
    private  String userImgUrl;
    private int postCount;
    private int commentCount;


}
