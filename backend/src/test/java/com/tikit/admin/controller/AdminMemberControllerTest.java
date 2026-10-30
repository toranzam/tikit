package com.tikit.admin.controller;

import com.tikit.admin.controller.advice.AdminMemberControllerAdvice;
import com.tikit.admin.service.AdminMemberService;
import com.tikit.auth.entity.Member;
import com.tikit.auth.entity.MemberStatus;
import com.tikit.auth.entity.Role;
import com.tikit.auth.repository.MemberRepository;
import com.tikit.common.advice.GlobalExceptionHandler;
import com.tikit.config.security.CustomAccessDeniedHandler;
import com.tikit.config.security.CustomAuthenticationEntryPoint;
import com.tikit.config.security.SecurityConfig;
import com.tikit.filter.JWTCheckFilter;
import com.tikit.util.JWTProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = AdminMemberControllerTest.TestConfig.class, properties = {
        "spring.datasource.url=jdbc:h2:mem:admin-member-test;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "jwt.secret=admin-member-test-only-secret-key-at-least-32-bytes"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminMemberControllerTest {

    // 회원 조회에 필요한 구성만 사용하고 운영 DB·Redis에는 연결하지 않습니다.
    @Configuration(proxyBeanMethods = false)
    @EnableAutoConfiguration
    @EntityScan(basePackageClasses = Member.class)
    @EnableJpaRepositories(basePackageClasses = MemberRepository.class)
    @EnableJpaAuditing
    @Import({AdminMemberController.class, AdminMemberService.class,
            AdminMemberControllerAdvice.class, GlobalExceptionHandler.class,
            SecurityConfig.class, JWTCheckFilter.class, JWTProvider.class,
            CustomAuthenticationEntryPoint.class, CustomAccessDeniedHandler.class})
    static class TestConfig {
    }

    @Autowired
    private MockMvc mvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private JWTProvider jwtProvider;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private Member alice;
    private Member bob;
    private Member manager;
    private Member withdrawn;
    private String adminToken;

    @BeforeEach
    void setUp() {
        prepareDatabase();
        alice = saveMember("alice@example.com", "앨리스", Role.MEMBER, MemberStatus.ACTIVE);
        bob = saveMember("bob@example.com", "밥", Role.MEMBER, MemberStatus.SUSPENDED);
        manager = saveMember("manager@example.com", "운영자", Role.MANAGER, MemberStatus.ACTIVE);
        withdrawn = saveMember("withdrawn@example.com", "탈퇴회원", Role.MEMBER, MemberStatus.WITHDRAWN);
        adminToken = token(Role.ADMIN);
    }

    // PostgreSQL 테스트에서는 테스트 데이터 저장 전에 세션 임시 테이블을 준비합니다.
    protected void prepareDatabase() {
    }

    @Test
    @DisplayName("회원 목록은 기본 페이지로 조회하고 민감정보를 제외한다")
    void listUsesDefaultPageAndExcludesSensitiveFields() throws Exception {
        performAsAdmin(get("/api/v1/admin/members"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(20))
                .andExpect(jsonPath("$.data.totalElements").value(4))
                .andExpect(jsonPath("$.data.totalPages").value(1))
                .andExpect(jsonPath("$.data.content.length()").value(4))
                .andExpect(content().string(not(containsString("passwordHash"))))
                .andExpect(content().string(not(containsString("customerKey"))))
                .andExpect(content().string(not(containsString("token"))))
                .andExpect(content().string(not(containsString("secret-password-hash"))));
    }

    @Test
    @DisplayName("가입 시각이 같으면 ID 내림차순으로 정렬하고 페이지를 나눈다")
    void listPaginatesWithStableIdOrderWhenCreationTimesMatch() throws Exception {
        jdbcTemplate.update("UPDATE members SET created_at = TIMESTAMP '2026-01-01 00:00:00'");

        performAsAdmin(get("/api/v1/admin/members").param("page", "1").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.size").value(2))
                .andExpect(jsonPath("$.data.totalElements").value(4))
                .andExpect(jsonPath("$.data.totalPages").value(2))
                .andExpect(jsonPath("$.data.first").value(false))
                .andExpect(jsonPath("$.data.last").value(true))
                .andExpect(jsonPath("$.data.content[0].id").value(bob.getId()))
                .andExpect(jsonPath("$.data.content[1].id").value(alice.getId()));
    }

    @Test
    @DisplayName("이메일 검색은 대소문자를 구분하지 않고 검색어 앞뒤 공백을 제거한다")
    void searchMatchesEmailIgnoringCaseAndTrimsWhitespace() throws Exception {
        performAsAdmin(get("/api/v1/admin/members").param("keyword", "  ALICE@EXAMPLE  "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].id").value(alice.getId()));
    }

    @Test
    @DisplayName("이름 검색과 상태·역할 필터를 함께 적용한다")
    void searchMatchesNameAndCombinesFilters() throws Exception {
        performAsAdmin(get("/api/v1/admin/members")
                .param("keyword", "운영").param("role", "MANAGER").param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].id").value(manager.getId()));

        performAsAdmin(get("/api/v1/admin/members")
                .param("keyword", "운영").param("status", "SUSPENDED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }

    @Test
    @DisplayName("검색어와 역할 없이 회원 상태만으로 조회할 수 있다")
    void statusFilterWorksWithoutKeywordOrRole() throws Exception {
        performAsAdmin(get("/api/v1/admin/members").param("status", "SUSPENDED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].id").value(bob.getId()));
    }

    @Test
    @DisplayName("검색어와 상태 없이 회원 역할만으로 조회할 수 있다")
    void roleFilterWorksWithoutKeywordOrStatus() throws Exception {
        performAsAdmin(get("/api/v1/admin/members").param("role", "MANAGER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].id").value(manager.getId()));
    }

    @ParameterizedTest
    @DisplayName("검색어의 LIKE 특수문자는 입력한 문자 그대로 검색한다")
    @ValueSource(strings = {"%", "_", "!"})
    void searchTreatsLikeSpecialCharactersAsLiterals(String keyword) throws Exception {
        Member literal = saveMember("literal@example.com", "문자" + keyword, Role.MEMBER, MemberStatus.ACTIVE);

        performAsAdmin(get("/api/v1/admin/members").param("keyword", keyword))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].id").value(literal.getId()));
    }

    @Test
    @DisplayName("검색어가 공백이면 전체 회원을 조회한다")
    void blankKeywordListsAllMembers() throws Exception {
        performAsAdmin(get("/api/v1/admin/members").param("keyword", "   "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(4));
    }

    @Test
    @DisplayName("마지막 페이지를 넘겨 조회하면 빈 목록과 전체 회원 수를 반환한다")
    void pagePastEndReturnsEmptyContentWithTotal() throws Exception {
        performAsAdmin(get("/api/v1/admin/members").param("page", "10").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(0))
                .andExpect(jsonPath("$.data.totalElements").value(4));
    }

    @Test
    @DisplayName("회원 상세는 상태·일시 정보를 포함하고 민감정보를 제외한다")
    void detailIncludesStatusAndDatesWithoutSensitiveFields() throws Exception {
        performAsAdmin(get("/api/v1/admin/members/{id}", withdrawn.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(withdrawn.getId()))
                .andExpect(jsonPath("$.data.email").value("withdrawn@example.com"))
                .andExpect(jsonPath("$.data.name").value("탈퇴회원"))
                .andExpect(jsonPath("$.data.role").value("MEMBER"))
                .andExpect(jsonPath("$.data.status").value("WITHDRAWN"))
                .andExpect(jsonPath("$.data.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.data.updatedAt").isNotEmpty())
                .andExpect(jsonPath("$.data.withdrawnAt").isNotEmpty())
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.data.customerKey").doesNotExist())
                .andExpect(content().string(not(containsString("secret-password-hash"))))
                .andExpect(content().string(not(containsString("token"))));
    }

    @Test
    @DisplayName("존재하지 않는 회원 상세 조회는 404 RESOURCE_NOT_FOUND를 반환한다")
    void missingMemberReturnsCommonNotFoundResponse() throws Exception {
        performAsAdmin(get("/api/v1/admin/members/999999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @ParameterizedTest
    @DisplayName("비로그인 사용자의 회원 목록·상세 조회는 401을 반환한다")
    @ValueSource(strings = {"/api/v1/admin/members", "/api/v1/admin/members/1"})
    void anonymousRequestsReturn401(String path) throws Exception {
        mvc.perform(get(path))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));
    }

    @ParameterizedTest
    @DisplayName("MEMBER와 MANAGER의 회원 목록·상세 조회는 403을 반환한다")
    @EnumSource(value = Role.class, names = {"MEMBER", "MANAGER"})
    void nonAdminRequestsReturn403(Role role) throws Exception {
        String accessToken = token(role);
        for (String path : new String[]{"/api/v1/admin/members", "/api/v1/admin/members/1"}) {
            mvc.perform(get(path).header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
        }
    }

    @ParameterizedTest
    @DisplayName("잘못된 검색 조건과 페이지 값은 400 VALIDATION_ERROR를 반환한다")
    @CsvSource({"page,-1", "page,abc", "page,2147483648", "size,0", "size,101",
            "size,abc", "role,SELLER", "status,DELETED"})
    void invalidSearchParametersReturnCommonValidationResponse(String parameter, String value) throws Exception {
        performAsAdmin(get("/api/v1/admin/members").param(parameter, value))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("검색어가 254자를 초과하면 400 VALIDATION_ERROR를 반환한다")
    void tooLongKeywordReturnsValidationError() throws Exception {
        performAsAdmin(get("/api/v1/admin/members").param("keyword", "a".repeat(255)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @ParameterizedTest
    @DisplayName("회원 ID가 양의 정수 형식이나 Long 범위를 벗어나면 400을 반환한다")
    @ValueSource(strings = {"0", "-1", "abc", "9223372036854775808"})
    void invalidMemberIdReturnsCommonValidationResponse(String id) throws Exception {
        performAsAdmin(get("/api/v1/admin/members/" + id))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    private ResultActions performAsAdmin(MockHttpServletRequestBuilder request) throws Exception {
        return mvc.perform(request.header("Authorization", "Bearer " + adminToken));
    }

    private Member saveMember(String email, String name, Role role, MemberStatus status) {
        Member member = Member.create(email, "secret-password-hash", name, role);
        ReflectionTestUtils.setField(member, "status", status);
        if (status == MemberStatus.WITHDRAWN) {
            ReflectionTestUtils.setField(member, "withdrawnAt", Instant.now());
        }
        return memberRepository.saveAndFlush(member);
    }

    private String token(Role role) {
        Member member = Member.create("token@example.com", "unused", "인증 사용자", role);
        ReflectionTestUtils.setField(member, "id", 999999L);
        return jwtProvider.generateAccessToken(member);
    }
}
