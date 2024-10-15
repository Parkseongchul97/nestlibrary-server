package com.server.nestlibrary.repo;

import com.server.nestlibrary.model.vo.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentDAO extends JpaRepository<Comment, Integer> {
}
