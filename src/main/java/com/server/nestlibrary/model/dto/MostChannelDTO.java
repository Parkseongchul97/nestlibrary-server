package com.server.nestlibrary.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MostChannelDTO {

    private String channelName;
    private int channelCode;
    private int postCount;
    private int commentCount;



}
