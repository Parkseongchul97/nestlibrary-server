package com.server.nestlibrary.service;

import com.server.nestlibrary.model.vo.Channel;
import com.server.nestlibrary.model.vo.ChannelTag;
import com.server.nestlibrary.model.vo.Management;
import com.server.nestlibrary.model.vo.User;
import com.server.nestlibrary.repo.ChannelDAO;
import com.server.nestlibrary.repo.ChannelTagDAO;
import com.server.nestlibrary.repo.ManagementDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChannelService {
    @Autowired
    private ChannelDAO channelDAO;
    @Autowired
    private ChannelTagDAO tagDAO;
    @Autowired
    private ManagementDAO managementDAO;

    public List<Channel> allChannel(){


        return channelDAO.findAll();
    }
    // 채널 코드로 상세 page 채널코드로 (반환 : 채널)
    public Channel findChannel(int channelCode){

        return channelDAO.findById(channelCode).orElse(null);
    }
    // 채널 이름 중복체크 (반환 : 채널)
    public Channel findByChannelName(Channel vo){
        Channel chan = channelDAO.findByChannelName(vo.getChannelName());
        if(chan != null&&(chan.getChannelCode()!=vo.getChannelCode())) { // 내가 설정하려는 채널명이 중복이라면
            return chan; // 이미 존재하는 채널 반환
        }
        return null; // 중복 X의 경우
    }
    // 채널 생성 메서드 (반환 : 채널)
    public Channel createChannel(Channel vo){
        Channel chan = channelDAO.save(vo);
        // 해당 채널에 게시판 태그가 0개면
        if(tagDAO.findByChannelCode(chan.getChannelCode()).size() == 0){
        createDefaultTag(chan.getChannelCode()); // 기본 채널 3개 생성
            // 채널 관리탭에 호스트 추가
            Management man = Management.builder()
                    .channelCode(vo.getChannelCode())
                    .managementUserStatus("host")
                    .userEmail(getLoginUser())
                    .build();
            managementDAO.save(man);

        }
        return chan;
    }

    // 기본채널 생성 메서드
    public void createDefaultTag(int ChannelCode){
        tagDAO.save(ChannelTag.builder().channelCode(ChannelCode).channelTagName("일반").build());
        tagDAO.save(ChannelTag.builder().channelCode(ChannelCode).channelTagName("공지").build());
        tagDAO.save(ChannelTag.builder().channelCode(ChannelCode).channelTagName("인기글").build());
    }
    // 채널 태그 추가
    public ChannelTag createTag(ChannelTag vo){
        // 이전에 유저 포인트, 등급 확인후 포인트 제거 등등
        return tagDAO.save(vo);
    }
    // 채널 태그 삭제
    public void removeTag(int channelTagCode){
        tagDAO.deleteById(channelTagCode);
    }

    //  + 채널코드로 채널태그 가져오기
    public List<ChannelTag> tagList (int channelCode){
      List<ChannelTag> tags =  tagDAO.findByChannelCode(channelCode);
      return tags;
    }


    public String getLoginUser(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth!= null && auth.isAuthenticated()){
            User user = (User) auth.getPrincipal();
            return user.getUserEmail();
        }
        return null;
    }
}
