package com.server.nestlibrary.repo;

import com.server.nestlibrary.model.vo.Push;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PushDAO extends JpaRepository<Push, Integer> {
}
