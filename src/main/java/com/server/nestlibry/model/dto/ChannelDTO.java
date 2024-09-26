package com.server.nestlibry.model.dto;


import com.server.nestlibry.model.vo.Channel;
import com.server.nestlibry.model.vo.ChannelTag;
import com.server.nestlibry.model.vo.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data@NoArgsConstructor@AllArgsConstructor@Builder
public class ChannelDTO {

	private Channel channel; // 채널 정보

	private List<ChannelTag> channelTag; // 채널 내의 게시판 정보

	private int favoriteCount; // 즐찾한 인원

	private User host; // 호스트
	
}
