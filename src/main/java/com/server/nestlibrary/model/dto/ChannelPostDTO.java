package com.server.nestlibrary.model.dto;

import com.server.nestlibrary.model.vo.ChannelTag;
import com.server.nestlibrary.model.vo.User;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

public class ChannelPostDTO {
    private int channelCode;   // 채널코드

    private String channelName; // 채널명

    private LocalDateTime channelCreatedAt; // 채널 생성일

    private String channelImg; // 채널 대표 이미지 링크
    
    private String channelInfo; // 채널소개

    private List<ChannelTag> channelTag; // 채널 내의 게시판 정보

    private int favoriteCount; // 즐찾한 인원

    private User host; // 호스트
}
