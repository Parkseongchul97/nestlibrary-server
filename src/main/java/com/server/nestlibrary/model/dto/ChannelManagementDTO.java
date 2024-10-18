package com.server.nestlibrary.model.dto;

import com.server.nestlibrary.model.vo.ChannelTag;
import com.server.nestlibrary.model.vo.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChannelManagementDTO {

    private int channelCode;   // 채널코드

    private String channelName; // 채널명

    private LocalDateTime channelCreatedAt;

    private String channelImg; // 채널 대표 이미지 링크

    private List<ChannelTag> channelTag = new ArrayList<>(); // 채널 내의 게시판 정보

    private List<UserDTO>  adminList = new ArrayList<>(); // 관리자이상 유저 정보

    private List<User> banList = new ArrayList<>(); // 차단유저 리스트  정보

    private String channelInfo; // 채널소개

    private int favoriteCount; // 즐찾한 인원
}
