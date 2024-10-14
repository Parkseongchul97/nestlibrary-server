package com.server.nestlibrary.repo;

import com.server.nestlibrary.model.vo.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostDAO extends JpaRepository<Post, Integer> {
}
