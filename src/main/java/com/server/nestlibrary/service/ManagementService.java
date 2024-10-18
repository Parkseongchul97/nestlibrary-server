package com.server.nestlibrary.service;


import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.server.nestlibrary.model.dto.ChannelDTO;
import com.server.nestlibrary.model.dto.UserDTO;
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
    private  ChannelService channelService;
    @Autowired
    private JPAQueryFactory queryFactory;


    private final QManagement qManagement = QManagement.management;




    // 해당 채널의 관리자들 user 로 반환 0번째는 호스트
    public List<UserDTO> findAdmin(int channelCode) {
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
        List<UserDTO> userList = new ArrayList<>();
        for (Management m : adminList) {
            User vo = userDAO.findById(m.getUserEmail()).get();
            userList.add(UserDTO.builder()
                            .userEmail(vo.getUserEmail())
                            .userImg(vo.getUserImgUrl())
                            .userNickname(vo.getUserNickname())
                            .build());
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

    private String getEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            User user = (User) auth.getPrincipal();
            return user.getUserEmail();
        }
        return null;
    }

    // 구독하기
    public Management subscribe(Management vo){
        vo.setManagementUserStatus("sub");
       return managementDAO.save(vo);
    }

    // 구독 취소
    public  void remove(int managementCode){

        managementDAO.deleteById(managementCode);
    }

    // 구독체크
    public Management check(int channelCode ){
       List<Management> list =  queryFactory.selectFrom(qManagement)
                .where(qManagement.userEmail.eq(getEmail()))
                .where(qManagement.channelCode.eq(channelCode))
                .where(qManagement.managementUserStatus.eq("sub"))
                .fetch();
        if(list.size() == 0)
        return null ;
        return list.get(0);
    }

    public  List<ChannelDTO> mySubscribe(){
        List<Management> list =  queryFactory.selectFrom(qManagement)
                .where(qManagement.userEmail.eq(getEmail()))
                .where(qManagement.managementUserStatus.eq("sub"))
                .fetch();
        List<ChannelDTO> dto = new ArrayList<>();
        for (Management m : list){
          Channel vo = channelService.findChannel(m.getChannelCode()) ;
          dto.add(ChannelDTO.builder()
                    .channelCode(vo.getChannelCode())
                    .channelImg(vo.getChannelImgUrl())
                    .channelCreatedAt(vo.getChannelCreatedAt())
                    .channelName(vo.getChannelName())
                    .channelInfo(vo.getChannelInfo())
                    .channelTag(channelService.tagList(vo.getChannelCode()))
                    .host(findAdmin(vo.getChannelCode()).get(0))
                    .favoriteCount(count(vo.getChannelCode()))
                    .build());
        }
        return dto;
    }

    // 구독자 수
    public int count(int channelCode){

        return managementDAO.count(channelCode);
    }


}


