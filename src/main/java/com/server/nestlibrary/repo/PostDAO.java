package com.server.nestlibrary.repo;

import com.server.nestlibrary.model.vo.Paging;
import com.server.nestlibrary.model.vo.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostDAO extends JpaRepository<Post, Integer> {

    @Query(value = "SELECT p, " +
            "(COUNT(DISTINCT CASE WHEN c.commentParentsCode = 0 THEN c.commentCode END) * 2 + " +
            "COUNT(DISTINCT pl.postLikeCode) * 5 + p.postViews) AS bestScore " +
            "FROM Post p " +
            "LEFT JOIN Comment c ON p.postCode = c.postCode " +
            "LEFT JOIN PostLike pl ON p.postCode = pl.postCode " +
            "WHERE p.channel.channelCode = :channelCode " +
            "GROUP BY p.postCode, p.postTitle, p.postViews " +
            "HAVING bestScore > 50 " +
            "ORDER BY p.postCreatedAt DESC" ,nativeQuery = true)
    List<Post> findBestPosts(@Param("channelCode") int channelCode);
}

