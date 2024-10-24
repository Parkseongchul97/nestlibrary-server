package com.server.nestlibrary.service;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.server.nestlibrary.model.dto.ChannelManagementDTO;
import com.server.nestlibrary.model.dto.ChannelPostDTO;
import com.server.nestlibrary.model.dto.ChannelTagDTO;
import com.server.nestlibrary.model.dto.UserDTO;
import com.server.nestlibrary.model.vo.*;
import com.server.nestlibrary.repo.ChannelDAO;
import com.server.nestlibrary.repo.ChannelTagDAO;
import com.server.nestlibrary.repo.ManagementDAO;
import com.server.nestlibrary.repo.UserDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ChannelService {
    @Autowired
    private ChannelDAO channelDAO;
    @Autowired
    private ChannelTagDAO tagDAO;
    @Autowired
    private ManagementDAO managementDAO;
    @Lazy
    @Autowired
    private  ManagementService managementService;
    @Autowired
    private UserService userService;

    @Autowired
    private UserDAO userDAO;
    @Autowired
    private  PostService postService;

    @Autowired
    private JPAQueryFactory queryFactory;
    private final QPost qPost = QPost.post;
    private final QChannel qChannel = QChannel.channel;

    public List<Channel> allChannel(){

        return channelDAO.findAll();
    }
// 페이징 시도 --------------------------------
    public Page<Channel> allChannelPage(BooleanBuilder builder, Pageable pageable){


        return channelDAO.findAll(builder, pageable);
    }
    //---------------------------------------
    // 채널 코드로 상세 page 채널코드로 (반환 : 채널)
    public Channel findChannel(int channelCode){

        return channelDAO.findById(channelCode).orElse(null);
    }

    // 채널수정 페이지에 필요한 정보 띄우기
    public ChannelManagementDTO update(int channelCode){
        Channel vo =  findChannel(channelCode);
        List<ChannelTag> tags = tagList(channelCode);
        List<UserDTO> admins = managementService.findAdmin(channelCode); // 여기 0번째는 호스트
        List<User> bans = managementService.bans(channelCode);

        ChannelManagementDTO cmDTO =  ChannelManagementDTO
                .builder()
                .channelCode(vo.getChannelCode())
                .channelName(vo.getChannelName())
                .channelCreatedAt(vo.getChannelCreatedAt())
                .channelImg(vo.getChannelImgUrl())
                .channelInfo(vo.getChannelInfo())
                .channelTag(tags)
                .favoriteCount(managementDAO.count(channelCode))
                .adminList(admins)
                .banList(bans)
                .build();

        return  cmDTO;
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

        User user = userService.getLoginUser();
        if(user.getUserPoint() < 3000){
            return null; // 포인트 부족
        }
        Channel chan = channelDAO.save(vo);
        // 해당 채널에 게시판 태그가 0개면
        if(tagDAO.findByChannelCode(chan.getChannelCode()).size() == 0){
            user.setUserPoint(user.getUserPoint()-3000);
            userDAO.save(user); // 포인트 소모
            
            createDefaultTag(chan.getChannelCode()); // 기본 채널 3개 생성
            // 채널 관리탭에 호스트 추가 -> 여기 문제 생기면 알려주세요!! (2024.10.18)
            Management man = Management.builder()
                    .channel(Channel.builder().channelCode(vo.getChannelCode()).build())
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
    }
    // 채널 태그 추가
    public ChannelTag createTag(ChannelTag vo){
        // 이전에 유저 포인트, 등급 확인후 포인트 제거 등등
        return tagDAO.save(vo);
    }
    // 채널 태그 삭제
    @Transactional
    public void removeTag(int channelTagCode ){


      ChannelTag tag = tagDAO.findById(channelTagCode).get(); // 태그 코드로 ChannelTag 객체
    int channelCode =  tag.getChannelCode();             // 해당 객체의 채널코드
       if( updateTag(channelCode , channelTagCode)) { //업데이트 실행
           tagDAO.deleteById(channelTagCode);
       }// 삭제
    }

    public boolean updateTag(int channelCode , int channelTagcode) {
       // 채널코드로 태그 리스트 뽑아서 첫번째 태그코드(일반)추출후 삭제된 태그 자리에 업데이트
        // 문제 생기면 알려주세요 (2024.10.18)
        List<ChannelTag> tags =  tagDAO.findByChannelCode(channelCode);
       int regularCode = tags.get(0).getChannelTagCode();
        queryFactory.update(qPost)
                .set(qPost.channelTag.channelTagCode, regularCode)
                .where(qPost.channelTag.channelTagCode.eq(channelTagcode))
                .execute();
        return true;
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
    // 해당 채널의 모든 정보(게시글까지)
    public ChannelPostDTO allChannelInfo(int channelCode){
        Channel vo = channelDAO.findById(channelCode).get(); // 해당 채널 vo

        List<ChannelTag> tagVoList = tagList(channelCode); // 해당채널 모든 태그vo
        List<ChannelTagDTO> tagDTOList = new ArrayList<>();
        for(ChannelTag tag : tagVoList){
           tagDTOList.add(channelTagAllPost(tag.getChannelTagCode()));
        }
        int totalCount = postService.allPostCount(channelCode,null,null);
        Paging paging = new Paging(1, totalCount); // 포스트 총숫자 0에 넣기
        paging.setTotalPage(totalCount);
        paging.setOffset(paging.getLimit() * (paging.getPage()-1));
        ChannelPostDTO dto = ChannelPostDTO.builder()
                .channelCode(channelCode)
                .channelInfo(vo.getChannelInfo())
                .channelName(vo.getChannelName())
                .channelImg(vo.getChannelImgUrl())
                .channelCreatedAt(vo.getChannelCreatedAt())
                .favoriteCount(0)// 즐찾 숫자 추가
                .channelTag(tagDTOList) // 태그 추가 + 태그 산하 게시글 추가
                .allPost(postService.channelCodeByAllPost(channelCode,paging,"","")) // 해당 채널의 모든 태그 게시글 추가
                .host(managementService.findAdmin(channelCode).get(0))
                .build();
        return dto;
    }
    // 해당 채널의 게시판 태그별 게시글 정보
    public ChannelTagDTO channelTagAllPost(int channelTagCode){
        ChannelTag vo = tagDAO.findById(channelTagCode).get();
        int totalCount = postService.tagPostCount(channelTagCode, null,null);
        Paging paging = new Paging(1, totalCount); // 포스트 총숫자 0에 넣기
        paging.setTotalPage(totalCount);
        paging.setOffset(paging.getLimit() * (paging.getPage()-1));
        return ChannelTagDTO.builder()
                .channelTagCode(vo.getChannelTagCode())
                .channelTagName(vo.getChannelTagName())
                .posts(postService.channelTagCodeByAllPost(vo.getChannelTagCode(),paging,"","")
                ).build();
    }
    // 채널 소개 수정

    @Transactional
    public void updateInfo(String channelInfo, int channelCode){

        queryFactory.update(qChannel)
                .set(qChannel.channelInfo, channelInfo)
                .where(qChannel.channelCode.eq(channelCode))
                .execute();

    }

    // 채널 이미지  url 반환
    public String  getUrl (int channelCode) {

     Channel channel =  channelDAO.findById(channelCode).get();

     return  channel.getChannelImgUrl();

    }

    @Transactional
    public void imgUpdate (String fileName , int channelCode){

        queryFactory.update(qChannel)
                .set(qChannel.channelImgUrl, fileName)
                .where(qChannel.channelCode.eq(channelCode))
                .execute();

    }

    // 모든 코드 리스트
    public List<Integer> allCode (){


        return channelDAO.findAllChannelCode();
    }

    // 채널 삭제

    public void removeChannel(int channelCode){

        channelDAO.deleteById(channelCode);
    }

    // 내 채널들
    public List<Channel> myChannel (  String userEmail ){

       List<Integer> myCodes = managementDAO.myChannel(userEmail);
       List<Channel> myChan = new ArrayList<>();

       if( myCodes != null) {
           for (int i = 0; i < myCodes.size(); i++) {

               myChan.add(channelDAO.findById(myCodes.get(i)).get());

           }

            return  myChan;
       } else {

           return null;
       }

    }



}
