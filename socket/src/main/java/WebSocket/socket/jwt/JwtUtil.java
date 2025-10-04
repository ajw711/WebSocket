package WebSocket.socket.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final long accessExpirationTime;
    private final long refreshExpirationTime;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-expiration-time}") long accessExpirationTime,
            @Value("${jwt.refresh-expiration-time}") long refreshExpirationTime
    ) {
        // 생성자에서 secretKey를 한 번만 초기화
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpirationTime = accessExpirationTime;
        this.refreshExpirationTime = refreshExpirationTime;
    }

    public enum TokenType {
        ACCESS_TOKEN,
        REFRESH_TOKEN
    }

    // ---------------- 토큰 생성 ----------------
    public String generateToken(Collection<? extends GrantedAuthority> authorities, Long memberId, TokenType tokenType) {
        List<String> authorityStrings = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        Instant now = Instant.now();
        long expirationMs = tokenType == TokenType.ACCESS_TOKEN ? accessExpirationTime : refreshExpirationTime;
        Instant expInstant = now.plusMillis(expirationMs);

        return Jwts.builder()
                .signWith(secretKey)
                .subject(memberId.toString())
                .claim("tokenType", tokenType.name())
                .claim("authorities", authorityStrings)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expInstant))
                .compact();
    }

    // ---------------- 유효성 검사 및 클레임 파싱 ----------------
    public Claims parseAndValidateToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.warn("토큰이 만료되었습니다: {}", e.getMessage());
            throw new ExpiredTokenException();
        } catch (JwtException | IllegalArgumentException e) {
            log.error("유효하지 않은 토큰입니다: {}", e.getMessage());
            throw new NotValidTokenException();
        }
    }

    // ---------------- 유틸 메서드 (클레임 재사용) ----------------
    public Long getMemberId(Claims claims) {
        return Long.parseLong(claims.getSubject());
    }

    public TokenType getTokenType(Claims claims) {
        String type = claims.get("tokenType", String.class);
        return TokenType.valueOf(type); // Enum의 name과 일치하므로 valueOf 사용
    }

    public Collection<? extends GrantedAuthority> getAuthorities(Claims claims) {
        List<String> authorities = claims.get("authorities", List.class);
        if (authorities == null) {
            return AuthorityUtils.NO_AUTHORITIES;
        }
        return AuthorityUtils.createAuthorityList(authorities.toArray(new String[0]));
    }

    public LocalDateTime getExpirationDateTime(Claims claims) {
        return claims.getExpiration().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    public void validateRefresh(Claims claims) {
        if (!TokenType.REFRESH_TOKEN.name().equals(claims.get("tokenType", String.class))) {
            throw new NotValidTokenException();
        }
    }

    // ---------------- 커스텀 예외 ----------------
    public static class ExpiredTokenException extends RuntimeException {}
    public static class NotValidTokenException extends RuntimeException {}
}