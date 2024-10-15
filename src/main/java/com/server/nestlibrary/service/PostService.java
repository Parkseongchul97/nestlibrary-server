package com.server.nestlibrary.service;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.server.nestlibrary.model.dto.CommentDTO;
import com.server.nestlibrary.model.dto.PostDTO;
import com.server.nestlibrary.model.dto.UserDTO;
import com.server.nestlibrary.model.vo.*;
import com.server.nestlibrary.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PostService {

    @Autowired
    private PostDAO postDAO;

    @Autowired
    private PostLikeDAO likeDAO;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private CommentService commentService;

    @Autowired
    private ChannelTagDAO tagDAO;

    @Autowired
    private JPAQueryFactory queryFactory;

    private final QPostLike qPostLike = QPostLike.postLike;
    private final QPost qPost = QPost.post;

    // 해당 채널의 전체 글
    public List<PostDTO> channelCodeByAllPost(int channelCode){
        List<PostDTO> dtoList = new ArrayList<>();
        List<Post> voList =  queryFactory.selectFrom(qPost)
                .where(qPost.channelCode.eq(channelCode))
                .orderBy(qPost.postCreatedAt.desc()) // 최신순으로
                .limit(10) // 일단 10개만 빼보기
                .fetch();
        for(Post p : voList){
            User userVo = userDAO.findById(p.getUserEmail()).get();
            dtoList.add(PostDTO.builder()
                    .postCreatedAt(p.getPostCreatedAt())
                    .postTitle(p.getPostTitle())
                    .postContent(p.getPostContent())
                    .postCode(p.getPostCode())
                    .channelTag(tagDAO.findById(p.getChannelTagCode()).get())
                    .channelCode(p.getChannelCode())
                    .postViews(p.getPostViews())
                    .user(UserDTO.builder().userNickname(userVo.getUserNickname())
                                    .userImg(userVo.getUserImgUrl())
                                    .userEmail(userVo.getUserEmail()).build())
                    .likeCount(queryFactory.selectFrom(qPostLike).where(qPostLike.postCode.eq(p.getPostCode())).fetch().size())
                    .commentCount(commentService.commentCount(p.getPostCode()))
                    .build());

        }
        if(dtoList.size() == 0){
            return null;
        }
        return dtoList;

    }
    // 채널 태그별 게시글
    public List<PostDTO> channelTagCodeByAllPost(int channelTagCode){
        List<PostDTO> dtoList = new ArrayList<>();
        List<Post> voList =  queryFactory.selectFrom(qPost)
                .where(qPost.channelTagCode.eq(channelTagCode))
                .orderBy(qPost.postCreatedAt.desc()) // 최신순으로
                .limit(10) // 일단 10개만 빼보기
                .fetch();
        for(Post p : voList){
            User userVo = userDAO.findById(p.getUserEmail()).get();
            dtoList.add(PostDTO.builder()
                    .postCreatedAt(p.getPostCreatedAt())
                    .postTitle(p.getPostTitle())
                    .postContent(p.getPostContent())
                    .postCode(p.getPostCode())
                    .channelTag(tagDAO.findById(p.getChannelTagCode()).get())
                    .channelCode(p.getChannelCode())
                    .postViews(p.getPostViews())
                    .user(UserDTO.builder().userNickname(userVo.getUserNickname())
                            .userImg(userVo.getUserImgUrl())
                            .userEmail(userVo.getUserEmail()).build())
                    .likeCount(queryFactory.selectFrom(qPostLike).where(qPostLike.postCode.eq(p.getPostCode())).fetch().size())
                    .commentCount(commentService.commentCount(p.getPostCode()))
                    .build());

        }
        if(dtoList.size() == 0){
            return null;
        }
        return dtoList;

    }

    // 게시글 조회
    @Transactional
    public PostDTO viewPost(int postCode){
        // 조회수 증가
        queryFactory.update(qPost)
                .set(qPost.postViews, qPost.postViews.add(1))
                .where(qPost.postCode.eq(postCode))
                .execute();
        Post vo = postDAO.findById(postCode).get();
        // 게시글 좋아요 숫자 확인
        User user = userDAO.findById(vo.getUserEmail()).get();
        PostDTO dto = PostDTO.builder()
                .postCreatedAt(vo.getPostCreatedAt())
                .postTitle(vo.getPostTitle())
                .postContent(vo.getPostContent())
                .postCode(postCode)
                .channelTag(tagDAO.findById(vo.getChannelTagCode()).get())
                .channelCode(vo.getChannelCode())
                .postViews(vo.getPostViews())
                .user(UserDTO.builder().userNickname(user.getUserNickname())
                        .userImg(user.getUserImgUrl())
                        .userEmail(user.getUserEmail()).build())
                .likeCount(queryFactory.selectFrom(qPostLike).where(qPostLike.postCode.eq(postCode)).fetch().size())
                .commentCount(commentService.commentCount(postCode))
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
             post.setChannelTagCode(vo.getChannelTagCode());
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





}
