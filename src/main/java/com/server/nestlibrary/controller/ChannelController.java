package com.server.nestlibrary.controller;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.server.nestlibrary.model.dto.*;
import com.server.nestlibrary.model.vo.*;
import com.server.nestlibrary.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/*")
@CrossOrigin(origins = {"*"}, maxAge = 6000)
public class ChannelController {

    @Autowired
    private ManagementService managementService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private UserService userService;

    @Autowired
    private PostService postService;

    @Autowired
    private JPAQueryFactory queryFactory;

    @Autowired
    private CommentService commentService;

    private final QChannel qChannel = QChannel.channel;
    private final QManagement qManagement = QManagement.management;
    private final QPost qPost = QPost.post;
    private final QChannelTag qChannelTag = QChannelTag.channelTag;


    // 모든 채널 조회(메인)
    @GetMapping("/channel/main")
    public ResponseEntity allChannel(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "keyword", required = false) String keyword) {

        BooleanBuilder builder = new BooleanBuilder();
        // 임시
        Pageable pageable = PageRequest.of(page - 1, 4);
        List<Tuple> channelQuery = new ArrayList<>();
        if(keyword != null && keyword != "") {

            channelQuery = channelJPAQuery(page).where(qChannel.channelName.like("%" + keyword + "%")).fetch();
        }else {

            channelQuery = channelJPAQuery(page).fetch();


        }
            List<Integer> channelCodes = new ArrayList<>();

          for(int i=0; i< channelQuery.size(); i++){

              channelCodes.add(channelQuery.get(i).get(0, Integer.class));
          }



        List<Channel> channels = new ArrayList<>();

        for(int i=0; i<channelCodes.size(); i++){

            channels.add(channelService.findChannel(channelCodes.get(i))) ;
        }


        List<ChannelPostDTO> dtoList = new ArrayList<>(); // 최종적으로 뽑을 dto리스트 생성
        for (Channel c : channels) { // 채널 코드로 post vo list 10개
            List<Post> posts = byChannelCode(c.getChannelCode());
            List<PostDTO> postDTOs = new ArrayList<>();
            for (Post p : posts) { // 포스트 vo -> dto 포장
                postDTOs.add(changePostVoDTO(p));
            }
            dtoList.add(mainDTO(c,postDTOs));

        }
        return ResponseEntity.ok(dtoList);
    }


    // 구독 채널 조회 (메인)
    // 시간나면 여기에 구독 여부까지 추가해야함!! 그거로 화인
    @GetMapping("/private/channel/main")
    public ResponseEntity myChannels(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "keyword", required = false) String keyword) {

        Pageable pageable = PageRequest.of(page - 1, 4);;


        List<Tuple> channelQuery = new ArrayList<>();

     if(keyword != null && keyword != ""){
         channelJPAQuery(page).where(qChannel.channelName.like("%" + keyword + "%"))
                 .where(qManagement.managementUserStatus.eq("sub"))
                 .where(qManagement.userEmail.eq(userService.getLoginUser().getUserEmail())).fetch();
     }else {
         channelJPAQuery(page)
                 .where(qManagement.managementUserStatus.eq("sub"))
                 .where(qManagement.userEmail.eq(userService.getLoginUser().getUserEmail()))
                 .fetch();

     }

        List<Integer> channelCodes = new ArrayList<>();

        for(int i=0; i< channelQuery.size(); i++){

            channelCodes.add(channelQuery.get(i).get(0, Integer.class));
        }

        List<Channel> channels = new ArrayList<>();

        for(int i=0; i<channelCodes.size(); i++){

            channels.add(channelService.findChannel(channelCodes.get(i))) ;
        }


        List<ChannelPostDTO> dtoList = new ArrayList<>(); // 최종적으로 뽑을 dto리스트 생성


        for (Channel c : channels) { // 채널 코드로 post vo list 10개
            List<Post> posts = byChannelCode(c.getChannelCode());
            List<PostDTO> postDTOs = new ArrayList<>();
            for (Post p : posts) { // 포스트 vo -> dto 포장
                postDTOs.add(changePostVoDTO(p));
            }
            dtoList.add(mainDTO(c,postDTOs));
        }
        return ResponseEntity.ok(dtoList);
    }

    public PostDTO changePostVoDTO(Post vo){
        return   PostDTO
                .builder()
                .postCode(vo.getPostCode())
                .postTitle(vo.getPostTitle())
                .postCreatedAt(vo.getPostCreatedAt())
                .postContent(vo.getPostContent())
                .postViews(vo.getPostViews())
                .channelTag(vo.getChannelTag())
                .channelCode(vo.getChannel().getChannelCode())
                .commentCount(commentService.commentCount(vo.getPostCode()))
                .user(userService.findDTO(vo.getUserEmail()))
                .bestPoint(postService.postViewCount(vo.getPostCode()) + (postService.postLikeCount(vo.getPostCode())*5) + (postService.postCommentCount(vo.getPostCode())*2))
                .build();
    }

    // 채널정보 조회
    @GetMapping("/channel/{channelCode}")
    public ResponseEntity channelSub(@PathVariable(name = "channelCode") int channelCode) {
        Channel vo = channelService.findChannel(channelCode);
        ChannelDTO dto = ChannelDTO.builder()
                .channelCode(channelCode)
                .channelImgUrl(vo.getChannelImgUrl())
                .channelCreatedAt(vo.getChannelCreatedAt())
                .channelName(vo.getChannelName())
                .channelInfo(vo.getChannelInfo())
                .channelTag(channelService.tagList(channelCode))
                .host(managementService.findAdmin(channelCode).get(0))
                .favoriteCount(managementService.count(channelCode))
                .build();
        return ResponseEntity.ok(dto);
    }
    // 채널 공지 가져오기
    @GetMapping("/channel/announcement/{channelCode}")
    public ResponseEntity channelAnnouncement(@PathVariable(name = "channelCode") int channelCode) {
        return ResponseEntity.ok(postService.channelAnnouncement(channelCode));
    }

    // 채널 모든 게시글
    @GetMapping("/{channelCode}")
    public ResponseEntity allPost(@PathVariable(name = "channelCode") int channelCode,
                                  @RequestParam(name = "page", defaultValue = "1") int page,
                                  @RequestParam(name = "target", defaultValue = "", required = false) String target,
                                  @RequestParam(name = "keyword", defaultValue = "", required = false) String keyword
    ) {
        int totalCount = postService.postQuery(channelCode, target, keyword,null,false).fetch().size();
        Paging paging = new Paging(page, totalCount);
        paging.setTotalPage(totalCount);
        paging.setOffset(paging.getLimit() * (paging.getPage() - 1));
        List<PostDTO> postList = postService.channelCodeByAllPost(channelCode, paging, target, keyword);
        BoardDTO postBoard = BoardDTO.builder().postList(postList).paging(paging).build();
        return ResponseEntity.ok(postBoard);
    }

    // 채널의 인기 게시판 조회
    @GetMapping("/{channelCode}/best")
    public ResponseEntity bestPost(@PathVariable(name = "channelCode") int channelCode,
                                   @RequestParam(name = "page", defaultValue = "1") int page,
                                   @RequestParam(name = "target", defaultValue = "", required = false) String target,
                                   @RequestParam(name = "keyword", defaultValue = "", required = false) String keyword
    ) {
        return ResponseEntity.ok(postService.channelCodeByBestPost(channelCode,page,target,keyword));
    }
    // 채널 태그 인기 게시글
    @GetMapping("/{channelCode}/{channelTagCode}/best")
    public ResponseEntity bestTagPost(@PathVariable(name = "channelCode") int channelCode,
                                      @PathVariable(name = "channelTagCode") int channelTagCode,
                                   @RequestParam(name = "page", defaultValue = "1") int page,
                                   @RequestParam(name = "target", defaultValue = "", required = false) String target,
                                   @RequestParam(name = "keyword", defaultValue = "", required = false) String keyword
    ) {
        return ResponseEntity.ok(postService.channelTagByBestPost(channelTagCode,page,target,keyword));
    }
    // 채널의 세부탭 게시판 조회
    @GetMapping("/{channelCode}/{channelTagCode}")
    public ResponseEntity tagPost(@PathVariable(name = "channelCode") int channelCode, @PathVariable(required = false, name = "channelTagCode") int channelTagCode,
                                  @RequestParam(name = "page", defaultValue = "1") int page,
                                  @RequestParam(name = "target", defaultValue = "", required = false) String target,
                                  @RequestParam(name = "keyword", defaultValue = "", required = false) String keyword) {
        int totalCount = postService.postQuery(channelTagCode, target, keyword,null,true).fetch().size(); // 해당하는 총 게시글 숫자
        Paging paging = new Paging(page, totalCount); // 맞춰서 생성하는 페이징 객체
        paging.setTotalPage(totalCount);
        paging.setOffset(paging.getLimit() * (paging.getPage() - 1));
        List<PostDTO> postList = postService.channelTagCodeByAllPost(channelTagCode, paging, target, keyword);
        BoardDTO postBoard = BoardDTO.builder().postList(postList).paging(paging).build();
        return ResponseEntity.ok(postBoard);// 화면단에서 사용할 형태로 맞춘 DTO
    }

    // 채널 이름 중복 확인
    @GetMapping("/channel/name")
    public ResponseEntity findByChannelName(@RequestParam(name = "channelName") String channelName, @RequestParam(required = false, name = "channelCode") int channelCode) {

        return ResponseEntity.ok(channelService.findByChannelName(Channel.builder().channelCode(channelCode).channelName(channelName).build()));
    }

    // 채널 생성(프라이빗 추가)
    @PostMapping("/private/channel/create")
    public ResponseEntity createChannel(ChannelDTO dto) throws Exception {
        Channel channel = channelService.createChannel(Channel
                .builder()
                .channelName(dto.getChannelName())
                .channelInfo(dto.getChannelInfo())
                .channelCreatedAt(LocalDateTime.now())
                .build());
        if (channel == null) {
            return ResponseEntity.ok(null);
        }
        Path directoryPath = Paths.get("\\\\\\\\192.168.10.51\\\\nest\\\\channel\\" + String.valueOf(channel.getChannelCode()) + "\\");
        Files.createDirectories(directoryPath);
        channel.setChannelImgUrl(fileUpload(dto.getChannelImg(), channel.getChannelCode())); // 이미지 추가
        Channel result = channelService.createChannel(channel);
        return ResponseEntity.ok(result);
    }

    // 채널 태그 추가
    @PostMapping("/private/channel/tag")
    public ResponseEntity createChannelTag(@RequestBody ChannelTag vo) throws Exception {
        ChannelTag tag = channelService.createTag(vo);
        log.info("생성된 새부 게시판 : " + tag);

        return ResponseEntity.ok(tag);
    }

    // 채널 태그 삭제
    @DeleteMapping("/private/channel/tag/{channelTagCode}")
    public ResponseEntity createChannelTag(@PathVariable(name = "channelTagCode") int channelTagCode) throws Exception {
        channelService.removeTag(channelTagCode);
        log.info("생성된 새부 게시판 삭제");
        // 해당 태그 밑에 있던 게시글들 처리? 일반탭으로
        return ResponseEntity.ok(null);
    }

    // 채널 수정 페이지
    @GetMapping("/private/channel/update/{channelCode}")
    public ResponseEntity updatePage(@PathVariable(name = "channelCode") int channelCode) {

        List<Channel> list = channelService.myChannel(userService.getLoginUser().getUserEmail());

        for (int i = 0; i < list.size(); i++) {

            if (list.get(i).getChannelCode() == channelCode) {

                ChannelManagementDTO dto = channelService.update(channelCode);
                return ResponseEntity.ok(dto);
            }
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("채널을 찾을 수 없습니다.");
    }

    @GetMapping("/private/channel/chart/{channelCode}")
    public ResponseEntity channelChart(@PathVariable(name = "channelCode") int channelCode) {
        MostChannelDTO dto =  channelService.channelChart(channelCode);
         List<MostChannelDTO>  list = new ArrayList<>();
         list.add(dto);

        return ResponseEntity.ok(list);
    }

    // 채널 소개 수정
    @PutMapping("/private/channel/update")
    public ResponseEntity updateInfo(@RequestBody Channel vo) {

        channelService.updateInfo(vo.getChannelInfo(), vo.getChannelCode());

        return ResponseEntity.ok(null);


    }

    // 채널 이미지 수정
    @PutMapping("/private/channel/channelImg")
    public ResponseEntity imgUpdate(ChannelDTO dto) throws Exception {

        String imgUrl = channelService.getUrl(dto.getChannelCode());
        // 기본 프사 사용시
        if (dto.getChange() == -1) {
            // 기존 이미지가 있는 경우
            if (imgUrl != null) {
                fileDelete(imgUrl, dto.getChannelCode());
                channelService.imgUpdate(null, dto.getChannelCode());
                //   // 기존 이미지가 있는 경우  없는경우
            } else {
                channelService.imgUpdate(null, dto.getChannelCode());
            }
        } else if (dto.getChange() == 1) {
            if (imgUrl != null) {
                fileDelete(imgUrl, dto.getChannelCode());
                channelService.imgUpdate(fileUpload(dto.getChannelImg(), dto.getChannelCode()), dto.getChannelCode());
            } else {
                channelService.imgUpdate(fileUpload(dto.getChannelImg(), dto.getChannelCode()), dto.getChannelCode());
            }

        }


        return ResponseEntity.ok(null);
    }

    // 채널 삭제
    @DeleteMapping("/private/channel/{channelCode}")
    public ResponseEntity removeChannel(@PathVariable(name = "channelCode") int channelCode) {
        channelService.removeChannel(channelCode);
        folderDelete(channelCode);

        return ResponseEntity.ok(null);
    }

    // 내 채널 정보
    @GetMapping("/private/channel/{userEmail}")
    public ResponseEntity myChannel(@PathVariable(name = "userEmail") String userEmail) {
        return ResponseEntity.ok(channelService.myChannel(userEmail));
    }









    // 파일 업로드
    public String fileUpload(MultipartFile file, int channelCode) throws IllegalStateException, Exception {
        if (file == null || file.getOriginalFilename().equals("")) {
            return null;
        }
        UUID uuid = UUID.randomUUID(); // 랜덤 파일명 부여
        String fileName = uuid.toString() + "_" + file.getOriginalFilename();

        File copyFile = new File("\\\\192.168.10.51\\nest\\channel\\" + String.valueOf(channelCode) + "\\" + fileName);
        file.transferTo(copyFile);
        return fileName;
    }

    // 삭제
    public void fileDelete(String file, int channelCode) throws IllegalStateException, Exception {
        if (file != null) {
            String decodedString = URLDecoder.decode(file, StandardCharsets.UTF_8.name()); // 한글 디코딩 처리
            File f = new File("\\\\192.168.10.51\\nest\\channel\\" + String.valueOf(channelCode) + "\\" + decodedString);
            f.delete();
        }
    }

    public boolean folderDelete(int channelCode) {
        String path = "\\\\192.168.10.51\\nest\\channel\\" + Integer.toString(channelCode);
        File folder = new File(path); //
        try {
            while (folder.exists()) { // 폴더가 존재한다면
                File[] listFiles = folder.listFiles();

                for (File file : listFiles) { // 폴더 내 파일을 반복시켜서 삭제
                    file.delete();
                }

                if (listFiles.length == 0 && folder.isDirectory()) { // 하위 파일이 없는지와 폴더인지 확인 후 폴더 삭제
                    folder.delete();
                }


            }
        }
        catch (Exception e) {
            return false;
        }
        return true;
    }







    public JPAQuery<Tuple> channelJPAQuery(int page){

        Pageable pageable = PageRequest.of(page - 1, 4);



        JPAQuery<Tuple> channelQuery = queryFactory.select(qChannel.channelCode,
                ExpressionUtils.as(JPAExpressions.select(qManagement.count())
                        .from(qManagement)
                        .where(qManagement.managementUserStatus.eq("sub"))
                        .where(qManagement.channel.channelCode.eq(qChannel.channelCode))
                        .groupBy(qManagement.channel.channelCode), "sub_count"),
                ExpressionUtils.as(JPAExpressions.select(qPost.count())
                        .from(qPost)
                        .where(qPost.channel.channelCode.eq(qChannel.channelCode))
                        .groupBy(qPost.channel.channelCode), "post_count")
                ).from(qChannel).orderBy(Expressions.stringPath("sub_count").desc())
                .orderBy(Expressions.stringPath("post_count").desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        return channelQuery;

    }
    public List<Post> byChannelCode(int channelCode){
        return queryFactory.selectFrom(qPost)
                .join(qChannelTag).on(qPost.channelTag.eq(qChannelTag))
                .where(qPost.channel.channelCode.eq(channelCode))
                .orderBy(qPost.postCreatedAt.desc())
                .limit(10)
                .fetch();
    }
    public ChannelPostDTO mainDTO(Channel c, List<PostDTO> dtos){
        return   ChannelPostDTO.builder()
                .channelCode(c.getChannelCode())
                .channelName(c.getChannelName())
                .channelCreatedAt(c.getChannelCreatedAt())
                .channelImgUrl(c.getChannelImgUrl())
                .host(managementService.findAdmin(c.getChannelCode()).get(0))
                .favoriteCount(managementService.count(c.getChannelCode()))
                .channelInfo(c.getChannelInfo())
                .allPost(dtos)
                .build();
    }





    }





