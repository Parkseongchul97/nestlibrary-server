package com.server.nestlibrary.controller;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.server.nestlibrary.model.dto.*;
import com.server.nestlibrary.model.vo.*;
import com.server.nestlibrary.repo.ManagementDAO;
import com.server.nestlibrary.service.*;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.query.criteria.JpaExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.net.Proxy;
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
        // 쿼리 반복부분
        List<Channel> channels = new ArrayList<>();
        if (keyword != null && keyword != "") { // 검색어가 있는경우
            BooleanExpression expression = qChannel.channelName.like("%" + keyword + "%");
            channels = channelJPAQuery(page).where(qChannel.channelName.like("%" + keyword + "%")).fetch();
            builder.and(expression);
        } else {  // 검색어가 없는경우
            channels = channelJPAQuery(page).fetch();
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

        BooleanBuilder builder = new BooleanBuilder();
        Pageable pageable = PageRequest.of(page - 1, 4);;
        List<Channel> channels = new ArrayList<>();
        if (keyword != null && keyword != "") {
            BooleanExpression expression = qChannel.channelName.like("%" + keyword + "%");
            builder.and(expression);
            channels = channelJPAQuery(page).where(qChannel.channelName.like("%" + keyword + "%"))
                                            .where(qManagement.managementUserStatus.eq("sub"))
                                            .where(qManagement.userEmail.eq(userService.getLoginUser().getUserEmail())).fetch();
        } else {
            channels = channelJPAQuery(page)
                        .where(qManagement.managementUserStatus.eq("sub"))
                        .where(qManagement.userEmail.eq(userService.getLoginUser().getUserEmail()))
                        .fetch();
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
                .build();
    }

    // 채널정보 조회
    @GetMapping("/channel/{channelCode}")
    public ResponseEntity channelSub(@PathVariable(name = "channelCode") int channelCode) {
        Channel vo = channelService.findChannel(channelCode);
        ChannelDTO dto = ChannelDTO.builder()
                .channelCode(channelCode)
                .channelImg(vo.getChannelImgUrl())
                .channelCreatedAt(vo.getChannelCreatedAt())
                .channelName(vo.getChannelName())
                .channelInfo(vo.getChannelInfo())
                .channelTag(channelService.tagList(channelCode))
                .host(managementService.findAdmin(channelCode).get(0))
                .favoriteCount(managementService.count(channelCode))
                .build();
        log.info("채널정보 : " + dto);
        return ResponseEntity.ok(dto);
    }

    // 채널의 전체 게시판 조회
    @GetMapping("/{channelCode}")
    public ResponseEntity allPost(@PathVariable(name = "channelCode") int channelCode,
                                  @RequestParam(name = "page", defaultValue = "1") int page,
                                  @RequestParam(name = "target", defaultValue = "", required = false) String target,
                                  @RequestParam(name = "keyword", defaultValue = "", required = false) String keyword
    ) {

        int totalCount = postService.allPostCount(channelCode, target, keyword);
        Paging paging = new Paging(page, totalCount); // 포스트 총숫자 0에 넣기
        paging.setTotalPage(totalCount);
        paging.setOffset(paging.getLimit() * (paging.getPage() - 1));
        // 전체 게시글 파라미터로 ?p=1~10%target=유저or제목,내용%search=검색어
        List<PostDTO> postList = postService.channelCodeByAllPost(channelCode, paging, target, keyword);
        BoradDTO postBorad = BoradDTO.builder().postList(postList).paging(paging).build();
        // 페이징도 같이 담긴걸로?
        return ResponseEntity.ok(postBorad);
    }

    // 채널의 세부탭 게시판 조회
    @GetMapping("/{channelCode}/{channelTagCode}")
    public ResponseEntity tagPost(@PathVariable(name = "channelCode") int channelCode, @PathVariable(required = false, name = "channelTagCode") int channelTagCode,
                                  @RequestParam(name = "page", defaultValue = "1") int page,
                                  @RequestParam(name = "target", defaultValue = "", required = false) String target,
                                  @RequestParam(name = "keyword", defaultValue = "", required = false) String keyword) {
        // 게시글 파라미터로 ?p=1~10%target=유저or제목,내용%search=검색어
        int totalCount = postService.tagPostCount(channelTagCode, target, keyword);
        Paging paging = new Paging(page, totalCount); // 포스트 총숫자 0에 넣기
        paging.setTotalPage(totalCount);
        paging.setOffset(paging.getLimit() * (paging.getPage() - 1));
        List<PostDTO> postList = postService.channelTagCodeByAllPost(channelTagCode, paging, target, keyword);
        BoradDTO postBorad = BoradDTO.builder().postList(postList).paging(paging).build();
        return ResponseEntity.ok(postBorad);
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
        channel.setChannelImgUrl(fileUpload(dto.getChannelImgUrl(), channel.getChannelCode())); // 이미지 추가
        Channel result = channelService.createChannel(channel);
        log.info("message : " + channel);
        return ResponseEntity.ok(result);
    }

    // 채널 태그 추가
    @PostMapping("/private/channel/tag")
    public ResponseEntity createChannelTag(@RequestBody ChannelTag vo) throws Exception {
        ChannelTag tag = channelService.createTag(vo);
        log.info("생성된 새부 게시판 : " + tag);
        // 태그 추가시 포인트 감소
        return ResponseEntity.ok(tag);
    }

    // 채널 태그 삭제
    @DeleteMapping("/private/channel/tag/{channelTagCode}")
    public ResponseEntity createChannelTag(@PathVariable(name = "channelTagCode") int channelTagCode) throws Exception {
        channelService.removeTag(channelTagCode);
        log.info("생성된 새부 게시판 삭제");
        // 해당 태그 밑에 있던 게시글들 처리? 일반탭으로? 아님 삭제
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


    // 채널 소개 수정
    @PutMapping("/private/channel/update")
    public ResponseEntity updateInfo(@RequestBody Channel vo) {

        channelService.updateInfo(vo.getChannelInfo(), vo.getChannelCode());

        return ResponseEntity.ok(null);


    }

    // 채널 이미지 수정
    @PutMapping("/private/channel/channelImg")
    public ResponseEntity imgUpdate(ChannelDTO dto) throws Exception {


        log.info("이미지 수정 dto " + dto);

        //    채널코드로 db가서 이미지 url 추출
        // 이미지가 안왔을때 기본 이미지 인지 , 기존 이미지 인지 구분을 해줘야함
        // 기본이미지라면  파일 삭제만 해주고 null로
        // 기존 이미지라면 아무것도 안함
        // 업로드라면 이전 이미지가 있을 경우 없을 경우 체크 해서 삭제 업로드

        // 기존 이미지 url
        String imgUrl = channelService.getUrl(dto.getChannelCode());
        log.info("기존 url " + imgUrl);
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


            // 기존 프사 사용시


            // 사진 변경시
        } else if (dto.getChange() == 1) {

            if (imgUrl != null) {
                fileDelete(imgUrl, dto.getChannelCode());
                channelService.imgUpdate(fileUpload(dto.getChannelImgUrl(), dto.getChannelCode()), dto.getChannelCode());

            } else {
                channelService.imgUpdate(fileUpload(dto.getChannelImgUrl(), dto.getChannelCode()), dto.getChannelCode());
            }

        }


        return ResponseEntity.ok(null);
    }

    // 채널 삭제
    @DeleteMapping("/private/channel/{channelCode}")
    public ResponseEntity removeChannel(@PathVariable(name = "channelCode") int channelCode) {
        channelService.removeChannel(channelCode);
        return ResponseEntity.ok(null);
    }

    // 내 채널 정보
    @GetMapping("/private/channel/{userEmail}")
    public ResponseEntity myChannel(@PathVariable(name = "userEmail") String userEmail) {
        return ResponseEntity.ok(channelService.myChannel(userEmail));
    }


    // 파일 업로드
    public String fileUpload(MultipartFile file, int channelCode) throws IllegalStateException, Exception {
        if (file == null || file.getOriginalFilename() == "") {
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
    public JPAQuery<Channel> channelJPAQuery(int page){
        Pageable pageable = PageRequest.of(page - 1, 4);




       // 채널코드랑 구독자수를 가지고 있는거
        JPAQuery<Tuple> subQuery = queryFactory
                .select(qManagement.channel.channelCode, qManagement.count())
                .from(qManagement)
                .where(qManagement.managementUserStatus.eq("sub"))
                .groupBy(qManagement.channel.channelCode);

        ;
        /*
        * SELECT
	channel_code, channel_name,
    (SELECT count(*) FROM management WHERE management_user_status = 'sub' AND channel_code = c.channel_code GROUP BY channel_code) as sub_count,
    (SELECT count(*) FROM post WHERE channel_code = c.channel_code GROUP BY channel_code) as post_count
FROM channel c
ORDER BY sub_count DESC, post_count ASC
        * */
        List<Tuple> channelQuery = queryFactory.select(qChannel.channelCode,
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
                .fetch();

      // 채널코드랑 게시물수를 가지고 있는거
        JPAQuery<Tuple> postQuery = queryFactory
                .select(qPost.channel.channelCode, qPost.count())
                .from(qPost)
                .groupBy(qPost.channel.channelCode);

       // List<Channel> chanQuery = queryFactory
         //       .selectFrom(qChannel)
           //     .leftJoin(ExpressionUtils.as(JPAExpressions.select(qManagement.channel.channelCode, qManagement.count())
             //           .from(qManagement)
               //         .where(qManagement.managementUserStatus.eq("sub"))
                //        .groupBy(qManagement.channel.channelCode), "subscribe"))

//                .leftJoin(postQuery).on(qChannel.channelCode.eq())

  //              .fetch();








        return queryFactory.selectFrom(qChannel)
                .join(qManagement).on(qManagement.channel.eq(qChannel))
                .leftJoin(qPost).on(qPost.channel.eq(qChannel))
                .groupBy(qChannel.channelCode)
                .orderBy(qManagement.count().desc())
                .orderBy(qPost.count().desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());
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
                .channelImg(c.getChannelImgUrl())
                .host(managementService.findAdmin(c.getChannelCode()).get(0))
                .channelInfo(c.getChannelInfo())
                .allPost(dtos)
                .build();
    }


    }





