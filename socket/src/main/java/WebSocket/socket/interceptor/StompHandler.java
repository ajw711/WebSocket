package WebSocket.socket.interceptor;

import WebSocket.socket.common.ChatManager;
import WebSocket.socket.common.SessionManager;
import WebSocket.socket.entity.Member;
import WebSocket.socket.jwt.JwtUtil;
import WebSocket.socket.repository.MemberRepository;
import WebSocket.socket.security.CustomUserDetails;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompHandler implements ChannelInterceptor {

    private final JwtUtil jwtUtil;
    private final SessionManager sessionManager;
    private final MemberRepository memberRepository;

    // 메시지가 채널로 전송되기 전에 검사
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        log.info("StompHandler = {} ", accessor);

        // CONNECT 명령어 확인: WebSocket 연결 요청 시에만 인증 로직 수행
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {

            // Authorization 헤더에서 JWT 추출 (클라이언트는 'Authorization' 헤더에 토큰을 담아 보냄)
            String authorizationHeader = accessor.getFirstNativeHeader("Authorization");

            if (authorizationHeader.startsWith("Bearer ")) {
                String token = authorizationHeader.substring(7);

                try {
                    // JwtUtil을 사용하여 토큰 유효성 검사 및 클레임 파싱
                    Claims claims = jwtUtil.parseAndValidateToken(token);

                    // 클레임에서 사용자 정보 및 권한 추출
                    Long memberId = jwtUtil.getMemberId(claims);
                    Collection<? extends GrantedAuthority> authorities = jwtUtil.getAuthorities(claims);
                    Member member = memberRepository.findById(memberId).orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: "));
                    log.info("memberId = {} ", memberId);
                    log.info("member = {} ", member);
                    CustomUserDetails customUserDetails = new CustomUserDetails(member);

                    // Spring Security Authentication 객체 생성
                    Authentication authentication = new UsernamePasswordAuthenticationToken(
                            customUserDetails, // Principal: 일반적으로 사용자 ID 사용
                            null,                    // Credentials: 토큰 인증 시 비밀번호는 null
                            authorities              // 권한 정보
                    );

                    // WebSocket 세션에 인증 정보 저장
                    // 이 정보는 @MessageMapping 메서드의 Principal 인자로 사용
                    accessor.setUser(authentication);
                    // (선택) SecurityContext에도 저장 (필요한 경우)
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    // 세션 정보 추출
                    String sessionId = accessor.getSessionId();

                    sessionManager.saveSession(sessionId, memberId.toString());

                } catch (JwtUtil.ExpiredTokenException | JwtUtil.NotValidTokenException e) {
                    log.error("STOMP 연결 실패: 유효하지 않은 JWT 또는 만료: {}", e.getMessage());
                    //  인증 실패 시 CONNECT 명령을 거부하여 연결을 끊기.
                    // Spring은 예외 발생 시 자동으로 DISCONNECT 프레임을 클라이언트에 보낸다.
                    throw e;
                }

            } else {
                // 토큰이 없는 경우 (인증되지 않은 연결 시도)
                log.warn("STOMP 연결 실패: Authorization 헤더 누락");
                throw new RuntimeException("Authorization header required.");
            }
        } else if(StompCommand.SUBSCRIBE.equals(accessor.getCommand())){

        }

        return message;
    }
}

