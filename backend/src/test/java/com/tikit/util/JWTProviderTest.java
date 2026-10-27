package com.tikit.util;


import com.tikit.auth.entity.Member;
import com.tikit.auth.entity.Role;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

public class JWTProviderTest {

    private JWTProvider jwtProvider;
    private Member member;

    @BeforeEach
    void setup() {
        jwtProvider = new JWTProvider();

        // @Value 대신 secret 주입
        ReflectionTestUtils.setField(
                jwtProvider,
                "secret",
                "2sdgfdsk5342usdfjgnhsdfgi43512312312"
        );

        // init 메서드 호출로 secretKey 초기화
        ReflectionTestUtils.invokeMethod(jwtProvider, "init");

        // member 초기화
        member = Member.create(
                "test@example.com",
                "encodedPassword",
                "tester"
                , Role.MEMBER
        );

        ReflectionTestUtils.setField(member, "id", 1L);
    }

    @Test
    @DisplayName("AccessToken 내에 정보 확인")
    void generate_access_token() throws Exception {
        // given
        // setup에서 이미 셋팅

        // when
        String at = jwtProvider.generateAccessToken(member);

        // then
        Claims claims = jwtProvider.getClaims(at);

        assertThat(claims.getSubject()).isEqualTo("1");
        assertThat(claims.get("roleName", String.class)).isEqualTo(Role.MEMBER.toString());
        assertThat(claims.get("tokenType", String.class)).isEqualTo("ACCESS");
        assertThat(claims.getExpiration().getTime() - claims.getIssuedAt().getTime()).isEqualTo(10L * 60 * 1000);
    }






}
