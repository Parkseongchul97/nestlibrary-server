package com.server.nestlibrary.repo;

import com.server.nestlibrary.model.vo.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentDAO extends JpaRepository<Comment, Integer> {



    @Query(value = "SELECT count(*) from comment p join post USING (post_code) WHERE p.user_email =:userEmail AND channel_code= :channelCode AND comment_content IS NOT null" , nativeQuery = true)
    int commentCount(@Param("userEmail")String userEmail, @Param("channelCode")int channelCode);
    @Query(value = "SELECT count(*) from comment p join post USING (post_code) WHERE p.user_email =:userEmail AND channel_code= :channelCode AND comment_content IS NOT null AND p.comment_created_at LIKE :date%" , nativeQuery = true)
    int commentUserCount(@Param("userEmail")String userEmail, @Param("channelCode")int channelCode , @Param("date")String date);
    @Query(value = "SELECT count(*) from comment p join post USING (post_code) WHERE channel_code= :channelCode AND comment_content IS NOT null AND p.comment_created_at LIKE :date%" , nativeQuery = true)
    int commentChannelCount(@Param("channelCode")int channelCode , @Param("date")String date);
}
