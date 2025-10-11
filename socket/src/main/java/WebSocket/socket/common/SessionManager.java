package WebSocket.socket.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class SessionManager {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String PREFIX = "WS_SESSION:";
    private static final long SESSION_TTL_MINUTES = 10L;

    //  세션 저장 (memberId → sessionId)
    public void saveSession(String memberId, String sessionId) {
        String key = PREFIX + memberId;
        redisTemplate.opsForValue().set(key, sessionId, SESSION_TTL_MINUTES, TimeUnit.MINUTES);
        log.info("세션 저장: memberId={}, sessionId={}, TTL={}분", memberId, sessionId, SESSION_TTL_MINUTES);
    }

    public void deleteSession(String memberId) {
        String key = PREFIX + memberId;
        redisTemplate.delete(key);
        log.info("세션 삭제: memberId={}", memberId);
    }

    //  세션 갱신 및 재연결 감지
    public void updateSession(String memberId, String newSessionId) {
        String key = PREFIX + memberId;
        String oldSessionId = redisTemplate.opsForValue().get(key);

        // 기존 세션이 없으면 신규 연결
        if (oldSessionId == null) {
            redisTemplate.opsForValue().set(key, newSessionId, SESSION_TTL_MINUTES, TimeUnit.MINUTES);
            log.info("신규 연결: memberId={}, sessionId={}", memberId, newSessionId);

        }

        // 기존 세션과 다르면 재연결
        if (!oldSessionId.equals(newSessionId)) {
            redisTemplate.opsForValue().set(key, newSessionId, SESSION_TTL_MINUTES, TimeUnit.MINUTES);
            log.info("재연결 감지: memberId={}, oldSessionId={}, newSessionId={}", memberId, oldSessionId, newSessionId);

        }

        // 동일 세션이면 TTL만 갱신 (keep-alive)
        redisTemplate.expire(key, SESSION_TTL_MINUTES, TimeUnit.MINUTES);

    }
}
