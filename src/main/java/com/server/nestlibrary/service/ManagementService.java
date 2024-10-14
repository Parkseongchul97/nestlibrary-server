package com.server.nestlibrary.service;


import com.querydsl.jpa.impl.JPAQueryFactory;
import com.server.nestlibrary.model.vo.Management;
import com.server.nestlibrary.model.vo.QManagement;
import com.server.nestlibrary.model.vo.User;
import com.server.nestlibrary.repo.ManagementDAO;
import com.server.nestlibrary.repo.UserDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ManagementService {


    @Autowired
    private ManagementDAO managementDAO;

    @Autowired
    private JPAQueryFactory queryFactory;



    private final QManagement qManagement = QManagement.management;



    // 사용자 아이디
    private String getUserEmail(){

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if( auth != null && auth.isAuthenticated()){
            User user = (User) auth.getPrincipal();
            return user.getUserEmail();
        }
        return  null;
    }


    @Autowired
    private UserDAO userDAO;

     // 채널 호스트 찾기
    public User findHost (int channelCode){
        return  userDAO.findById(managementDAO.findHost(channelCode).getUserEmail()).orElse(null);
    }

    // 구독하기
    public void subscribe(Management vo){

        managementDAO.save(vo);
    }

    public  void remove(int managementCode){

        managementDAO.deleteById(managementCode);
    }

    // 구독체크
    public Management check(int channelCode ){

        return managementDAO.check(channelCode, getUserEmail());
    }


}
