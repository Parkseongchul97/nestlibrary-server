package com.server.nestlibrary.controller;

import com.server.nestlibrary.model.vo.PostLike;
import com.server.nestlibrary.service.PostLikeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/*")
@CrossOrigin(origins = {"*"}, maxAge = 6000)
public class PostLikeController {
    @Autowired
    private PostLikeService postLikeService;

    @GetMapping("/private/state/{postCode}")
    public ResponseEntity likeState(@PathVariable(name = "postCode") int postCode){
        PostLike like = postLikeService.state(postCode);

        return ResponseEntity.ok(like);
    }

    @GetMapping("/private/like")
    public ResponseEntity like(@RequestBody PostLike vo){

        return ResponseEntity.ok(postLikeService.like(vo));
    }

    @GetMapping("/private/like/{postLikeCode}")
    public ResponseEntity UnLike(@PathVariable(name = "postLikeCode") int postLikeCode){
        postLikeService.unLike(postLikeCode);
        return ResponseEntity.ok(null);
    }
}
