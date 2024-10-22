package com.server.nestlibrary.service;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.server.nestlibrary.model.dto.UserDTO;
import com.server.nestlibrary.model.vo.QPost;
import com.server.nestlibrary.model.vo.QPostLike;
import com.server.nestlibrary.model.vo.QUser;
import com.server.nestlibrary.model.vo.User;
import com.server.nestlibrary.repo.UserDAO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
        // 비밀번호 암호화
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
                .userNickname(user.getUserNickname())
                .userImg(user.getUserImgUrl())

                .build();

        return userdto;
    }
    public List<User> findByNicknameUserList(String userNickname){
            return queryFactory.selectFrom(qUser)
                    .where(qUser.userNickname.containsIgnoreCase(userNickname))
                    .limit(10)
                    .fetch();
    }
}
