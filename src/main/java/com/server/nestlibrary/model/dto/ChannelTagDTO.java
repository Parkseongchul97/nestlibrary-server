package com.server.nestlibrary.model.dto;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChannelTagDTO {

    private int channelTagCode; // 채널의게시판 코드

    private String channelTagName; // 채널의게시판 이름

    private List<PostDTO> posts = new ArrayList<>();;

}
