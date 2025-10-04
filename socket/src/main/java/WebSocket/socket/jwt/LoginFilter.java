package WebSocket.socket.jwt;

import WebSocket.socket.dto.LoginDto;
import WebSocket.socket.repository.RefreshTokenRepository;
import WebSocket.socket.security.CustomUserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Collection;

@Slf4j
public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;

    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    private final long refreshExpirationTime;

    public LoginFilter(AuthenticationManager authenticationManager,
                       JwtUtil jwtUtil,
                       RefreshTokenRepository refreshTokenRepository,
                       @Value("${jwt.refresh-expiration-time}") long refreshExpirationTime) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshExpirationTime = refreshExpirationTime;
        // 로그인 URL 설정 (필요에 따라 커스터마이징)
        setFilterProcessesUrl("/auth/login");
    }



    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            // 요청 본문에서 사용자 이름과 비밀번호를 읽어와 LoginDto 객체로 변환
            LoginDto loginDto = new ObjectMapper().readValue(request.getInputStream(), LoginDto.class);

            log.info("username = {}", loginDto.getEmail());
            log.info("password = {}", loginDto.getPassword());

            // UsernamePasswordAuthenticationToken 생성
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    loginDto.getEmail(),
                    loginDto.getPassword(),
                    null // 인증 전이므로 권한은 null
            );

            // AuthenticationManager를 통해 인증 시도
            return authenticationManager.authenticate(authToken);

        } catch (IOException e) {
            throw new RuntimeException("로그인 정보 파싱 실패", e);
        }
    }


    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {

        // 1. 인증된 사용자의 정보(principal)에서 실제 UserDetails 객체를 가져옵니다.
        // getPrincipal()은 Authentication 객체에 담긴 주체(principal)를 반환합니다.
        CustomUserDetails customUserDetails = (CustomUserDetails) authResult.getPrincipal();

        // 2. CustomUserDetails에서 필요한 정보들을 추출합니다.
        Long memberId = Long.parseLong(customUserDetails.getUsername());
        Collection<? extends GrantedAuthority> authorities = customUserDetails.getAuthorities();

        // 3. 추출한 정보들을 바탕으로 JWT 토큰을 생성합니다.
        String accessToken = jwtUtil.generateToken(authorities, memberId, JwtUtil.TokenType.ACCESS_TOKEN);
        String refreshToken = jwtUtil.generateToken(authorities, memberId, JwtUtil.TokenType.REFRESH_TOKEN);

        refreshTokenRepository.saveRefreshToken(customUserDetails.getUsername(), refreshToken, refreshExpirationTime);

        // 4. 응답 헤더에 토큰을 추가합니다.
        // 토큰을 클라이언트에게 전달하는 가장 일반적인 방법은 Authorization 헤더에 Bearer 타입으로 넣는 것입니다.
        response.addHeader("Authorization", "Bearer " + accessToken);
        // 또는 필요에 따라 refresh token을 쿠키나 다른 헤더에 추가할 수 있습니다.

        // 5. 로그인 성공 응답을 보냅니다.
        // 본문을 JSON으로 구성하여 클라이언트에게 더 상세한 정보를 전달할 수 있습니다.
        response.setStatus(HttpServletResponse.SC_OK); // 200 OK
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // JSON 응답 본문
        String jsonResponse = String.format("{\"message\": \"로그인 성공\"," +
                " \"accessToken\": \"%s\", \"refreshToken\": \"%s\"}", accessToken, refreshToken);
        response.getWriter().write(jsonResponse);
    }


    @Override //로그인 실패시 실행하는 메소드
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) {
        response.setStatus(401) ;
        System.out.println("login fail");
    }


}