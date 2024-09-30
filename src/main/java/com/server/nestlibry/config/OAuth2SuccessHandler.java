package com.server.nestlibry.config;


import com.server.nestlibry.model.vo.User;
import com.server.nestlibry.repo.UserDAO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private TokenProvider token;

    @Autowired
    private UserDAO dao;

    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OAuth2User user = (OAuth2User) authentication.getPrincipal();
        log.info("google : " + user);
        
        // 소셜 로그인 성공 후 사용자 정보 추출
        String email = (String) user.getAttributes().get("email");
        
        // DB에 사용자 정보 조회
        // + 존재하지 않으면 신규 사용자로 등록
        User user2 = dao.findById(email).orElseGet(() -> {
            return dao.save(User
                        .builder()
                        .userEmail(email)
                        .build());
        });

        // JWT 토큰 생성

        String token = this.token.create(user2);

        response.sendRedirect("http://localhost:3000/login-success?token=" + token);
    }
}
