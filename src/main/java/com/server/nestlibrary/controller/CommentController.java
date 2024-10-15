package com.server.nestlibrary.controller;

import com.server.nestlibrary.model.dto.CommentDTO;
import com.server.nestlibrary.model.vo.Comment;
import com.server.nestlibrary.model.vo.User;
import com.server.nestlibrary.service.CommentService;
import com.server.nestlibrary.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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


    // 댓글 추가
    @PostMapping("/private/comment")
    public ResponseEntity addComment(@RequestBody Comment vo){
        vo.setCommentCreatedAt(LocalDateTime.now());
        return  ResponseEntity.ok(commentService.addComment(vo));
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
    public ResponseEntity viewComment(@PathVariable(name = "postCode")int postCode){
        List<Comment> allComment = commentService.getTopComment(postCode);
        List<CommentDTO> response = commentList(allComment);

        return  ResponseEntity.ok(response);
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
        User user = userService.findUser(c.getUserEmail());
        user.setUserPassword(null);
        return CommentDTO.builder()
                .commentCode(c.getCommentCode())
                .commentCreatedAt(c.getCommentCreatedAt())
                .postCode(c.getPostCode())
                .user(user)
                .commentContent((c.getCommentContent())).build();
    }
}
