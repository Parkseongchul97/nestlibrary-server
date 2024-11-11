package com.server.nestlibrary.service;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.server.nestlibrary.model.dto.UserDTO;
import com.server.nestlibrary.model.vo.*;
import com.server.nestlibrary.repo.UserDAO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class UserService {
    @Autowired
    private UserDAO dao;
    @Lazy
    @Autowired
    private MessagesService messagesService;
    
    @Autowired
    private PostService postService;
    @Lazy
    @Autowired
    private ManagementService managementService;
    @Lazy
    @Autowired
    private ChannelService channelService;
    @Lazy
    @Autowired
    private CommentService commentService;

    @Autowired
    private JPAQueryFactory queryFactory;

    private final QUser qUser = QUser.user;

    @Autowired
    private PasswordEncoder bcpe;
    // 사용자 정보 가져오기
    public User getLoginUser(){
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if(auth!= null && auth.isAuthenticated()){
                User user = (User) auth.getPrincipal();
                User result = dao.findById(user.getUserEmail()).get();

                return result;
            }
            return null;
        } catch (Exception e) {

            return null;
        }

    }

    @Transactional
    public void registerUser(User vo){
        if(getLoginUser()== null)
        vo.setUserPassword(bcpe.encode(vo.getUserPassword()));
        dao.save(vo);
    }


    @Transactional
    public void decodingPassword (User vo){
        vo.setUserPassword(bcpe.encode(vo.getUserPassword()));
        dao.save(vo);


    }


    // 로그인
    public  User login(String id, String password){
        User user = dao.findById(id).orElseThrow(()-> new UsernameNotFoundException("User Not Found"));
        if(bcpe.matches(password, user.getUserPassword())){ // 아이디 비밀번호 같을시
            return user;
        }
        return null;
    }
    public User findUser(String userEmail){

        return dao.findById(userEmail).orElse(null);
    }
    // 닉네임 중복체크용 닉네임으로 유저 찾기
    public User findByNickname(String nickname){
        User user = dao.findByUserNickname(nickname);
        return user;
    }
    public UserDTO findDTO(String userEmail){
        User user = dao.findById(userEmail).get();

        UserDTO userdto = UserDTO
                .builder()
                .userEmail(user.getUserEmail())
                .userNickname(user.getUserNickname())
                .userImgUrl(user.getUserImgUrl())

                .build();

        return userdto;
    }
    public List<User> findByNicknameUserList(String userNickname){
            return queryFactory.selectFrom(qUser)
                    .where(qUser.userNickname.containsIgnoreCase(userNickname))
                    .limit(10)
                    .fetch();
    }
    // 회원 탈퇴
    public void removeUser(){
        User user = getLoginUser();
        // 쪽지, 댓글, 호스트 채널은 따로 조회후 삭제
        // 내 쪽지
        List<Messages> myMessages = messagesService.allMessages(null, null);
        for(Messages m : myMessages){
            messagesService.removeMessages(m.getMessagesCode());
        }
        // 내 댓글
        List<Comment> myCommentList = commentService.getMyComment(user.getUserEmail());
        for(Comment c : myCommentList){
            commentService.removeComment(c.getCommentCode());
        }
        // 내 호스트 채널
        List<Management> myHostChannel = managementService.myManagement();
        for (Management m : myHostChannel){
            channelService.removeChannel(m.getChannel().getChannelCode());
        }
        // 알림, 메니지먼트, 게시글, 좋아요는 링크되어있어서 단순삭제로 가능
        dao.deleteById(user.getUserEmail());
    }
}
