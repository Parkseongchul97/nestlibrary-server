package com.server.nestlibry.repo;


import com.server.nestlibry.model.vo.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserDAO extends JpaRepository<User, String> {
    @Query(value = "SELECT * FROM user WHERE user_nickname = :nickname", nativeQuery = true)
    User findByUserNickname(@Param("nickname") String nickname);


}
