package WebSocket.socket.security;

import WebSocket.socket.entity.Member;
import WebSocket.socket.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;


@Component
@Slf4j
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));

        log.info("CustomUserDetailsService email = {}", email);
        log.info("CustomUserDetailsService member = {}", member);

        // 조회된 Member 객체를 CustomUserDetails 객체로 변환하여 반환
        return new CustomUserDetails(member);
    }
}
