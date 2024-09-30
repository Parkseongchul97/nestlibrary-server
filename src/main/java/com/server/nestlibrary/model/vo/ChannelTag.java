package com.server.nestlibrary.model.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor @AllArgsConstructor @Data @Builder
public class ChannelTag {

	@Id
	@Column(name = "channel_tag_code")
	private int channelTagCode; // 채널의게시판 코드

	@Column(name = "channel_tag_name")
	private String channelTagName; // 채널의게시판 이름

	@Column(name = "channel_code")
	private int channelCode; // 채널 코드 
	
	
	
}
