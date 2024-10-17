package com.server.nestlibrary.repo;

import com.server.nestlibrary.model.vo.Management;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ManagementDAO extends JpaRepository<Management, Integer> {
   @Query(value = "SELECT count(*) FROM management WHERE channel_code =:channelCode AND  management_user_status = 'sub'", nativeQuery = true)
    int count(@Param("channelCode") int channelcode);

}
