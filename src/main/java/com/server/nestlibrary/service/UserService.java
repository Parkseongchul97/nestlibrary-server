package com.server.nestlibrary.service;

import com.server.nestlibrary.model.vo.User;
import com.server.nestlibrary.repo.UserDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
