package WebSocket.socket.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final CustomUserDetailsService customUserDetailsService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        // 1. Authentication 객체에서 사용자 이름과 비밀번호 추출
        String email = authentication.getName();
        String password = authentication.getCredentials().toString();

        log.info("CustomAuthenticationProvider email = {}", email);
        log.info("CustomAuthenticationProvider password = {}", password);


        // 2. UserDetailsService를 통해 사용자 정보 로드
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        // 3. 비밀번호 일치 여부 확인
        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            throw new BadCredentialsException("비밀번호가 일치하지 않습니다.");
        }

        // 4. 인증 성공 시, 새로운 Authentication 객체 반환
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    //AuthenticationManager는 Provider 목록을 순회하며 supports() 메서드가 True를 반환하는 Provider를 찾습니다.
    // return false; 이는 CustomAuthenticationProvider가 어떤 토큰 타입도 지원하지 않는다고 선언한 것과 같습니다.
    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
