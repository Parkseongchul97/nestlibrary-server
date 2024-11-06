package com.server.nestlibrary.controller;

import com.server.nestlibrary.model.dto.CommentDTO;
import com.server.nestlibrary.model.dto.CommentListDTO;
import com.server.nestlibrary.model.dto.UserDTO;
import com.server.nestlibrary.model.vo.*;
import com.server.nestlibrary.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/*")
@CrossOrigin(origins = {"*"}, maxAge = 6000)
public class CommentController {
    @Autowired
    private CommentService commentService;
    @Autowired
    private  UserService userService;
    @Autowired
    private PostService postService;
    @Autowired
    private PushService pushService;
    @Autowired
    private ChannelService channelService;


    // 댓글 추가
    @PostMapping("/private/comment")
    public ResponseEntity addComment(@RequestBody Comment vo){
        vo.setCommentCreatedAt(LocalDateTime.now());
        Comment com = commentService.addComment(vo);
        if(com.getCommentParentsCode() == 0){ // 게시글 주인에게 알림
            Post post = postService.postCodeByPost(com.getPostCode()); // 게시글 주인
            if(post.getUserEmail()== null){
                return ResponseEntity.ok(com);
            }
            if(!userService.getLoginUser().getUserEmail().equals(post.getUserEmail()))// 내글에 내댓글 아니면
            pushService.savePush(Push.builder()
                    .pushCreatedAt(LocalDateTime.now()) // 알림 하루지나면 삭제?
                    .pushMassage("게시글 " + post.getPostTitle() + "에 새로운 댓글이 달렸습니다!")
                    .postCode(post.getPostCode()) // 주소링크용 링크용 글코드
                    .channelCode(post.getChannel().getChannelCode())
                    .userEmail(post.getUserEmail()) // 대상유저
                    .build()) ;
        }else{ // 부모 댓글 주인에게 알림
            Comment comment = commentService.findComment(com.getCommentParentsCode()); // 상위 댓글 주인
            Post post = postService.postCodeByPost(com.getPostCode());
            if(comment.getUserEmail()== null){
                return ResponseEntity.ok(com);
            }
            if(!userService.getLoginUser().getUserEmail().equals(comment.getUserEmail()))// 내댓글에 내댓글 아니면
            pushService.savePush(Push.builder()
                    .pushCreatedAt(LocalDateTime.now()) // 알림 하루지나면 삭제?
                    .pushMassage("댓글 " + comment.getCommentContent() + "에 새로운 댓글이 달렸습니다!")
                    .postCode(comment.getPostCode()) // 주소링크용
                    .channelCode(post.getChannel().getChannelCode())
                    .userEmail(comment.getUserEmail()) // 대상유저
                    .build());

        }

        return ResponseEntity.ok(com);
    }

    // 댓글 수정
    @PutMapping("/private/comment")
    public ResponseEntity updateComment(@RequestBody Comment vo){

        return  ResponseEntity.ok(commentService.updateComment(vo));
    }
    // 댓글 삭제
    @DeleteMapping("/private/comment/{commentCode}")
    public ResponseEntity removeComment(@PathVariable(name = "commentCode") int commentCode){
        
        commentService.removeComment(commentCode);
        return  ResponseEntity.ok(null);
    }

    // 게시글 댓글 전체 보여주기 <- 페이징 처리 추가(부모댓글 숫자에 따라서)
    @GetMapping("/post/{postCode}/comment")
    public ResponseEntity viewComment(@PathVariable(name = "postCode")int postCode,@RequestParam(name = "comment_page" ,defaultValue = "1")int commentPage){

        int totalCount = commentService.getTopCommentCount(postCode); // 총 상위댓글 숫자
        Paging paging = new Paging(commentPage, totalCount); // 포스트 총숫자 0에 넣기
        paging.setTotalPage(totalCount);
        paging.setOffset(paging.getLimit() * (paging.getPage()-1));
        List<Comment> allComment = commentService.getTopComment(postCode, paging);
        List<CommentDTO> response = commentList(allComment);
        CommentListDTO commentListDTO = CommentListDTO.builder()
                .commentList(response)
                .paging(paging)
                .build();
        return  ResponseEntity.ok(commentListDTO);
    }

    public  List<CommentDTO> commentList(List<Comment> comments){
        List<CommentDTO> response = new ArrayList<>();
        for(Comment c : comments ){
            List<Comment> reComment = commentService.getBottomComment(c.getCommentCode());
            List<CommentDTO> reDTO = commentList(reComment);
            CommentDTO dto = commentDetail(c);
            dto.setReCommentDTO(reDTO);
            response.add(dto);
        }
        return response;
    }

    public CommentDTO commentDetail(Comment c){
        if(c.getUserEmail() == null){
            return CommentDTO.builder()
                    .commentCode(c.getCommentCode())
                    .commentCreatedAt(c.getCommentCreatedAt())
                    .postCode(c.getPostCode())
                    .user(null)
                    .commentContent((c.getCommentContent())).build();
        }
        User vo = userService.findUser(c.getUserEmail());
            return CommentDTO.builder()
                    .commentCode(c.getCommentCode())
                    .commentCreatedAt(c.getCommentCreatedAt())
                    .postCode(c.getPostCode())
                    .user(UserDTO.builder().userEmail(vo.getUserEmail()).userNickname(vo.getUserNickname()).userImgUrl(vo.getUserImgUrl()).build())
                    .commentContent((c.getCommentContent())).build();



    }
}
