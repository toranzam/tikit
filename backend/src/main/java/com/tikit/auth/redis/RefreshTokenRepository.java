package com.tikit.auth.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {

    private final RedisTemplate<String, String> redisTemplate;
    private static final long REFRESH_TOKEN_TTL = 60L; // 60분
    private static final String PREFIX = "refresh:";

    // 로그인, 재발급에 사용
    public void save(String accountId, String refreshToken) {
        redisTemplate.opsForValue()
                .set(PREFIX + accountId, refreshToken, Duration.ofMinutes(REFRESH_TOKEN_TTL));
    }

    // 검증에 사용
    public Optional<String> find(String accountId) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(PREFIX + accountId));
    }

    // 로그아웃에 사용
    public void delete(String accountId) {
        redisTemplate.delete(PREFIX + accountId);
    }
}
