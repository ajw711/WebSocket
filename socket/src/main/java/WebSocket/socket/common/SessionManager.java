package WebSocket.socket.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class SessionManager {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String PREFIX = "WS_SESSION:";

    public void saveSession(String sessionId, String memberId) {
        redisTemplate.opsForValue().set(PREFIX + sessionId, memberId);
        log.info("세션 저장: {} -> {}", sessionId, memberId);
    }

    public void removeSession(String sessionId) {
        redisTemplate.delete(PREFIX + sessionId);
        log.info("세션 제거: {}", sessionId);
    }

    public Optional<String> getMemberId(String sessionId) {
        return Optional.of(redisTemplate.opsForValue().get(PREFIX + sessionId));
    }
}
