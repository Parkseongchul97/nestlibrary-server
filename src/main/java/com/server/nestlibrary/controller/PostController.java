package com.server.nestlibrary.controller;

import com.server.nestlibrary.model.vo.Channel;
import com.server.nestlibrary.model.vo.Post;
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
        return ResponseEntity.ok(postService.viewPost(postCode));
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

        return ResponseEntity.ok(null);
    }





}
