package WebSocket.socket.config;

import WebSocket.socket.jwt.JwtFilter;
import WebSocket.socket.jwt.JwtUtil;
import WebSocket.socket.jwt.LoginFilter;
import WebSocket.socket.repository.RefreshTokenRepository;
import WebSocket.socket.security.CustomAuthenticationProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {


    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtFilter jwtFilter;
    private final AuthenticationConfiguration authenticationConfiguration;

    private static final String[] PERMIT_ALL_PATTERNS = {
            "/auth/login",
    };

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{

        // 1. CSRF 비활성화 (JWT는 CSRF 공격으로부터 안전함)
        http.csrf(AbstractHttpConfigurer::disable);

        // 2. 세션 관리 방식 설정: JWT 기반 인증이므로 세션 STATELESS로 설정
        http.sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );


        // 3. 인가(Authorization) 규칙 정의
        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers(PERMIT_ALL_PATTERNS).permitAll()
                .anyRequest().authenticated());

        // 4. 필터 체인에 커스텀 필터 추가
        // 로그인 필터는 Spring의 UsernamePasswordAuthenticationFilter 위치에 추가하여 대체함.

        AuthenticationManager authenticationManager = authenticationConfiguration.getAuthenticationManager();

        // 5. LoginFilter를 직접 생성하고 필요한 의존성을 주입합니다.
        // LoginFilter가 생성하는 토큰: 당신의 LoginFilter는 사용자의 아이디와 비밀번호를 받아서
        // UsernamePasswordAuthenticationToken 객체를 생성하여 AuthenticationManager에게 전달합니다.
        // 이 토큰은 "사용자 이름과 비밀번호를 포함하는 인증 시도"를 나타냅니다.
        long refreshExpirationTime = 3600000L;
        LoginFilter loginFilter = new LoginFilter(
                authenticationManager,
                jwtUtil,
                refreshTokenRepository,
                refreshExpirationTime // @Value를 사용하지 않고 여기서 직접 값 전달
        );
        loginFilter.setFilterProcessesUrl("/auth/login");

        http.addFilterAt(loginFilter, UsernamePasswordAuthenticationFilter.class);

        // JWT 필터는 UsernamePasswordAuthenticationFilter 이전에 실행되도록 배치.
        // 이는 로그인 요청을 제외한 모든 요청에서 토큰을 검증하는 역할을 수행.
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);


        return http.build();
    }
}
