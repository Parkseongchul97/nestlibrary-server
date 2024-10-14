package com.server.nestlibrary.service;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.server.nestlibrary.model.dto.PostDTO;
import com.server.nestlibrary.model.vo.*;
import com.server.nestlibrary.repo.PostDAO;
import com.server.nestlibrary.repo.PostLikeDAO;
import com.server.nestlibrary.repo.UserDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    private JPAQueryFactory queryFactory;

    private final QPostLike qPostLike = QPostLike.postLike;
    private final QPost qPost = QPost.post;
    private final QUser qUser = QUser.user;
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
        user.setUserPassword(null);
        PostDTO dto = PostDTO.builder()
                .postCreatedAt(vo.getPostCreatedAt())
                .postTitle(vo.getPostTitle())
                .postContent(vo.getPostContent())
                .postCode(postCode)
                .channelTagCode(vo.getChannelTagCode())
                .channelCode(vo.getChannelCode())
                .postViews(vo.getPostViews())
                .user(user)
                .likeCount(queryFactory.selectFrom(qPostLike).where(qPostLike.postCode.eq(postCode)).fetch().size())
                .build();
       // 작성자, 게시글 , 좋아요 숫자 리턴
        
        return dto;
    }
    // 게시글 작성,수정
    public Post savePost (Post vo){
        if(vo.getPostCode() == 0){
            // 수정이아니라 작성의 경우에만
            vo.setPostCreatedAt(LocalDateTime.now());
        } else{
            // 수정일땐 시간 원래 시간 다시 넣기
        vo.setPostCreatedAt(viewPost(vo.getPostCode()).getPostCreatedAt());
        }
        return postDAO.save(vo);
    }
    // 게시글 삭제
    public void removePost (int postCode){

        postDAO.deleteById(postCode);
    }
    // 게시글 좋아요
    public PostLike like(PostLike vo){
        // 좋아요 숫자가 일정 달성시 해당 post 조회하고 조회수, 좋아요수 조건 충족하면 인기글로 < 추가
        return likeDAO.save(vo);
    }
    // 좋아요 취소
    public void unLike(int postLikeCode){
        likeDAO.deleteById(postLikeCode);
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
