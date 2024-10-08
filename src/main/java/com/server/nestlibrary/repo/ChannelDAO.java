package com.server.nestlibrary.repo;

import com.server.nestlibrary.model.vo.Channel;

import com.server.nestlibrary.model.vo.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface ChannelDAO extends JpaRepository<Channel, Integer> {
    @Query(value = "SELECT * FROM channel WHERE channel_name = :channelName", nativeQuery = true)
    Channel findByChannelName(@Param("channelName") String channelName);

}
