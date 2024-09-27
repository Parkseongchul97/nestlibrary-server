package com.server.nestlibry.service;

import com.server.nestlibry.model.vo.User;
import com.server.nestlibry.repo.UserDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserDAO dao;

    public void changeUser(User user){
        dao.save(user);
    }
}
