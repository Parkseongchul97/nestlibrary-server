package com.server.nestlibrary.controller;

import com.server.nestlibrary.model.vo.Comment;
import com.server.nestlibrary.model.vo.MessageBoxDTO;
import com.server.nestlibrary.model.vo.Messages;
import com.server.nestlibrary.model.vo.Paging;
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

    // 내가 안읽은 메일 조회
    @GetMapping("/private/messages")
    public ResponseEntity notOpenMessages(@RequestParam(name = "page", defaultValue = "1") int page,
                                         @RequestParam(name = "target", defaultValue = "", required = false) String target,
                                         @RequestParam(name = "keyword", defaultValue = "", required = false) String keyword){
        // 내가 받은 모든 쪽지 가져오기
        int totalCount = messagesService.messageCount(target, keyword).size();
        Paging paging = new Paging(page, totalCount);
        paging.setTotalPage(totalCount);
        paging.setOffset(paging.getLimit() * (paging.getPage() - 1));
        return  ResponseEntity.ok(MessageBoxDTO.builder().paging(paging).messagesDTOList(messagesService.notOpenMessages(paging, target, keyword)).build());
    }
    // 전체 메일 조회
    @GetMapping("/private/messages/all")
    public ResponseEntity findMyMessages(@RequestParam(name = "page", defaultValue = "1") int page,
                                         @RequestParam(name = "target", defaultValue = "", required = false) String target,
                                         @RequestParam(name = "keyword", defaultValue = "", required = false) String keyword){
        // 내가 받은 모든 쪽지 가져오기
        int totalCount = messagesService.allMessages(target, keyword).size();
        Paging paging = new Paging(page, totalCount);
        paging.setTotalPage(totalCount);
        paging.setOffset(paging.getLimit() * (paging.getPage() - 1));
        return  ResponseEntity.ok(MessageBoxDTO.builder().paging(paging).messagesDTOList(messagesService.findMyMessages(paging, target, keyword)).build());
    }
    // 내가 받은 메일 조회
    @GetMapping("/private/messages/to")
    public ResponseEntity findToMessages(@RequestParam(name = "page", defaultValue = "1") int page,
                                         @RequestParam(name = "target", defaultValue = "", required = false) String target,
                                         @RequestParam(name = "keyword", defaultValue = "", required = false) String keyword){
        // 내가 받은 모든 쪽지 가져오기
        int totalCount = messagesService.toMessages(target, keyword).size();
        Paging paging = new Paging(page, totalCount);
        paging.setTotalPage(totalCount);
        paging.setOffset(paging.getLimit() * (paging.getPage() - 1));
        return ResponseEntity.ok(MessageBoxDTO.builder().paging(paging).messagesDTOList(messagesService.findMyToMessages(paging, target, keyword)).build());
    }
    // 내가 보낸 메일 조회
    @GetMapping("/private/messages/from")
    public ResponseEntity findFromMessages(@RequestParam(name = "page", defaultValue = "1") int page,
                                           @RequestParam(name = "target", defaultValue = "", required = false) String target,
                                           @RequestParam(name = "keyword", defaultValue = "", required = false) String keyword){
        // 내가 보낸 모든 쪽지 가져오기
        int totalCount = messagesService.fromMessages(target, keyword).size();
        Paging paging = new Paging(page, totalCount);
        paging.setTotalPage(totalCount);
        paging.setOffset(paging.getLimit() * (paging.getPage() - 1));
        return ResponseEntity.ok(MessageBoxDTO.builder().paging(paging).messagesDTOList(messagesService.findMyFromMessages(paging, target, keyword)).build());
    }
    // 단일 메일 조회
    @GetMapping("/private/messages/{messagesCode}")
    public ResponseEntity findMessages(@PathVariable(name = "messagesCode") int messagesCode){
        // 해당 쪽지의 정보 자세히 뿌리기
        // 수신자인지 발신자인지 확인후 수신자가 확인하면 조회여부 변경
        return ResponseEntity.ok(messagesService.viewMessage(messagesCode));
    }
    // 내가 안읽은 메시지 숫자
    @GetMapping("/private/messages/count")
    public ResponseEntity messageCount(){

        return ResponseEntity.ok(messagesService.messageCount(null,null).size());
    }

    // 메일 발송시 대상 찾기
    @GetMapping("/private/user")
    public ResponseEntity findUser(@RequestParam(name = "userNickname") String userNickname){
        // 해당 닉네임 포함된 유저 리스트 리턴
        if(userNickname == "")return ResponseEntity.ok(null);
        return ResponseEntity.ok(userService.findByNicknameUserList(userNickname));
    }
    
    // 쪽지 보내기
    @PostMapping("/private/messages")
    public ResponseEntity addMessages(@RequestBody Messages vo){
        // 수신자 정보를(닉네임을 받아서) 검색후 선택 ? 입력후 DB 저장

        return ResponseEntity.ok(messagesService.addMessages(vo));
    }
    // 쪽지 삭제
    @DeleteMapping("/private/messages/{messagesCode}")
    public ResponseEntity removeMessages(@PathVariable(name = "messagesCode") int messagesCode){
        // 발신자인지 수신자인지 확인해서 messages_is_delete 변경, 만약 이미 messages_is_delete 가 반대에서 삭제한 경우 그냥 삭제
        messagesService.removeMessages(messagesCode);
        return ResponseEntity.ok(null);
    }




}
