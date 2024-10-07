package com.server.nestlibrary.service;

import com.server.nestlibrary.model.vo.Channel;
import com.server.nestlibrary.repo.ChannelDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChannelService {
    @Autowired
    private ChannelDAO channelDAO;

    public Channel createChannel(Channel vo){
        return channelDAO.save(vo);
    }
}
