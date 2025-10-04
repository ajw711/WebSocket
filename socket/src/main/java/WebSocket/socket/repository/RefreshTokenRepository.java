package WebSocket.socket.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String BLACKLIST_PREFIX = "BLACKLIST:";

    public void saveRefreshToken(String memberId, String refreshToken, long expirationMillis) {
        redisTemplate.opsForValue().set(
                getKey(memberId),
                refreshToken,
                Duration.ofMillis(expirationMillis)
        );
    }

    public String getRefreshToken(String username) {
        return redisTemplate.opsForValue().get(getKey(username));
    }

    public void deleteRefreshToken(String username) {
        redisTemplate.delete(getKey(username));
    }

    // 블랙리스트에 액세스 토큰을 저장
    public void addAccessTokenToBlacklist(String token, long expirationMillis) {
        redisTemplate.opsForValue().set(
                getBlacklistKey(token),
                "blacklisted",  // 블랙리스트에 있는 토큰을 나타내는 값
                Duration.ofMillis(expirationMillis)  // 토큰 만료 시간에 맞춰 설정
        );
    }

    // 액세스 토큰이 블랙리스트에 있는지 확인
    public boolean isAccessTokenBlacklisted(String token) {
        return redisTemplate.hasKey(getBlacklistKey(token));
    }

    // 블랙리스트에서 액세스 토큰 삭제
    public void removeAccessTokenFromBlacklist(String token) {
        redisTemplate.delete(getBlacklistKey(token));
    }

    // Key 생성: 블랙리스트
    private String getBlacklistKey(String token) {
        return BLACKLIST_PREFIX + token;
    }


    private String getKey(String email) {
        return "RT:" + email;
    }

}