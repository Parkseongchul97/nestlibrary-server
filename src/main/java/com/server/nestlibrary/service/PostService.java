package com.server.nestlibrary.service;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.server.nestlibrary.model.dto.BoardDTO;
import com.server.nestlibrary.model.dto.PostDTO;
import com.server.nestlibrary.model.dto.UserDTO;
import com.server.nestlibrary.model.vo.*;
import com.server.nestlibrary.repo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Slf4j
@Service
public class PostService {

    @Autowired
    private PostDAO postDAO;

    @Autowired
    private PostLikeDAO likeDAO;

    @Autowired
    private UserDAO userDAO;
    @Lazy
    @Autowired
    private CommentService commentService;

    @Autowired
    private ChannelTagDAO tagDAO;

    @Autowired
    private  ChannelDAO channelDAO;

    @Autowired
    private JPAQueryFactory queryFactory;

    private final QPostLike qPostLike = QPostLike.postLike;
    private final QPost qPost = QPost.post;
    private final QUser qUser = QUser.user;
    private final QComment qComment = QComment.comment;

    public Post postCodeByPost(int postCode){
        return postDAO.findById(postCode).orElse(null);
    }
    public int allPostCount(int channelCode, String target, String keyword){
        JPAQuery<Post> query = queryFactory.selectFrom(qPost)
                .join(qUser).on(qPost.userEmail.eq(qUser.userEmail))
                .where(qPost.channel.channelCode.eq(channelCode));
        if (target != null && !target.equals("") && keyword != null && !keyword.equals("")) {
            if(target.equals("title")){ // 제목이 포함된게시글
                query.where(qPost.postTitle.containsIgnoreCase(keyword));
            }else if(target.equals("content")){// 내용이 포함된게시글
                query.where(qPost.postContent.containsIgnoreCase(keyword));
            }else if(target.equals("user")){ // 작성자가
                query.where(qUser.userNickname.containsIgnoreCase(keyword));

            }
        }
        return query.fetch().size();

    }
    public int tagPostCount(int channelTagCode,String target, String keyword){
        JPAQuery<Post> query = queryFactory.selectFrom(qPost)
                .join(qUser).on(qPost.userEmail.eq(qUser.userEmail))
                .where(qPost.channelTag.channelTagCode.eq(channelTagCode));
        if (target != null && !target.equals("") && keyword != null && !keyword.equals("")) {
            if(target.equals("title")){ // 제목이 포함된게시글
                query.where(qPost.postTitle.containsIgnoreCase(keyword));
            }else if(target.equals("content")){// 내용이 포함된게시글
                query.where(qPost.postContent.containsIgnoreCase(keyword));
            }else if(target.equals("user")){ // 작성자가
                query.where(qUser.userNickname.containsIgnoreCase(keyword));
            }
        }
        return query.fetch().size();
    }
    // 해당 채널의 전체 글
    public List<PostDTO> channelCodeByAllPost(int channelCode, Paging paging, String target, String keyword){
        List<PostDTO> dtoList = new ArrayList<>();
        JPAQuery<Post> query = queryFactory.selectFrom(qPost)
                .join(qUser).on(qPost.userEmail.eq(qUser.userEmail))
                    .where(qPost.channel.channelCode.eq(channelCode));
        if (target != null && !target.equals("") && keyword != null && !keyword.equals("")) {
            if(target.equals("title")){ // 제목이 포함된게시글
                query.where(qPost.postTitle.containsIgnoreCase(keyword));
            }else if(target.equals("content")){// 내용이 포함된게시글
                query.where(qPost.postContent.containsIgnoreCase(keyword));
            }else if(target.equals("user")){ // 작성자가
                query.where(qUser.userNickname.containsIgnoreCase(keyword));
            }
        }

        List<Post> voList = new ArrayList<>();
        if(paging != null ) {
           voList = query
                    .orderBy(qPost.postCreatedAt.desc()) // 최신순으로
                    .offset(paging.getOffset()) //
                    .limit(paging.getLimit()) //10개씩
                    .fetch();

        }else {
            voList = query
                    .orderBy(qPost.postCreatedAt.desc()) // 최신순으로


                    .fetch();


        }

        for(Post p : voList){
            User userVo = userDAO.findById(p.getUserEmail()).get();
            // 문제 생기면 알려주세요 (2024.10.18)
            dtoList.add(PostDTO.builder()
                    .postCreatedAt(p.getPostCreatedAt())
                    .postTitle(p.getPostTitle())
                    .postContent(p.getPostContent())
                    .postCode(p.getPostCode())
                            .channelTag(tagDAO.findById(p.getChannelTag().getChannelTagCode()).get())
                            .channelCode(p.getChannel().getChannelCode())
                    .postViews(p.getPostViews())
                    .user(UserDTO.builder().userNickname(userVo.getUserNickname())
                                    .userImgUrl(userVo.getUserImgUrl())
                                    .userEmail(userVo.getUserEmail()).build())
                    .likeCount(queryFactory.selectFrom(qPostLike).where(qPostLike.postCode.eq(p.getPostCode())).fetch().size())
                    .commentCount(commentService.commentCount(p.getPostCode()))
                    .bestPoint(postViewCount(p.getPostCode()) + (postLikeCount(p.getPostCode())*5) + (postCommentCount(p.getPostCode())*2))
                    .build());

        }
        if(dtoList.size() == 0){
            return null;
        }
        return dtoList;

    }

    // 해당 채널의 인기 글

    public BoardDTO channelCodeByBestPost(int channelCode, int page, String target, String keyword) {
        List<Post> voList = bestPostVoList(channelCode,target,keyword);
        int totalCount = voList.size();
        Paging paging = new Paging(page, totalCount); // 포스트 총숫자 0에 넣기
        paging.setTotalPage(totalCount);
        paging.setOffset(paging.getLimit() * (paging.getPage() - 1));
        log.info("인기페이징 상태 : " + paging);
        int extraCount = paging.getTotalPage() % paging.getPageSize(); // 나머지 숫자
        List<Post> pagingVoList = new ArrayList<>();
        if(paging.getPage() == paging.getEndPage()){ // 마지막 페이지이면서
            if(extraCount != 0){ // 딱떨어지지 않으면 마지막 페이지가
                for(int i = paging.getOffset(); i < paging.getOffset()+extraCount; i++){
                    pagingVoList.add(voList.get(i));
                }
            }else{
                for(int i = paging.getOffset(); i < paging.getOffset()+paging.getPageSize(); i++){
                    pagingVoList.add(voList.get(i));
                }
            }
        }else{
            for(int i = paging.getOffset(); i < paging.getOffset()+paging.getPageSize(); i++){
                pagingVoList.add(voList.get(i));
            }
        }
        List<PostDTO> dtoList = new ArrayList<>();
        for(Post p : pagingVoList){
            User userVo = userDAO.findById(p.getUserEmail()).get();
            dtoList.add(PostDTO.builder()
                    .postCreatedAt(p.getPostCreatedAt())
                    .postTitle(p.getPostTitle())
                    .postContent(p.getPostContent())
                    .postCode(p.getPostCode())
                    .channelTag(tagDAO.findById(p.getChannelTag().getChannelTagCode()).get())
                    .channelCode(p.getChannel().getChannelCode())
                    .postViews(p.getPostViews())
                    .user(UserDTO.builder().userNickname(userVo.getUserNickname())
                            .userImgUrl(userVo.getUserImgUrl())
                            .userEmail(userVo.getUserEmail()).build())
                    .likeCount(queryFactory.selectFrom(qPostLike).where(qPostLike.postCode.eq(p.getPostCode())).fetch().size())
                    .commentCount(commentService.commentCount(p.getPostCode()))
                    .bestPoint(postViewCount(p.getPostCode()) + (postLikeCount(p.getPostCode())*5) + (postCommentCount(p.getPostCode())*2))
                    .build());
        }
        if(dtoList.size() == 0) dtoList = null;
        BoardDTO postBoard = BoardDTO.builder().postList(dtoList).paging(paging).build();
        return postBoard;
    }
    public List<Post> bestPostVoList(int channelCode,  String target, String keyword){
        List<PostDTO> dtoList = new ArrayList<>();
        BooleanBuilder builder = new BooleanBuilder();
        JPAQuery<Post> query = queryFactory.selectFrom(qPost)
                .join(qUser).on(qPost.userEmail.eq(qUser.userEmail))
                .where(qPost.channel.channelCode.eq(channelCode));
        // 검색 조건 추가
        if (target != null && !target.isEmpty() && keyword != null && !keyword.isEmpty()) {
            if (target.equals("title")) {
                query.where(qPost.postTitle.containsIgnoreCase(keyword));
            } else if (target.equals("content")) {
                query.where(qPost.postContent.containsIgnoreCase(keyword));
            } else if (target.equals("user")) {
                query.where(qUser.userNickname.containsIgnoreCase(keyword));
            }
        }
        List<Post> voList = query.fetch();
        List<Post> bestList = new ArrayList<>();
        for(Post p : voList){ // 점수 계산
            int bestPoint = postViewCount(p.getPostCode()) + (postLikeCount(p.getPostCode())*5) + (postCommentCount(p.getPostCode())*2);
            if(bestPoint > 50){
                bestList.add(p);
            }
        }
        return bestList;
    }

    public BoardDTO channelTagByBestPost(int channelTagCode, int page, String target, String keyword) {
        List<Post> voList = bestTagPostVoList(channelTagCode,target,keyword);
        int totalCount = voList.size();
        Paging paging = new Paging(page, totalCount); // 포스트 총숫자 0에 넣기
        paging.setTotalPage(totalCount);
        paging.setOffset(paging.getLimit() * (paging.getPage() - 1));

        int extraCount = paging.getTotalPage() % paging.getPageSize(); // 나머지 숫자
        List<Post> pagingVoList = new ArrayList<>();
        if(voList.size() != 0){
        if(paging.getPage() == paging.getEndPage()){ // 마지막 페이지이면서
            if(extraCount != 0){ // 딱떨어지지 않으면 마지막 페이지가
                for(int i = paging.getOffset(); i < paging.getOffset()+extraCount; i++){
                    pagingVoList.add(voList.get(i));
                }
            }else{
                for(int i = paging.getOffset(); i < paging.getOffset()+paging.getPageSize(); i++){
                    pagingVoList.add(voList.get(i));
                }
            }
        }else{
            for(int i = paging.getOffset(); i < paging.getOffset()+paging.getPageSize(); i++){
                pagingVoList.add(voList.get(i));
            }
        }
        }
        List<PostDTO> dtoList = new ArrayList<>();
        for(Post p : pagingVoList){
            User userVo = userDAO.findById(p.getUserEmail()).get();
            dtoList.add(PostDTO.builder()
                    .postCreatedAt(p.getPostCreatedAt())
                    .postTitle(p.getPostTitle())
                    .postContent(p.getPostContent())
                    .postCode(p.getPostCode())
                    .channelTag(tagDAO.findById(p.getChannelTag().getChannelTagCode()).get())
                    .channelCode(p.getChannel().getChannelCode())
                    .postViews(p.getPostViews())
                    .user(UserDTO.builder().userNickname(userVo.getUserNickname())
                            .userImgUrl(userVo.getUserImgUrl())
                            .userEmail(userVo.getUserEmail()).build())
                    .likeCount(queryFactory.selectFrom(qPostLike).where(qPostLike.postCode.eq(p.getPostCode())).fetch().size())
                    .commentCount(commentService.commentCount(p.getPostCode()))
                            .bestPoint(postViewCount(p.getPostCode()) + (postLikeCount(p.getPostCode())*5) + (postCommentCount(p.getPostCode())*2))
                    .build());
        }
        if(dtoList.size() == 0) dtoList = null;
        BoardDTO postBoard = BoardDTO.builder().postList(dtoList).paging(paging).build();
        return postBoard;
    }
    public List<Post> bestTagPostVoList(int channelTagCode, String target, String keyword){
        List<PostDTO> dtoList = new ArrayList<>();
        BooleanBuilder builder = new BooleanBuilder();
        JPAQuery<Post> query = queryFactory.selectFrom(qPost)
                .join(qUser).on(qPost.userEmail.eq(qUser.userEmail))
                .where(qPost.channelTag.channelTagCode.eq(channelTagCode));
        // 검색 조건 추가
        if (target != null && !target.isEmpty() && keyword != null && !keyword.isEmpty()) {
            if (target.equals("title")) {
                query.where(qPost.postTitle.containsIgnoreCase(keyword));
            } else if (target.equals("content")) {
                query.where(qPost.postContent.containsIgnoreCase(keyword));
            } else if (target.equals("user")) {
                query.where(qUser.userNickname.containsIgnoreCase(keyword));
            }
        }
        List<Post> voList = query.fetch();
        List<Post> bestList = new ArrayList<>();
        for(Post p : voList){ // 점수 계산
            int bestPoint = postViewCount(p.getPostCode()) + (postLikeCount(p.getPostCode())*5) + (postCommentCount(p.getPostCode())*2);
            if(bestPoint > 50){
                bestList.add(p);
            }
        }
        return bestList;
    }


    // 알림 포스트 페이지 알아내는 메서드
    public int postPage(int postCode){
        Post vo = findByPostCode(postCode);
        // 삭제된 게시글이라면
        if(vo == null)return -1;
        List<Post> list =  queryFactory
                .selectFrom(qPost)
                .where(qPost.channel.channelCode.eq(vo.getChannel().getChannelCode()))
                .orderBy(qPost.postCreatedAt.desc()).fetch();
        int index = list.indexOf(vo) +1;
        // 인덱스 +1 = 해당 채널의 게시글중 N번째 게시글 /10 *10
        if(index == 0){
            // 몬가 문제생긴경우
            return -1;
        }
        ;
        // 전체(25) % 10 = 0이면  전체 / 10 아니면 (전체 /10) + 1
        int pageNum = (int) Math.ceil(index / 10 );
        if(index % 10 != 0)pageNum++;
        return pageNum;
    }





    public int postCommentCount(int postCode){
        return queryFactory.selectFrom(qComment).where(qComment.postCode.eq(postCode)).fetch().size();
    }
    public int postViewCount(int postCode){
        List<Integer> list =queryFactory.select(QPost.post.postViews).from(qPost).where(qPost.postCode.eq(postCode)).fetch();
        return list.get(0);
    }
    public int postLikeCount(int postCode){
        return queryFactory.selectFrom(qPostLike).where(qPostLike.postCode.eq(postCode)).fetch().size();
    }




    // 채널 태그별 게시글
    public List<PostDTO> channelTagCodeByAllPost(int channelTagCode, Paging paging, String target, String keyword){
        // 문제 생기면 알려주세요 (2024.10.18)
        List<PostDTO> dtoList = new ArrayList<>();
        JPAQuery<Post> query = queryFactory.selectFrom(qPost)
                .join(qUser).on(qPost.userEmail.eq(qUser.userEmail))
                .where(qPost.channelTag.channelTagCode.eq(channelTagCode));
        if (target != null && !target.equals("") && keyword != null && !keyword.equals("")) {
            if(target.equals("title")){ // 제목이 포함된게시글
                query.where(qPost.postTitle.containsIgnoreCase(keyword));
            }else if(target.equals("content")){// 내용이 포함된게시글
                query.where(qPost.postContent.containsIgnoreCase(keyword));
            }else if(target.equals("user")){ // 작성자가
                query.where(qUser.userNickname.containsIgnoreCase(keyword));
            }
        }
        List<Post> voList =  query
                .orderBy(qPost.postCreatedAt.desc()) // 최신순으로
                .offset(paging.getOffset()) //
                .limit(paging.getLimit())
                .fetch();
        // 문제 생기면 알려주세요 (2024.10.18)
        for(Post p : voList){
            User userVo = userDAO.findById(p.getUserEmail()).get();
            dtoList.add(PostDTO.builder()
                    .postCreatedAt(p.getPostCreatedAt())
                    .postTitle(p.getPostTitle())
                    .postContent(p.getPostContent())
                    .postCode(p.getPostCode())
                            .channelTag(tagDAO.findById(p.getChannelTag().getChannelTagCode()).get())
                            .channelCode(p.getChannel().getChannelCode())
                    .postViews(p.getPostViews())
                    .user(UserDTO.builder().userNickname(userVo.getUserNickname())
                            .userImgUrl(userVo.getUserImgUrl())
                            .userEmail(userVo.getUserEmail()).build())
                    .likeCount(queryFactory.selectFrom(qPostLike).where(qPostLike.postCode.eq(p.getPostCode())).fetch().size())
                    .commentCount(commentService.commentCount(p.getPostCode()))
                            .bestPoint(postViewCount(p.getPostCode()) + (postLikeCount(p.getPostCode())*5) + (postCommentCount(p.getPostCode())*2))
                    .build());

        }
        if(dtoList.size() == 0){
            return null;
        }
        return dtoList;

    }
    public Post findByPostCode(int postCode){

        return postDAO.findById(postCode).orElse(null);
    }

    // 게시글 조회
    @Transactional
    public PostDTO viewPost(int postCode){
        // 조회수 증가
        queryFactory.update(qPost)
                .set(qPost.postViews, qPost.postViews.add(1))
                .where(qPost.postCode.eq(postCode))
                .execute();
        Post vo = postDAO.findById(postCode).orElse(null);
        if(vo == null)return null;
        // 게시글 좋아요 숫자 확인
        // 문제 생기면 알려주세요 (2024.10.18)
        User user = userDAO.findById(vo.getUserEmail()).get();
        PostDTO dto = PostDTO.builder()
                .postCreatedAt(vo.getPostCreatedAt())
                .postTitle(vo.getPostTitle())
                .postContent(vo.getPostContent())
                .postCode(postCode)
                .channelTag(tagDAO.findById(vo.getChannelTag().getChannelTagCode()).get())
                .channelCode(vo.getChannel().getChannelCode())
                .postViews(vo.getPostViews())
                .user(UserDTO.builder().userNickname(user.getUserNickname())
                        .userImgUrl(user.getUserImgUrl())
                        .userEmail(user.getUserEmail()).build())
                .likeCount(queryFactory.selectFrom(qPostLike).where(qPostLike.postCode.eq(postCode)).fetch().size())
                .commentCount(commentService.commentCount(postCode))
                .bestPoint(postViewCount(vo.getPostCode()) + (postLikeCount(vo.getPostCode())*5) + (postCommentCount(vo.getPostCode())*2))
                .build();
       // 작성자, 게시글 , 좋아요 숫자 리턴

        return dto;
    }
    // 게시글 작성,수정
    public Post savePost (Post vo){
        if(vo.getPostCode() == 0){
            // 수정이아니라 작성의 경우에만
            // 추가조건 : 도배방지 같은 제목 내용 나우랑 최근글 페이징 해오는거 비교
            vo.setPostCreatedAt(LocalDateTime.now());
            User user = userDAO.findById(getEmail()).get();
            user.setUserPoint(user.getUserPoint()+50);
            // 게시글 작성시 50포인트 추가
            userDAO.save(user);
            return postDAO.save(vo);
        } else{
            // 수정일땐 시간 원래 시간 다시 넣기
             Post post =  postDAO.findById(vo.getPostCode()).get();
             post.setPostTitle(vo.getPostTitle());
             // 문제 생기면 알려주세요 (2024.10.18)
            post.setChannelTag(ChannelTag.builder()
                            .channelTagCode(vo.getChannelTag().getChannelTagCode())
                    .build());
             post.setPostContent(vo.getPostContent());

            return postDAO.save(post);
        }

    }
    // 게시글 삭제
    public void removePost (int postCode){
        // 삭제시 포인트감소?
        postDAO.deleteById(postCode);
    }
    // 게시글 좋아요
    public PostLike like(PostLike vo){
        // 좋아요 숫자가 일정 달성시 해당 post 조회하고 조회수, 좋아요수 조건 충족하면 인기글로 < 추가
        PostLike like = likeDAO.save(vo);
        // 좋아요 받은 게시글 작성자 포인트 + 10
        Post post = postDAO.findById(like.getPostCode()).get();
        User postAuthor= userDAO.findById(post.getUserEmail()).get();
        postAuthor.setUserPoint(postAuthor.getUserPoint()+10);
        userDAO.save(postAuthor);
        return like;
    }
    // 좋아요 취소
    public void unLike(int postLikeCode){
        // 좋아요 취소되면 다시 포인트 -10
        Post post = postDAO.findById(likeDAO.findById(postLikeCode).get().getPostCode()).get();
        User postAuthor= userDAO.findById(post.getUserEmail()).get();
        // 작성자의 보유 포인트가 10보다 크면
        if(postAuthor.getUserPoint() > 10){
            postAuthor.setUserPoint(postAuthor.getUserPoint()-10);
            userDAO.save(postAuthor); // 포인트 감소
        }
        likeDAO.deleteById(postLikeCode); // 좋아요 취소
    }
    // 로그인한 사용자의 좋아요 여부
    public PostLike findLike(int postCode){
        List<PostLike> list = queryFactory.selectFrom(qPostLike)
                .where(qPostLike.postCode.eq(postCode))
                .where(qPostLike.userEmail.eq(getEmail())).fetch();
        if(list.size() > 0){
            return list.get(0);
        }
        return null;
    }

    private String getEmail(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth!= null && auth.isAuthenticated()){
            User user = (User) auth.getPrincipal();
            return user.getUserEmail();
        }
        return null;
    }

    // 포스트 코드로 채널 찾기
    public int postCodeByChannel(int postCode){
     Post vo =   postDAO.findById(postCode).get();
     int channelCode = vo.getChannel().getChannelCode();
        return  channelCode;

    }

    //해당 채널 게시글 수
    public int userPostCount(int channelCode, String userEmail){

        return postDAO.postCount(channelCode,userEmail);
    }

    //해당 유저의 최신글 10개
    public List<PostDTO> emailByPost(String userEmail){
     List<Post> postList =   postDAO.emailByPost(userEmail);
        List<PostDTO> dtoList = new ArrayList<>();
        User user = userDAO.findById(userEmail).orElse(null);
        if(postList.size() == 0){
            return null;
        } else {
            for(int i=0; i<postList.size(); i++){

                PostDTO dto = PostDTO.builder()
                        .postCreatedAt(postList.get(i).getPostCreatedAt())
                        .postTitle(postList.get(i).getPostTitle())
                        .postContent(postList.get(i).getPostContent())
                        .postCode(postList.get(i).getPostCode())
                        .channelTag(tagDAO.findById(postList.get(i).getChannelTag().getChannelTagCode()).get())
                        .channelCode(postList.get(i).getChannel().getChannelCode())
                        .postViews(postList.get(i).getPostViews())
                        .channelName(channelDAO.findById(postList.get(i).getChannel().getChannelCode()).get().getChannelName())
                        .user(UserDTO.builder().userNickname(user.getUserNickname())
                                .userImgUrl(user.getUserImgUrl())
                                .userEmail(user.getUserEmail()).build())
                        .likeCount(queryFactory.selectFrom(qPostLike).where(qPostLike.postCode.eq(postList.get(i).getPostCode())).fetch().size())
                        .commentCount(commentService.commentCount(postList.get(i).getPostCode()))
                        .bestPoint(postViewCount(postList.get(i).getPostCode()) + (postLikeCount(postList.get(i).getPostCode())*5) + (postCommentCount(postList.get(i).getPostCode())*2))
                        .build();
                dtoList.add(dto);
            }
            return dtoList;
        }



    }

    public List<Integer> findChannelCode(String userEmail){

       return postDAO.findChannelCode(userEmail);
    }




}
