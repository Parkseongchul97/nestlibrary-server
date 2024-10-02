package com.server.nestlibrary.service;

import com.server.nestlibrary.model.vo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    private BCryptPasswordEncoder bcpe = new BCryptPasswordEncoder();

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // 임시 비밀번호
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()";
    private static final int PASSWORD_LENGTH = 12;

    // 인증번호
    public static final int code = (int)(Math.random() * (90000)) + 100000;

    public int sendEmailCode(String userEmail) {
        String subject = "Nest Library 사이트 인증 코드 안내";
        String body = code + "를 입력해 주십시오.";
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(userEmail); // 수신자
        message.setSubject(subject); // 제목
        message.setText(body); // 이메일 내용
        message.setFrom("dol9991@naver.com"); // 발신자 이메일 주소 설정
        try {
            mailSender.send(message);  // 발송
            return code;
        } catch (Exception e) {
            return -1;
        }

    }


    // 임시 비밀번호 생성 메서드
//    private String generateTemporaryPassword() {
//        SecureRandom random = new SecureRandom();
//        StringBuilder password = new StringBuilder(PASSWORD_LENGTH);
//        for (int i = 0; i < PASSWORD_LENGTH; i++) {
//            int index = random.nextInt(CHARACTERS.length());
//            password.append(CHARACTERS.charAt(index));
//        }
//        return password.toString();
//    }
    // 인증코드



//    // 이메일로 임시 비밀번호 전송
//    public void sendTemporaryPasswordEmail(String email, String tempPassword) {
//        String subject = "Nest Library 임시 비밀번호 안내";
//        String body = "당신의 임시 비밀번호는: " + tempPassword + " 입니다.";
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setTo(email); // 수신자
//        message.setSubject(subject); // 제목
//        message.setText(body); // 이메일 내용
//        message.setFrom("dol9991@naver.com"); // 발신자 이메일 주소 설정
//        try {
//            mailSender.send(message);  // 발송
//        } catch (Exception e) {
//
//        }
//
//    }
//    // 임시 비밀번호
//    public void processPasswordReset(User user) {
//        String tempPassword = generateTemporaryPassword();
//        // 임시 비밀번호 전송용
//        sendTemporaryPasswordEmail(user.getUserEmail(), tempPassword);
//    }
}
