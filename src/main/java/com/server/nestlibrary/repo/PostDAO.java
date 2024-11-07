package com.server.nestlibrary.repo;

import com.server.nestlibrary.model.vo.Paging;
import com.server.nestlibrary.model.vo.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.method.P;

import java.util.List;

public interface PostDAO extends JpaRepository<Post, Integer> {




    @Query(value = "SELECT count(*) FROM post WHERE user_email= :userEmail AND channel_code = :channelCode", nativeQuery = true)
    int postCount (@Param("channelCode") int channelCode, @Param("userEmail")String userEmail);


    @Query(value = "SELECT * FROM post WHERE user_email= :userEmail " +
            "ORDER BY post_created_at DESC" +
            " LIMIT 10",nativeQuery = true)
    List<Post> emailByPost(@Param("userEmail") String userEmail);


    @Query(value = "SELECT channel_code FROM post WHERE user_email= :userEmail "
          ,nativeQuery = true)
    List<Integer> findChannelCode(@Param("userEmail") String userEmail);

    // 특정 유저가 특정 채널에 글을 쓴 횟수
    @Query(value = "SELECT count(*) FROM post  JOIN  channel USING (channel_code) WHERE user_email = :userEmail AND channel_code =:channelCode" ,nativeQuery = true)
    Integer howManyPost(@Param("userEmail")String userEmail, @Param("channelCode")int channelCode);
}

