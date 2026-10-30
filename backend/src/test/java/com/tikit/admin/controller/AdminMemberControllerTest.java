package com.tikit.admin.controller;

import com.tikit.admin.controller.advice.AdminMemberControllerAdvice;
import com.tikit.admin.service.AdminMemberService;
import com.tikit.admin.entity.AuditLog;
import com.tikit.admin.repository.AuditLogRepository;
import com.tikit.auth.dto.request.MemberLoginRequestDTO;
import com.tikit.auth.exception.AccountRestrictedException;
import com.tikit.auth.redis.RefreshTokenRepository;
import com.tikit.auth.service.MemberService;
import com.tikit.auth.entity.Member;
import com.tikit.auth.entity.MemberStatus;
import com.tikit.auth.entity.Role;
import com.tikit.auth.repository.MemberRepository;
import com.tikit.common.advice.GlobalExceptionHandler;
import com.tikit.config.security.CustomAccessDeniedHandler;
import com.tikit.config.security.CustomAuthenticationEntryPoint;
import com.tikit.config.security.SecurityConfig;
import com.tikit.config.security.CustomUserDetails;
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
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import jakarta.persistence.EntityManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.anyString;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Transactional
abstract class AdminMemberControllerTest {

    // 회원 조회에 필요한 구성만 사용하고 운영 DB·Redis에는 연결하지 않습니다.
    @Configuration(proxyBeanMethods = false)
    @EnableAutoConfiguration
    @EntityScan(basePackageClasses = {Member.class, AuditLog.class})
    @EnableJpaRepositories(basePackageClasses = {MemberRepository.class, AuditLogRepository.class})
    @EnableJpaAuditing
    @Import({AdminMemberController.class, AdminMemberService.class, MemberService.class,
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

    @MockitoBean
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private MemberService memberService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EntityManager entityManager;

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

    // 팀 PostgreSQL 테스트 클래스에서 테스트 데이터 저장 전에 임시 테이블을 준비합니다.
    protected abstract void prepareDatabase();

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

    @Test
    @DisplayName("활성 회원을 정지하고 처리자·시각·사유·전후 상태를 저장한다")
    void suspendMemberAndRecordAuditLog() throws Exception {
        Member admin = usePersistedAdmin();
        changeStatus(alice.getId(), "SUSPENDED", "  운영 정책 위반  ")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SUSPENDED"));
        entityManager.clear();

        assertThat(memberRepository.findById(alice.getId()).orElseThrow().getStatus())
                .isEqualTo(MemberStatus.SUSPENDED);
        AuditLog log = auditLogRepository.findAll().getFirst();
        assertThat(log.getActorMemberId()).isEqualTo(admin.getId());
        assertThat(log.getTargetType()).isEqualTo("MEMBER");
        assertThat(log.getTargetId()).isEqualTo(alice.getId());
        assertThat(log.getAction()).isEqualTo("MEMBER_SUSPEND");
        assertThat(log.getReason()).isEqualTo("운영 정책 위반");
        assertThat(log.getBeforeState()).containsEntry("status", "ACTIVE");
        assertThat(log.getAfterState()).containsEntry("status", "SUSPENDED");
        assertThat(log.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("정지 회원을 활성 상태로 복구하고 해제 이력을 저장한다")
    void resumeSuspendedMember() throws Exception {
        usePersistedAdmin();
        changeStatus(bob.getId(), "ACTIVE", "정지 해제 승인")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
        entityManager.clear();

        assertThat(memberRepository.findById(bob.getId()).orElseThrow().getStatus())
                .isEqualTo(MemberStatus.ACTIVE);
        AuditLog log = auditLogRepository.findAll().getFirst();
        assertThat(log.getAction()).isEqualTo("MEMBER_RESUME");
        assertThat(log.getBeforeState()).containsEntry("status", "SUSPENDED");
        assertThat(log.getAfterState()).containsEntry("status", "ACTIVE");
    }

    @Test
    @DisplayName("MANAGER 회원도 정지와 해제가 가능하고 역할은 유지된다")
    void managerStatusCanChangeWithoutChangingRole() throws Exception {
        usePersistedAdmin();
        changeStatus(manager.getId(), "SUSPENDED", "운영 정책 위반")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.role").value("MANAGER"))
                .andExpect(jsonPath("$.data.status").value("SUSPENDED"));
        changeStatus(manager.getId(), "ACTIVE", "조치 완료")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.role").value("MANAGER"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
        assertThat(auditLogRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("이미 정지된 회원을 다시 정지하거나 활성 회원을 해제하면 409를 반환한다")
    void duplicateTransitionsAreRejectedWithoutAuditLog() throws Exception {
        usePersistedAdmin();
        changeStatus(bob.getId(), "SUSPENDED", "중복 정지")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("STATE_CONFLICT"));
        changeStatus(alice.getId(), "ACTIVE", "중복 해제")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("STATE_CONFLICT"));
        assertThat(auditLogRepository.count()).isZero();
    }

    @ParameterizedTest
    @DisplayName("탈퇴 회원은 정지하거나 활성 상태로 복구할 수 없다")
    @ValueSource(strings = {"SUSPENDED", "ACTIVE"})
    void withdrawnMemberCannotChangeStatus(String newStatus) throws Exception {
        usePersistedAdmin();
        changeStatus(withdrawn.getId(), newStatus, "탈퇴 계정 변경 시도")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("STATE_CONFLICT"));
        assertThat(memberRepository.findById(withdrawn.getId()).orElseThrow().getStatus())
                .isEqualTo(MemberStatus.WITHDRAWN);
        assertThat(auditLogRepository.count()).isZero();
    }

    @Test
    @DisplayName("상태 변경 API로 회원을 탈퇴 처리할 수 없다")
    void requestedWithdrawalIsRejected() throws Exception {
        usePersistedAdmin();
        changeStatus(alice.getId(), "WITHDRAWN", "잘못된 전환")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("STATE_CONFLICT"));
        assertThat(alice.getStatus()).isEqualTo(MemberStatus.ACTIVE);
        assertThat(auditLogRepository.count()).isZero();
    }

    @Test
    @DisplayName("자기 계정과 다른 관리자 계정의 상태 변경은 403으로 차단한다")
    void adminAccountsAreProtected() throws Exception {
        Member admin = usePersistedAdmin();
        Member other = saveMember("other-admin@example.com", "다른 관리자", Role.ADMIN, MemberStatus.ACTIVE);
        for (Long id : new Long[]{admin.getId(), other.getId()}) {
            changeStatus(id, "SUSPENDED", "관리자 정지 시도")
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("ADMIN_ACCOUNT_PROTECTED"));
        }
        assertThat(auditLogRepository.count()).isZero();
    }

    @Test
    @DisplayName("존재하지 않는 회원 상태 변경은 404를 반환한다")
    void missingStatusChangeTargetReturns404() throws Exception {
        usePersistedAdmin();
        changeStatus(999999999L, "SUSPENDED", "대상 없음")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
        assertThat(auditLogRepository.count()).isZero();
    }

    @ParameterizedTest
    @DisplayName("잘못된 상태 변경 본문은 400을 반환한다")
    @ValueSource(strings = {"{}", "{\"status\":\"DELETED\",\"reason\":\"사유\"}",
            "{\"status\":\"SUSPENDED\"}", "{\"status\":\"SUSPENDED\",\"reason\":\"   \"}",
            "{\"status\":null,\"reason\":\"사유\"}", "not-json"})
    void invalidStatusChangeBodyReturns400(String body) throws Exception {
        usePersistedAdmin();
        performAsAdmin(patch("/api/v1/admin/members/{id}/status", alice.getId())
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        assertThat(auditLogRepository.count()).isZero();
    }

    @Test
    @DisplayName("1000자를 초과하는 상태 변경 사유는 400을 반환한다")
    void tooLongStatusChangeReasonReturns400() throws Exception {
        usePersistedAdmin();
        changeStatus(alice.getId(), "SUSPENDED", "a".repeat(1001))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        assertThat(auditLogRepository.count()).isZero();
    }

    @Test
    @DisplayName("비로그인 회원 상태 변경은 401을 반환한다")
    void anonymousStatusChangeReturns401() throws Exception {
        mvc.perform(patch("/api/v1/admin/members/{id}/status", alice.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"SUSPENDED\",\"reason\":\"사유\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));
        assertThat(auditLogRepository.count()).isZero();
    }

    @ParameterizedTest
    @DisplayName("MEMBER와 MANAGER의 회원 상태 변경은 403을 반환한다")
    @EnumSource(value = Role.class, names = {"MEMBER", "MANAGER"})
    void nonAdminStatusChangeReturns403(Role role) throws Exception {
        mvc.perform(patch("/api/v1/admin/members/{id}/status", alice.getId())
                .header("Authorization", "Bearer " + token(role))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"SUSPENDED\",\"reason\":\"사유\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
        assertThat(auditLogRepository.count()).isZero();
    }

    @Test
    @DisplayName("기존 ADMIN 토큰이 있어도 정지된 관리자는 상태를 변경할 수 없다")
    void suspendedAdminCannotChangeStatus() throws Exception {
        Member admin = usePersistedAdmin();
        ReflectionTestUtils.setField(admin, "status", MemberStatus.SUSPENDED);
        memberRepository.flush();
        changeStatus(alice.getId(), "SUSPENDED", "권한 차단 확인")
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCOUNT_RESTRICTED"));
        assertThat(auditLogRepository.count()).isZero();
    }

    @Test
    @DisplayName("정지 처리 결과가 로그인 차단에 반영되고 해제 후 다시 로그인할 수 있다")
    void suspensionAndResumeAreReflectedInLogin() throws Exception {
        usePersistedAdmin();
        String password = "Example-password-2026!";
        ReflectionTestUtils.setField(alice, "passwordHash", passwordEncoder.encode(password));
        memberRepository.flush();
        when(userDetailsService.loadUserByUsername(alice.getEmail())).thenAnswer(invocation ->
                new CustomUserDetails(memberRepository.findByEmail(alice.getEmail()).orElseThrow()));
        MemberLoginRequestDTO request = new MemberLoginRequestDTO(alice.getEmail(), password);
        assertThat(memberService.login(request).getAccessToken()).isNotBlank();

        changeStatus(alice.getId(), "SUSPENDED", "정지 승인").andExpect(status().isOk());
        entityManager.clear();
        assertThatThrownBy(() -> memberService.login(request)).isInstanceOf(AccountRestrictedException.class);

        changeStatus(alice.getId(), "ACTIVE", "해제 승인").andExpect(status().isOk());
        entityManager.clear();
        assertThat(memberService.login(request).getAccessToken()).isNotBlank();
    }

    @Test
    @DisplayName("정지 전에 발급한 리프레시 토큰도 재발급 시 차단하고 저장된 세션을 삭제한다")
    void suspensionBlocksExistingRefreshToken() throws Exception {
        usePersistedAdmin();
        String refreshToken = jwtProvider.generateRefreshToken(alice);
        changeStatus(alice.getId(), "SUSPENDED", "정지 승인").andExpect(status().isOk());
        entityManager.clear();

        assertThatThrownBy(() -> memberService.refresh(refreshToken))
                .isInstanceOf(AccountRestrictedException.class);
        verify(refreshTokenRepository).delete(alice.getId().toString());
        verify(refreshTokenRepository, never()).rotate(anyString(), anyString(), anyString());
    }

    private Member usePersistedAdmin() {
        Member admin = saveMember("admin@example.com", "관리자", Role.ADMIN, MemberStatus.ACTIVE);
        adminToken = jwtProvider.generateAccessToken(admin);
        return admin;
    }

    private ResultActions changeStatus(Long memberId, String newStatus, String reason) throws Exception {
        return performAsAdmin(patch("/api/v1/admin/members/{id}/status", memberId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"" + newStatus + "\",\"reason\":\"" + reason + "\"}"));
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
