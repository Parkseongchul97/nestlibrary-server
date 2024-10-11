package com.server.nestlibrary.repo;

import com.server.nestlibrary.model.vo.Management;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ManagementDAO extends JpaRepository<Management, Integer> {
    @Query(value = "SELECT * FROM management WHERE management_user_status = 'host' AND channel_code = :channelCode", nativeQuery = true)
    Management findHost(@Param("channelCode") int channelCode);


}
