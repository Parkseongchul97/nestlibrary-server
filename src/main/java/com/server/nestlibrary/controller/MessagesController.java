package com.server.nestlibrary.controller;

import com.server.nestlibrary.model.vo.Comment;
import com.server.nestlibrary.model.vo.Messages;
import com.server.nestlibrary.service.MessagesService;
import com.server.nestlibrary.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/api/*")
@CrossOrigin(origins = {"*"}, maxAge = 6000)
public class MessagesController {
    @Autowired
    private MessagesService messagesService;
    @Autowired
    private UserService userService;
    
    // 수신 메일 조회
    @GetMapping("/private/to/messages")
    public ResponseEntity findToMessages(@RequestBody Messages vo){
        // 내가 받은 모든 쪽지 가져오기
        return   ResponseEntity.ok(null);
    }

    // 발신 메일 조회
    @GetMapping("/private/from/messages")
    public ResponseEntity findFromMessages(@RequestBody Messages vo){
        // 내가 보낸 모든 쪽지 가져오기
        return   ResponseEntity.ok(null);
    }
    // 단일 메일 조회
    @GetMapping("/private/messages/{messagesCode}")
    public ResponseEntity findMessages(@PathVariable(name = "messagesCode") int messagesCode){
        // 해당 쪽지의 정보 자세히 뿌리기
        // 수신자인지 발신자인지 확인후 수신자가 확인하면 조회여부 변경
        return   ResponseEntity.ok(null);
    }

    @GetMapping("/private/user")
    public ResponseEntity findUser(@RequestParam(name = "userNickname") String userNickname){
        // 해당 닉네임 포함된 유저 리스트 리턴
        if(userNickname == "")return ResponseEntity.ok(null);
        return   ResponseEntity.ok(userService.findByNicknameUserList(userNickname));
    }
    
    // 쪽지 보내기
    @PostMapping("/private/messages")
    public ResponseEntity addMessages(@RequestBody Messages vo){
        // 수신자 정보를(닉네임을 받아서) 검색후 선택 ? 입력후 DB 저장
        return   ResponseEntity.ok(null);
    }
    // 쪽지 삭제
    @DeleteMapping("/private/messages/{messagesCode}")
    public ResponseEntity removeMessages(@PathVariable(name = "messagesCode") int messagesCode){
        // 발신자인지 수신자인지 확인해서 messages_is_delete 변경, 만약 이미 messages_is_delete 가 반대에서 삭제한 경우
        // delete 처리
        return   ResponseEntity.ok(null);
    }




}
