package com.server.nestlibrary.repo;

import com.server.nestlibrary.model.vo.Messages;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessagesDAO extends JpaRepository<Messages ,Integer> {
}
