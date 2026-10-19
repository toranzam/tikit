package com.tikit.util;

import com.tikit.auth.entity.Account;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Component
public class JWTProvider {

    @Value("${jwt.secret}")
    private String secret;

    private SecretKey secretKey;

    private static final long ACCESS_TOKEN_EXPIRATION_MILLIS = 10L * 60 * 1000; // 10분
    private static final long REFRESH_TOKEN_EXPIRATION_MILLIS = 60L * 60 * 1000; // 60분

    @PostConstruct
    private void init() {
        secretKey = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateAccessToken(Account account) {
        Date now = new Date() ;
        Date expire = new Date(now.getTime() + (long) ACCESS_TOKEN_EXPIRATION_MILLIS);

        return Jwts.builder()
                .subject(account.getAccountId().toString())
                .claim("email", account.getEmail())
                .claim("roleName", account.getRoleName().toString())
                .claim("tokenType", "ACCESS")
                .issuedAt(now)
                .expiration(expire)
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    public String generateRefreshToken(Account account) {
        Date now = new Date() ;
        Date expire = new Date(now.getTime() + (long) REFRESH_TOKEN_EXPIRATION_MILLIS);

        return Jwts.builder()
                .subject(account.getAccountId().toString())
                .claim("tokenType", "REFRESH")
                .id(UUID.randomUUID().toString())
                .issuedAt(now)
                .expiration(expire)
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isExpired(String token) {
        try {
            getClaims(token);
            return false;
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

}
