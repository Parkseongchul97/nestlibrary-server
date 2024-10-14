package com.server.nestlibrary.controller;

import com.server.nestlibrary.model.dto.PostDTO;
import com.server.nestlibrary.model.vo.Channel;
import com.server.nestlibrary.model.vo.Post;
import com.server.nestlibrary.model.vo.PostLike;
import com.server.nestlibrary.service.PostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/*")
@CrossOrigin(origins = {"*"}, maxAge = 6000)
public class PostController {

    @Autowired
    private PostService postService;
    
    // 게시글 상세 페이지
    @GetMapping("/post/{postCode}")
    public ResponseEntity viewPost(@PathVariable(name = "postCode")int postCode){

        PostDTO dto =postService.viewPost(postCode);
        return ResponseEntity.ok(dto);
    }
    // 게시글 작성
    @PostMapping("/private/post")
    public ResponseEntity addPost(@RequestBody Post vo){
        log.info("게시글 내용 : "  + vo);
        Post post = postService.savePost(vo);
        return ResponseEntity.ok(post);
    }
    // 게시글 수정
    @PutMapping("/private/post")
    public ResponseEntity updatePost(@RequestBody Post vo){
        log.info("게시글 내용 : "  + vo);
        Post post = postService.savePost(vo);
        return ResponseEntity.ok(vo);
    }
    // 게시글 삭제
    @DeleteMapping("/private/post/{postCode}")
    public ResponseEntity removePost(@PathVariable(name = "postCode") int postCode){
        postService.removePost(postCode);
        return ResponseEntity.ok(null);
    }
    // 로그인 회원의 좋아요 여부
    @GetMapping("/private/like/{postCode}")
    public  ResponseEntity findLike(@PathVariable(name = "postCode") int postCode){
        // 좋아요 X 면 null 리턴
        PostLike postLike = postService.findLike(postCode);
        return ResponseEntity.ok(postLike);
    }
    // 게시글 좋아요
    @PostMapping("/private/like")
    public ResponseEntity like(@RequestBody PostLike vo){
        postService.like(vo);
        return ResponseEntity.ok( postService.like(vo));
    }
    // 좋아요 취소
    @DeleteMapping("/private/like/{postLikeCode}")
    public ResponseEntity unLike(@PathVariable(name = "postLikeCode") int postLikeCode){
        postService.unLike(postLikeCode);
        return ResponseEntity.ok(null);
    }




}
