package com.server.nestlibrary.service;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.server.nestlibrary.model.dto.MessagesDTO;
import com.server.nestlibrary.model.vo.Messages;
import com.server.nestlibrary.model.vo.Paging;
import com.server.nestlibrary.model.vo.QMessages;
import com.server.nestlibrary.model.vo.User;
import com.server.nestlibrary.repo.MessagesDAO;
import com.server.nestlibrary.repo.UserDAO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class MessagesService {

    @Autowired
    private MessagesDAO messagesDAO;
    @Autowired
    private UserDAO userDAO;
    @Autowired
    private UserService userService;
    @Autowired
    private JPAQueryFactory queryFactory;

    private final QMessages qMessages = QMessages.messages;

   // 메일 발송
    public MessagesDTO addMessages(Messages messages){
        User user = userService.getLoginUser();
        if(user.getUserPoint() < 50) return null;
        messages.setMessagesSentAt(LocalDateTime.now());
        messages.setMessagesFromUser(user.getUserEmail());
        Messages vo = messagesDAO.save(messages);
        user.setUserPoint(user.getUserPoint()-50);
        userDAO.save(user);
        return  voMessagesDTO(vo);
    }
    // 내가 안읽은 모든 메일
    public List<MessagesDTO> notOpenMessages(Paging paging,String target, String keyword){
        String email =  userService.getLoginUser().getUserEmail();
        List<Messages> voList = messagesJPAQuery(paging, target, keyword)
                .where(qMessages.messagesToUser.eq(email)) // 내가 수신자고
                .where(qMessages.messagesRead.eq(false)) // 안읽은거만
                .where(qMessages.messagesToDelete.eq(false)) // 삭제 안한거만
                .fetch();
        List<MessagesDTO> dtoList = new ArrayList<>();
        for (Messages m : voList){
            dtoList.add(voMessagesDTO(m)) ;
        }
        return dtoList;
    }
    // 나의 모든 메일
    public List<MessagesDTO> findMyMessages(Paging paging,String target, String keyword){
       String email =  userService.getLoginUser().getUserEmail();
        List<Messages> voList = messagesJPAQuery(paging, target, keyword)
                .where(qMessages.messagesFromUser.eq(email).and(qMessages.messagesFromDelete.eq(false))
                        .or(qMessages.messagesToUser.eq(email)).and(qMessages.messagesToDelete.eq(false)))
                .fetch();
        List<MessagesDTO> dtoList = new ArrayList<>();
        for (Messages m : voList){
            dtoList.add(voMessagesDTO(m)) ;
        }
        return dtoList;
    }
    //내가 받은 메일 목록 dto
    public List<MessagesDTO> findMyToMessages(Paging paging,String target, String keyword){
        String email =  userService.getLoginUser().getUserEmail();
        List<Messages> voList = messagesJPAQuery(paging, target, keyword)
                .where(qMessages.messagesToUser.eq(email))
                .where(qMessages.messagesToDelete.eq(false))
                .fetch();
        List<MessagesDTO> dtoList = new ArrayList<>();
        for (Messages m : voList){
            dtoList.add(voMessagesDTO(m)) ;
        }
        return dtoList;
    }
    //내가 보낸 메일 목록 dto
    public List<MessagesDTO> findMyFromMessages(Paging paging,String target, String keyword){
        String email =  userService.getLoginUser().getUserEmail();
        List<Messages> voList = messagesJPAQuery(paging, target, keyword)
                .where(qMessages.messagesFromUser.eq(email))
                .where(qMessages.messagesFromDelete.eq(false))
                .fetch();
        List<MessagesDTO> dtoList = new ArrayList<>();
        for (Messages m : voList){
            dtoList.add(voMessagesDTO(m)) ;
        }
        return dtoList;
    }
    // 단일 메시지 확인
    public MessagesDTO viewMessage(int messagesCode){
        Messages vo = messagesDAO.findById(messagesCode).get();
        String email =  userService.getLoginUser().getUserEmail();
        if(vo.getMessagesToUser().equals(email)){ // 수신자 확인이면
            log.info("읽음 여부 변경");
            vo.setMessagesRead(true); // 조회여부 트루로
           Messages reVo = messagesDAO.save(vo);
           return voMessagesDTO(reVo);
        }
       return voMessagesDTO(vo);
    }

    // 내 모든 메시지 vo(카운트용)
    public List<Messages> allMessages(String target, String keyword){
        String email =  userService.getLoginUser().getUserEmail();
       return search(target,keyword)
                .where(qMessages.messagesFromUser.eq(email).and(qMessages.messagesFromDelete.eq(false))
                        .or(qMessages.messagesToUser.eq(email)).and(qMessages.messagesToDelete.eq(false)))
                .fetch();
    }
    // 내 모든 발신 메시지 (카운트용)
    public List<Messages> fromMessages(String target, String keyword){
        String email =  userService.getLoginUser().getUserEmail(); // 내가
        return search(target,keyword)
                .where(qMessages.messagesFromUser.eq(email)) // 발신자일때만
                .where(qMessages.messagesFromDelete.eq(false))// 발신자 삭제가 아닐때만
                .fetch();
    }
    // 내 모든 수신 메시지 (카운트용)
    public List<Messages> toMessages(String target, String keyword){
        String email =  userService.getLoginUser().getUserEmail();
        return  search(target,keyword)
                .where(qMessages.messagesToUser.eq(email))
                .where(qMessages.messagesToDelete.eq(false))// 수신자 삭제가 아닐때만
                .fetch();
    }
    // 내가 안읽은 메시지(카운트용)
    public List<Messages> messageCount(String target, String keyword){
        String email =  userService.getLoginUser().getUserEmail();
        return search(target,keyword)
                .where(qMessages.messagesToUser.eq(email))
                .where(qMessages.messagesRead.eq(false))
                .where(qMessages.messagesToDelete.eq(false))
                .fetch();
    }
    // 쪽지 삭제
    public void removeMessages(int messagesCode){
        // 메시지 코드 받아온거로 사용자가 수신자인지 발신자인지 확인후
        String email =  userService.getLoginUser().getUserEmail(); // 사용자
        Messages messages = messagesDAO.findById(messagesCode).get(); // 삭제 예정 메시지
        if(messages.getMessagesToUser().equals(email)){ // 수신자 삭제 상황이라면
            if(messages.isMessagesFromDelete()){ // 이미 발신자도 삭제한 메일이면
                messagesDAO.deleteById(messagesCode);
            }else { // 아직 발신자는 삭제를 안했다면
                messages.setMessagesToDelete(true);
                messagesDAO.save(messages); // 수신자 삭제만 추가
            }
        }else{// 발신자 삭제 상황이라면
            if(messages.isMessagesToDelete()){ // 이미 수신자도 삭제한 메일이면
                messagesDAO.deleteById(messagesCode);
            }else { // 아직 수신자는 삭제를 안했다면
                messages.setMessagesFromDelete(true);
                messagesDAO.save(messages); // 수신자 삭제만 추가
            }

        }
        
    }
    // 페이징으로 쿼리 반환 페이징, 출력 조건만
    public JPAQuery<Messages> messagesJPAQuery(Paging paging, String target, String keyword){
        return search(target,keyword).orderBy(qMessages.messagesSentAt.desc())
                .offset(paging.getOffset())
                .limit(paging.getLimit());


    }
    // 검색 여부까지 확인
    public JPAQuery<Messages> search(String target, String keyword){
        JPAQuery<Messages> query = queryFactory.selectFrom(qMessages);
        if (target != null && !target.equals("") && keyword != null && !keyword.equals("")) {
            if (target.equals("title")) { // 제목이 쪽지
                query.where(qMessages.messagesTitle.containsIgnoreCase(keyword));
            } else if (target.equals("user")) { // 작성자 검색이면 수신, 발신 여부 찾음
                query.where(qMessages.messagesFromUser.containsIgnoreCase(keyword)
                        .or(qMessages.messagesToUser.containsIgnoreCase(keyword)));
            }
        }
        return query;
    }
    // vo 를 dto 로 바꾸는 메서드
    public MessagesDTO voMessagesDTO(Messages vo){
        User fromUser = userService.findUser(vo.getMessagesFromUser());
        User toUser = userService.findUser(vo.getMessagesToUser());
        fromUser.setUserPassword(null);

        toUser.setUserPassword(null);
        return MessagesDTO.builder()
                .messagesCode(vo.getMessagesCode())
                .messagesContent(vo.getMessagesContent())
                .messagesTitle(vo.getMessagesTitle())
                .messagesFromUser(fromUser)
                .messagesToUser(toUser)
                .messagesRead(vo.isMessagesRead())
                .messagesSentAt(vo.getMessagesSentAt())
                .build();
    }
}
