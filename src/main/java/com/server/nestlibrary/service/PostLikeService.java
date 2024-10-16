package com.server.nestlibrary.service;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.server.nestlibrary.model.vo.PostLike;
import com.server.nestlibrary.model.vo.QPostLike;
import com.server.nestlibrary.model.vo.User;
import com.server.nestlibrary.repo.PostLikeDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostLikeService {

    @Autowired
    private JPAQueryFactory queryFactory;


    @Autowired
    private PostLikeDAO postLikeDAO;

    @Autowired
    private ManagementService managementService;

    private final QPostLike qPostLike = QPostLike.postLike;

    public PostLike state(int postCode){
        String email = getEmail();
        List<PostLike> list = queryFactory.selectFrom(qPostLike).where(qPostLike.postCode.eq(postCode))
                .where(qPostLike.userEmail.eq(email))
                .fetch();

        if(list.size()==0) return null;
        return list.get(0);
    }

    public PostLike like(PostLike vo){
        return postLikeDAO.save(vo);
    }

    public void unLike(int postLikeCode){
        postLikeDAO.deleteById(postLikeCode);
    }

    public String getEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            User user = (User) auth.getPrincipal();
            return user.getUserEmail();
        }
        return null;
    }
}
