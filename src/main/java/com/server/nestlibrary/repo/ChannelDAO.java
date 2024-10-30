package com.server.nestlibrary.repo;

import com.server.nestlibrary.model.vo.Channel;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface ChannelDAO extends JpaRepository<Channel, Integer>, QuerydslPredicateExecutor<Channel> {
    @Query(value = "SELECT * FROM channel WHERE channel_name = :channelName", nativeQuery = true)
    Channel findByChannelName(@Param("channelName") String channelName);

    @Query(value = "SELECT channel_code FROM channel ", nativeQuery = true)
    List<Integer> findAllChannelCode();

}
