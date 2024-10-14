package com.server.nestlibrary.repo;

import com.server.nestlibrary.model.vo.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostLikeDAO extends JpaRepository<PostLike ,Integer> {
}
