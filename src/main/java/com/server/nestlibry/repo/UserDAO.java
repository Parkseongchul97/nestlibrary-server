package com.server.nestlibry.repo;


import com.server.nestlibry.model.vo.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDAO extends JpaRepository<User, String> {
}
