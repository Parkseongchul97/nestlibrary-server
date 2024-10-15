package com.server.nestlibrary.config;

import com.server.nestlibrary.model.vo.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private TokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 클라이언트에서 보낸 토큰을 받아서 사용자 확인 후 인증 처리
        String token = parseBearerToken(request);

        if(token != null){
            User user = tokenProvider.validate(token);
            System.out.println("유저 : " + user);
            // 추출한 인증 정보를 필터링에서 사용할 수 있도록 Security Context에 등록
            AbstractAuthenticationToken authenticationToken= new UsernamePasswordAuthenticationToken(user , user.getUserPassword(),new ArrayList<>());
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(authenticationToken); // 인증정보
            SecurityContextHolder.setContext(securityContext);
        }
        filterChain.doFilter(request,response);

    }

    private  String parseBearerToken(HttpServletRequest request){
        String bearerToken = request.getHeader("Authorization");
        if(StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer")){
            return  bearerToken.substring(7); // 받아오는 형태인  Authorization : Bearer 토큰글씨 에서 뒤에만
        }
        return null; // 아닐땐 null
    }
}
