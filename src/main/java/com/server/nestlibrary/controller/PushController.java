package com.server.nestlibrary.controller;

import com.server.nestlibrary.service.PushService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/*")
@CrossOrigin(origins = {"*"}, maxAge = 6000)
public class PushController {

    @Autowired
    private PushService pushService;

    // 리액트 쿼리로 계속 불러올 예정인 내 알림목록
    @GetMapping("/private/push")
    public ResponseEntity findPush(){
        return ResponseEntity.ok(pushService.allPush());
    }
    // 단일 알람 삭제 (버튼을 누른경우)
    @DeleteMapping("/private/push/{pushCode}")
    public ResponseEntity removePush(@PathVariable(name = "pushCode")int pushCode){
        pushService.removePush(pushCode);
        return ResponseEntity.ok(null);
    }
    // 모든 알람 삭제 (버튼을 누른경우)
    @DeleteMapping("/private/push")
    public ResponseEntity removeAllPush(){
        pushService.removeAllPush();
        return ResponseEntity.ok(null);
    }

}
