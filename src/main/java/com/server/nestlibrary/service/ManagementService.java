package com.server.nestlibrary.service;


import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.server.nestlibrary.model.vo.Management;

import com.server.nestlibrary.model.vo.User;
import com.server.nestlibrary.model.vo.*;
import com.server.nestlibrary.repo.ManagementDAO;
import com.server.nestlibrary.repo.UserDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ManagementService {


    @Autowired
    private ManagementDAO managementDAO;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private JPAQueryFactory queryFactory;

    private final QPostLike qPostLike = QPostLike.postLike;
    private final QPost qPost = QPost.post;
    private final QUser qUser = QUser.user;
    private final QChannel qChannel = QChannel.channel;
    private final QManagement qManagement = QManagement.management;





    // 해당 채널의 관리자들 user 로 반환 0번째는 호스트
    public List<User> findAdmin(int channelCode) {
        List<Management> adminList = queryFactory.selectFrom(qManagement)
                .where(qManagement.channelCode.eq(channelCode))
                .where(
                        qManagement.managementUserStatus.eq("host")
                        .or(qManagement.managementUserStatus.eq("admin"))
                ).orderBy(
                        Expressions.numberTemplate(Integer.class,
                                        "case when {0} = {1} then {2} else {3} end",
                                        qManagement.managementUserStatus, "host", 1, 2)
                                .asc()
                ).fetch();
        List<User> userList = new ArrayList<>();
        for (Management m : adminList) {
            userList.add(userDAO.findById(m.getUserEmail()).get());
        }
        return userList;
    }



    // 로그인 유저가 벤되었나 확인
    public Management findBan(int channelCode) {
        List<Management> banList = queryFactory.selectFrom(qManagement)
                .where(qManagement.channelCode.eq(channelCode))
                .where(qManagement.managementUserStatus.eq("ban"))
                .where(qManagement.userEmail.eq(getEmail()))
                .fetch();
        if(banList.size() > 0){ // 내가 벤되었다면
            return banList.get(0);
        }else{ // 일반적으로 접근가능상황
            return null;
        }

    }

    //벤된 애들

    public List<User> bans (int channelCode){

        List<Management> banUser = queryFactory.selectFrom(qManagement)
                .where(qManagement.channelCode.eq(channelCode))
                .where(qManagement.managementUserStatus.eq("ban"))

                .fetch();

        List<User> userList = new ArrayList<>();
        for (Management m : banUser) {
            userList.add(userDAO.findById(m.getUserEmail()).get());
        }

        return userList;
    }

    private String getEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            User user = (User) auth.getPrincipal();
            return user.getUserEmail();
        }
        return null;
    }

    // 구독하기
    public void subscribe(Management vo){

        managementDAO.save(vo);
    }

    // 구독 취소
    public  void remove(int managementCode){

        managementDAO.deleteById(managementCode);
    }

    // 구독체크
    public Management check(int channelCode ){

        return managementDAO.check(channelCode, getEmail());
    }

    // 구독자 수
    public int count(int channelCode){

        return managementDAO.count(channelCode);
    }


}


