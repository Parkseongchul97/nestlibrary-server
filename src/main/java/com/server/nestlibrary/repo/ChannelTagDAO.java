package com.server.nestlibrary.repo;


import com.server.nestlibrary.model.vo.ChannelTag;
import com.server.nestlibrary.model.vo.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChannelTagDAO extends JpaRepository<ChannelTag, Integer> {
    @Query(value = "SELECT * FROM channel_tag WHERE channel_code = :channelCode", nativeQuery = true)
    List<ChannelTag> findByChannelCode(@Param("channelCode") int channelCode);

}
