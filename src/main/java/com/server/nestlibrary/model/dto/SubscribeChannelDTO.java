package com.server.nestlibrary.model.dto;

import com.server.nestlibrary.model.vo.Management;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor @AllArgsConstructor @Builder @Data
public class SubscribeChannelDTO {
    private ChannelDTO channelDTO;
    private Management management;
}
