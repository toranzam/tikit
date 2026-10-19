package com.tikit.auth.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.List;
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

    private static final DefaultRedisScript<Long> ROTATE_SCRIPT =
            new DefaultRedisScript<>("""
                    local savedToken = redis.call('GET', KEYS[1])
                    
                    if not savedToken then
                        return 0
                    end
                    
                    if savedToken ~= ARGV[1] then
                        redis.call('DEL', KEYS[1])
                        return -1
                    end
                    
                    redis.call('SET', KEYS[1], ARGV[2], 'EX', ARGV[3])
                    return 1
                    """, Long.class);


    public long rotate(
            String accountId,
            String oldRefreshToken,
            String newRefreshToken
    ) {
        Long result = redisTemplate.execute(
                ROTATE_SCRIPT,
                List.of(PREFIX + accountId),
                oldRefreshToken,
                newRefreshToken,
                Long.toString(
                        Duration.ofMinutes(REFRESH_TOKEN_TTL).toSeconds()
                )
        );

        if (result == null) {
            throw new IllegalStateException("Refresh Token 교체 결과가 없습니다.");
        }

        return result;
    }
}
