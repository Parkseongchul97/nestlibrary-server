package com.server.nestlibrary.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MostChannelDTO {

    private String channelName;
    private int channelCode;
    private int postCount;
    private int commentCount;
    private List<ChartDTO> chartDTO;

}
