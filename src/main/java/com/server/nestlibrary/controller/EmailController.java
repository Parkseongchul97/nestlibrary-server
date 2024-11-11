package com.server.nestlibrary.controller;

import com.server.nestlibrary.model.vo.User;
import com.server.nestlibrary.service.EmailService;
import com.server.nestlibrary.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/email/*")
@CrossOrigin(origins = {"*"}, maxAge = 6000)
public class EmailController {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;


 // 신규 가입시
    @GetMapping("/code")
    public ResponseEntity sendEmail(@RequestParam("userEmail") String userEmail) {
        User user =  userService.findUser(userEmail);
            if (user == null)  // 해당 이메일로 가입된 유저가 없으면
                return ResponseEntity.status(HttpStatus.OK).body(emailService.sendEmailCode(userEmail));
        // 이미 가입한 유저면 -1 리턴
        return ResponseEntity.status(HttpStatus.OK).body(-1);
    }

  // 채널삭제시 이메일 인증 할때

  @GetMapping("/private/code")
  public ResponseEntity sendCode(@RequestParam("userEmail") String userEmail) {
        return ResponseEntity.status(HttpStatus.OK).body(emailService.sendEmailCode(userEmail));

  }





    @GetMapping("/findPassword")
    public ResponseEntity newPassword(@RequestParam("userEmail") String userEmail) {
        User user =  userService.findUser(userEmail);
        log.info("유저정보 " + user);
        if (user == null) {
            log.info("1번" );
            return ResponseEntity.status(HttpStatus.OK).body(1);

        } else if(user.getUserPassword() == null){
            log.info("2번" );
            return ResponseEntity.status(HttpStatus.OK).body(2);
        } else {
            log.info("3번" );
            emailService.processPasswordReset(user);


            return ResponseEntity.status(HttpStatus.OK).body(3);

        }


    }


    @PostMapping("/code")
    public ResponseEntity<Boolean> codeCheck(@RequestParam(name="code") String code) {

        log.info("code : " + code);

        if (String.valueOf(emailService.code).equals(code)) {
            return ResponseEntity.ok(true);
        } else {
            return ResponseEntity.ok(false);
        }
    }
}
