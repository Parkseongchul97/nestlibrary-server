package com.server.nestlibrary.service;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryFactory;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.server.nestlibrary.model.dto.*;
import com.server.nestlibrary.model.vo.*;
import com.server.nestlibrary.repo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
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
    private PostDAO postDAO;
    @Autowired
    private UserDAO userDAO;
    @Autowired
    private CommentDAO commentDAO;
    @Lazy
    @Autowired
    private  PostService postService;
    @Lazy
    @Autowired
    private  CommentService commentService;

    @Autowired
    private JPAQueryFactory queryFactory;
    private final QPost qPost = QPost.post;
    private final QChannel qChannel = QChannel.channel;
    private final QComment qComment = QComment.comment;
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
                .channelImgUrl(vo.getChannelImgUrl())
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
        if(user.getUserPoint() < 3000 && vo.getChannelCode()==0){
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
        int totalCount = postService.postQuery(channelCode, null,null ,null , false).fetch().size();
        Paging paging = new Paging(1, totalCount); // 포스트 총숫자 0에 넣기
        paging.setTotalPage(totalCount);
        paging.setOffset(paging.getLimit() * (paging.getPage()-1));
        ChannelPostDTO dto = ChannelPostDTO.builder()
                .channelCode(channelCode)
                .channelInfo(vo.getChannelInfo())
                .channelName(vo.getChannelName())
                .channelImgUrl(vo.getChannelImgUrl())
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
        int totalCount = postService.postQuery(channelTagCode, null,null ,null , true).fetch().size();
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

       if( myCodes.size() > 0) {
           for (int i = 0; i < myCodes.size(); i++) {

               myChan.add(channelDAO.findById(myCodes.get(i)).get());

           }

            return  myChan;
       } else {

           return null;
       }

    }
    //
    public List<MostChannelDTO> favoriteChannel(String userEmail){
        HashMap<Integer, Integer> map = new HashMap<>();
        // 맵에다가 채널코드 : 댓글수 + 포스트 수 넣고 오름 차순 정렬후 높은 채널코드 3개만 뽑아서 그거로
        // userRole을 만들기?
        // 우선 글을 쓴 채널 코드 목록이 필요
        List<Integer> manList = postService.findChannelCode(userEmail);
        // 각 채널 코드들로 이 사람이 작성한 글과 댓글 수를 기록

        // 채널코드들로 작성글 수 기록
        if(manList.size() != 0){
            for(int i=0; i<manList.size(); i++ ){

                 map.put(manList.get(i),postDAO.postCount( manList.get(i) , userEmail) + commentDAO.commentCount(userEmail,manList.get(i)));
            }



            List<Map.Entry<Integer, Integer>> sortedEntries = map.entrySet()
                    .stream()
                    .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                    .collect(Collectors.toList());

            log.info("값" + sortedEntries);
            // 이제 여기서
            List<Integer> favoriteCode = new ArrayList<>();
            for(int i=0;  i<sortedEntries.size(); i++){

                favoriteCode.add(sortedEntries.get(i).getKey());


            }

            List<MostChannelDTO> mostList = new ArrayList<>();
            LocalDateTime today = LocalDateTime.now().minusDays(6);
            for(int i=0; i< favoriteCode.size(); i++){
                    MostChannelDTO dto = MostChannelDTO

                            .builder()
                            .channelCode(favoriteCode.get(i))
                            .channelName(channelDAO.findById(favoriteCode.get(i)).get().getChannelName())
                            .postCount(postDAO.postCount(favoriteCode.get(i), userEmail))
                            .commentCount(commentDAO.commentCount(userEmail, favoriteCode.get(i)))
                            .build();


                    List<ChartDTO> chartList= new ArrayList<>();
                for(int j=0; j< 7; j++){

                    chartList.add(ChartDTO.builder()
                            .date(today.plusDays(j))
                            .postCount(channelPostCountQuery(userEmail,favoriteCode.get(i), today.plusDays(j)))
                            .CommentCount(channelCommentCountQuery(userEmail, favoriteCode.get(i),today.plusDays(j)))
                            .build());
                    log.info("날짜라인"  + today.minusDays(j));
                }
                dto.setChartDTO(chartList);
                mostList.add(dto);
                if(i == 2)break;
            }



        return mostList;
        }

        return null;
    }
    public int channelPostCountQuery(String userEmail, int channelCode, LocalDateTime time){
        String dateStr = time.toLocalDate().toString();
        log.info("날짜 : "  + time);
        log.info(dateStr);
        if(userEmail != null){ // 채널 포스트 카운트 조회라면
            return  queryFactory.selectFrom(qPost)
                    .where(qPost.userEmail.eq(userEmail))
                    .where(qPost.channel.channelCode.eq(channelCode))
                    .where(qPost.postCreatedAt.stringValue().like(dateStr + "%"))
                    .fetch().size();
        }
            return  queryFactory.selectFrom(qPost)
                    .where(qPost.channel.channelCode.eq(channelCode))
                    .where(qPost.postCreatedAt.stringValue().like(dateStr + "%"))
                    .fetch().size();
    }
    public int channelCommentCountQuery(String userEmail, int channelCode, LocalDateTime time){
        String dateStr = time.toLocalDate().toString();
        if(userEmail != null){ // 채널 포스트 카운트 조회라면
            return commentDAO.commentUserCount(userEmail,channelCode,dateStr)  ;
        }
        return  commentDAO.commentChannelCount(channelCode,dateStr);
    }



}
