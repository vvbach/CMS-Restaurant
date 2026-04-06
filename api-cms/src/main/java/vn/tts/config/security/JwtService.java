package vn.tts.config.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import vn.tts.enums.TokenTypeEnum;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Service
@Slf4j
@RequiredArgsConstructor
public class JwtService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${sso.secret}")
    private String SECRET;

    @Value("${sso.expiration.token}")
    private int expirationToken;

    @Value("${sso.expiration.reset_token}")
    private int expirationResetToken;

    @Value("${sso.expiration.change_password_token}")
    private int expirationChangePasswordToken;

    private static final String KEY_REDIS_TOKEN = "auth:access:%s";
    private static final String KEY_REDIS_REF_TOKEN = "auth:refresh:%s";

    //  Token generation

    public String generateToken(String userId) {
        Map<String, Object> claims = Map.of("type", TokenTypeEnum.ACCESS);
        String token = createToken(claims, userId, expirationToken, TimeUnit.MINUTES);
        storeTokenInRedis(String.format(KEY_REDIS_TOKEN, userId), token, expirationToken, TimeUnit.MINUTES);
        return token;
    }

    public String generateRefreshToken(String userId) {
        Map<String, Object> claims = Map.of("type", TokenTypeEnum.REFRESH);
        String token = createToken(claims, userId, expirationResetToken, TimeUnit.DAYS);
        storeTokenInRedis(String.format(KEY_REDIS_REF_TOKEN, userId), token, expirationResetToken, TimeUnit.DAYS);
        return token;
    }

    public String generateChangePasswordToken(String userId) {
        Map<String, Object> claims = Map.of("type", TokenTypeEnum.CHANGE_PASSWORD);
        return createToken(claims, userId, expirationChangePasswordToken, TimeUnit.HOURS);
    }

    // Token creation

    private String createToken(Map<String, Object> claims, String subject, int duration, TimeUnit unit) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + unit.toMillis(duration));

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSignKey())
                .compact();
    }

    private void storeTokenInRedis(String key, String token, int duration, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, token, duration, unit);
    }

    // Token extraction

    public String extractAccountId(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public String extractTokenType(String token) {
        return extractClaim(token, claims -> claims.get("type", String.class));
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(extractAllClaims(token));
    }

    public String extractTokenFromHeader(String authHeader) {
        if (authHeader == null || authHeader.isBlank()) return null;
        if (authHeader.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return authHeader.substring(7).trim();
        }
        return null;
    }

    // Token validation

    public boolean validateAccessToken(String token) {
        return validateToken(token, TokenTypeEnum.ACCESS, KEY_REDIS_TOKEN);
    }

    public boolean validateRefreshToken(String token) {
        return validateToken(token, TokenTypeEnum.REFRESH, KEY_REDIS_REF_TOKEN);
    }

    public boolean validateChangePasswordToken(String token) {
        if (!validateToken(token, TokenTypeEnum.CHANGE_PASSWORD, null)) return false;
        return !extractExpiration(token).before(new Date());
    }

    private boolean validateToken(String token, TokenTypeEnum type, String redisKeyTemplate) {
        if (token == null) return false;

        try {
            Jwts.parser().verifyWith(getSignKey()).build().parseSignedClaims(token);
        } catch (Exception e) {
            return false;
        }

        boolean typeMatches = extractTokenType(token).equals(type.toString());
        boolean expired = extractExpiration(token).before(new Date());

        if (!typeMatches || expired) return false;

        if (redisKeyTemplate != null) {
            String redisToken = Objects.requireNonNull(
                    redisTemplate.opsForValue().get(String.format(redisKeyTemplate, extractAccountId(token)))
            ).toString();
            return token.equals(redisToken);
        }

        return true;
    }

    // Session management

    public void deleteSession(UUID userId) {
        if (userId == null) return;
        redisTemplate.delete(String.format(KEY_REDIS_TOKEN, userId));
        redisTemplate.delete(String.format(KEY_REDIS_REF_TOKEN, userId));
    }

    // Helper functions

    private SecretKey getSignKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSignKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (SignatureException e) {
            throw new RuntimeException("Chữ ký Token không hợp lệ");
        } catch (ExpiredJwtException e) {
            throw new RuntimeException("Token đã hết hạn");
        } catch (Exception e) {
            throw new RuntimeException("Token không hợp lệ hoặc đã bị hỏng");
        }
    }
}
