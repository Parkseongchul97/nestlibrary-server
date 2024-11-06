package com.server.nestlibrary.controller;

import com.server.nestlibrary.model.dto.ChannelDTO;
import com.server.nestlibrary.model.dto.PostDTO;
import com.server.nestlibrary.model.vo.*;
import com.server.nestlibrary.service.ChannelService;
import com.server.nestlibrary.service.ManagementService;
import com.server.nestlibrary.service.PostService;
import com.server.nestlibrary.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @Autowired
    private ChannelService channelService;

    @Autowired
    private ManagementService managementService;

    @Autowired
    private UserService userService;

    private String getEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            User user = (User) auth.getPrincipal();
            return user.getUserEmail();
        }
        return null;
    }
    
    // 게시글 상세 페이지
    @GetMapping("/post/{postCode}")
    public ResponseEntity viewPost(@PathVariable(name = "postCode")int postCode){
        PostDTO dto = postService.viewPost(postCode);
        // 작성자 정보, 게시글, 좋아요 숫자 가지고있는 DTO 리턴
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/page/{postCode}")
    public ResponseEntity getPage(@PathVariable(name = "postCode")int postCode){

        // 작성자 정보, 게시글, 좋아요 숫자 가지고있는 DTO 리턴
        return ResponseEntity.ok(postService.postPage(postCode));
    }
    // 게시글 작성 - 문제 생기면 알려주세요 (2024.10.18)
    @PostMapping("/private/post")
    public ResponseEntity addPost(@RequestBody Post vo){
        Management ban = managementService.findBan(vo.getChannel().getChannelCode());
        if(ban != null){ // 내가 벤당했다면 벤정보 리턴
            return ResponseEntity.ok(ban);
        }
        // 작성자 50포인트
        Post post = postService.savePost(vo);
        // 게시글 작성하는데 작성실패(도배글)
        if(post == null){
            return ResponseEntity.ok(null);
        }
        return ResponseEntity.ok(post);
    }

    @GetMapping("/private/post/{channelCode}")
    public ResponseEntity write (@PathVariable(name = "channelCode")int channelCode){



        Channel chan = channelService.findChannel(channelCode);

        List<ChannelTag> tags = channelService.tagList(channelCode);


        for( int i=0; i<managementService.findAdmin(channelCode).size(); i++) {

            if (managementService.findAdmin(channelCode).get(i).getUserEmail().equals(getEmail())) {
                tags = channelService.tagList(channelCode);

                break;
            } else {
                tags.remove(1);
                break;
            }

        }

        ChannelDTO dto = ChannelDTO.builder()
                .channelCode(chan.getChannelCode())
                .channelName(chan.getChannelName())
                .channelInfo(chan.getChannelInfo())
                .channelImgUrl(chan.getChannelImgUrl())
                .channelCreatedAt(chan.getChannelCreatedAt())
                .channelTag(tags)
//                .host(managementService.findHost(channelCode))
                .build();



        return ResponseEntity.ok(dto);

    }


    // 게시글 수정
    @PutMapping("/private/post")
    public ResponseEntity updatePost(@RequestBody Post vo){
        log.info("게시글 내용 : "  + vo);
        Post post = postService.savePost(vo);
        log.info("포스트정보" + post);

        return ResponseEntity.ok(post);
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
        // 좋아요 받은 게시글 작성자 10포인트
        return ResponseEntity.ok( postService.like(vo));
    }
    // 좋아요 취소
    @DeleteMapping("/private/like/{postLikeCode}")
    public ResponseEntity unLike(@PathVariable(name = "postLikeCode") int postLikeCode){
        postService.unLike(postLikeCode);
        // 좋아요 받았던 게시글 작성자 -10포인트(포인트 있으면)
        return ResponseEntity.ok(null);
    }

    @GetMapping("/post/user/{userEmail}")
    public  ResponseEntity userPost(@PathVariable(name = "userEmail") String userEmail){

        return ResponseEntity.ok(postService.emailByPost(userEmail));
    }


    @GetMapping("/user/favorite/{userEmail}")
    public ResponseEntity favoriteChannel(@PathVariable(name = "userEmail")String userEmail){



        return  ResponseEntity.ok(channelService.favoriteChannel(userEmail));

    }










}
