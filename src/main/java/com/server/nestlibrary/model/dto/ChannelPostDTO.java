package com.server.nestlibrary.model.dto;

import com.server.nestlibrary.model.vo.ChannelTag;
import com.server.nestlibrary.model.vo.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChannelPostDTO {
    private int channelCode;   // 채널코드

    private String channelName; // 채널명

    private LocalDateTime channelCreatedAt; // 채널 생성일

    private String channelImg; // 채널 대표 이미지 링크

    private String channelInfo; // 채널소개

    private List<ChannelTagDTO> channelTag = new ArrayList<>(); // 채널 내의 게시판 정보

    private  List<PostDTO> allPost = new ArrayList<>(); // 채널의 모든 작성글

    private int favoriteCount; // 즐찾한 인원

    private User host; // 호스트
}
