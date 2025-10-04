package WebSocket.socket.jwt;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // 1. HTTP 요청 헤더에서 토큰 추출
        String token = jwtUtil.extractToken(request);

        // 2. 토큰이 존재하면 유효성 검증
        if(token != null){
            try {
                Claims claims = jwtUtil.parseAndValidateToken(token);

                Long memberId = jwtUtil.getMemberId(claims);
                String tokenType = jwtUtil.getTokenType(claims).name();
                Collection<? extends GrantedAuthority> authorities = jwtUtil.getAuthorities(claims);

                if (jwtUtil.getTokenType(claims) == JwtUtil.TokenType.ACCESS_TOKEN) {

                    // 6. Authentication 객체 생성
                    Authentication authentication = new UsernamePasswordAuthenticationToken(
                            memberId.toString(), null, authorities);

                    // 7. SecurityContext에 Authentication 객체 설정
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (JwtUtil.ExpiredTokenException e) {
                log.warn("Access token has expired: {}", e.getMessage());
            } catch (JwtUtil.NotValidTokenException e) {
                log.error("Invalid token: {}", e.getMessage());
            } catch (Exception e) {
                log.error("An unexpected error occurred during token validation", e);
            }

        }

        filterChain.doFilter(request, response);


    }
}
