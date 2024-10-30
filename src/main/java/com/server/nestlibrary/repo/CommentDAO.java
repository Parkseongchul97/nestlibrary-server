package com.server.nestlibrary.repo;

import com.server.nestlibrary.model.vo.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentDAO extends JpaRepository<Comment, Integer> {



    @Query(value = "SELECT count(*) from comment p join post USING (post_code) WHERE p.user_email =:userEmail AND channel_code= :channelCode AND comment_content IS NOT null" , nativeQuery = true)
    int commentCount(@Param("userEmail")String userEmail, @Param("channelCode")int channelCode);
}
