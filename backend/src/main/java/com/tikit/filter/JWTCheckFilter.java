package com.tikit.filter;

import com.tikit.common.dto.ResponseDTO;
import com.tikit.util.JWTProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class JWTCheckFilter extends OncePerRequestFilter {

    private final JWTProvider jWTProvider;
    private final ObjectMapper objectMapper;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {

        String path = request.getServletPath();

        return path.equals("/api/v1/auth/login") || path.equals("/api/v1/auth/signup") || path.equals("/api/v1/auth/refresh");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String authHeaderStr = request.getHeader("Authorization");

        if (authHeaderStr == null) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!authHeaderStr.startsWith("Bearer ")) {
            sendErrorResponse(response, "올바르지 않은 인증 형식입니다.");
            return;
        }

        // JWT 토큰 앞에 "Bearer " 제거
        String accessToken = authHeaderStr.substring(7);

        try {
            // 추출 할 때 만료 여부 위조 여부 확인
            Claims claims = jWTProvider.getClaims(accessToken);

            // 액세스 토큰인지 검증
            if (!"ACCESS".equals(claims.get("tokenType", String.class))) {
                sendErrorResponse(response, "Access Token이 아닙니다.");
                return;
            }

            // 토큰에서 정보 추출
            String accountId = claims.getSubject();
            String email = claims.get("email", String.class);
            String roleName = claims.get("roleName", String.class);

            log.info("JWT 검증 성공");
            log.info("JWT 사용자 정보: {}", claims);

            // Security Token 생성
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    accountId,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + roleName))
            );

            // Security Context에 저장
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);

        } catch (ExpiredJwtException e) {
            sendErrorResponse(response, "Access Token이 만료되었습니다.");
            return;
        } catch (JwtException | IllegalArgumentException e) {
            sendErrorResponse(response, "유효하지 않은 Access Token입니다.");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        ResponseDTO<Object> responseDTO = new ResponseDTO<>(
                -1,
                message,
                null
        );

        PrintWriter printWriter = response.getWriter();

        String json = objectMapper.writeValueAsString(responseDTO);

        printWriter.print(json);
    }


}
