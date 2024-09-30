package com.server.nestlibry.service;

import com.server.nestlibry.model.dto.UserDTO;
import com.server.nestlibry.model.vo.User;
import com.server.nestlibry.repo.UserDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.beans.Transient;
import java.util.List;
import java.util.function.Function;

@Service
public class UserService {
    @Autowired
    private UserDAO dao;

    @Autowired
    private PasswordEncoder bcpe;

    @Transactional
    public void registerUser(User vo){
        // 비밀번호 암호화
        vo.setUserPassword(bcpe.encode(vo.getUserPassword()));

        dao.save(vo);
    }
    // 닉네임 중복체크용 닉네임으로 유저 찾기
    public User findByNickname(String nickname){

        return dao.findByUserNickname(nickname);
    }
}
