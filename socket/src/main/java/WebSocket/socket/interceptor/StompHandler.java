package WebSocket.socket.interceptor;

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
    private final MemberRepository memberRepository;



    // 💡 메시지가 채널로 전송되기 전에 가로챕니다.
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if(!StompCommand.CONNECT.equals((accessor.getCommand()))){
            return message;
        }

        List<String> authHeaders = accessor.getNativeHeader("Authorization");
        String token = authHeaders.get(0);

        if(token != null) {
            Claims claims = jwtUtil.parseAndValidateToken(token);
            String memberId = jwtUtil.getMemberId(claims).toString();

            // Spring Security의 Authentication 객체 생성 및 Security Context 등록
            Authentication authentication = new UsernamePasswordAuthenticationToken(memberId, token, null);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // WebSocket 연결에서 사용자 정보 설정 (STOMP 세션에 사용자 정보 설정)
            accessor.setUser(authentication);
        } else {
            throw new IllegalArgumentException("Invalid JWT token");
        }

        log.info("CustomJwtInterceptor 끝!");
        return message;
    }
}

