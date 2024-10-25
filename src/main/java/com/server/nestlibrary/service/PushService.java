package com.server.nestlibrary.service;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.server.nestlibrary.model.vo.Push;
import com.server.nestlibrary.model.vo.QPush;
import com.server.nestlibrary.repo.PushDAO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Slf4j
@Service
public class PushService {
    @Autowired
    private PushDAO pushDAO;
    @Autowired
    private UserService userService;

    @Autowired
    private JPAQueryFactory queryFactory;

    private final QPush qPush = QPush.push;
    
    // 내 모든 알림
    public List<Push> allPush(){
        return queryFactory.selectFrom(qPush)
                .where(qPush.userEmail.eq(userService.getLoginUser().getUserEmail()))
                .orderBy(qPush.pushCreatedAt.asc())
                .limit(10) // 최신순 10개만
                .fetch();
    }

    // 알람추가
    public Push savePush(Push vo){
        log.info("브이오 : " + vo);
        return pushDAO.save(vo);
    }
    // 알람 삭제
    public void removePush(int pushCode){
        pushDAO.deleteById(pushCode);
    }
    
    // 내알람 다지우기
    public void removeAllPush(){
        List<Push> list = queryFactory.selectFrom(qPush)
                .where(qPush.userEmail.eq(userService.getLoginUser().getUserEmail()))
                .orderBy(qPush.pushCreatedAt.asc())
                .fetch();
        for(Push p : list){
            pushDAO.deleteById(p.getPushCode());
        }
    }
    
}
