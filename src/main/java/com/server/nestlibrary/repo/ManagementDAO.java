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

   @Query(value = "SELECT channel_code FROM management WHERE user_email = :userEmail AND (management_user_status = 'host' OR management_user_status = 'admin') ", nativeQuery = true)
    List<Integer> myChannel ( @Param("userEmail") String userEmail);

   @Query(value = "SELECT * FROM management WHERE user_email= :userEmail AND channel_code = :channelCode" , nativeQuery = true)
    List<Management> findGrade (@Param("userEmail") String userEmail, @Param("channelCode") int channelCode);

   @Query(value = "DELET FROM management WHERE user_email = :userEmail AND  management_user_status = 'admin'", nativeQuery = true)
   void RemoveAdmin(@Param("userEmail") String userEmail);
}
