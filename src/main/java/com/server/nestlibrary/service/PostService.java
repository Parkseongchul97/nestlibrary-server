package com.server.nestlibrary.service;

import com.server.nestlibrary.model.vo.Post;
import com.server.nestlibrary.repo.PostDAO;
import com.server.nestlibrary.repo.PostLikeDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PostService {

    @Autowired
    private PostDAO postDAO;

    @Autowired
    private PostLikeDAO likeDAO;

    public Post viewPost(int postCode){
        return postDAO.findById(postCode).orElse(null);
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
    public void removePost (int postCode){
        // 게시글 삭제
        postDAO.deleteById(postCode);
    }

}
