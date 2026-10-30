package com.tikit.admin.controller;

import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 명시적으로 지정한 팀 PostgreSQL에서 회원 조회·상태 변경 API 테스트를 실행합니다.
 * 실제 테이블의 구조만 읽으며 데이터 쓰기는 세션 임시 테이블로 제한합니다.
 */
@EnabledIfEnvironmentVariable(named = "TEAM_DB_URL", matches = "jdbc:postgresql:.*")
@SpringBootTest(classes = AdminMemberControllerTest.TestConfig.class, properties = {
        "spring.datasource.url=${TEAM_DB_URL}",
        "spring.datasource.driver-class-name=org.postgresql.Driver",
        "spring.datasource.username=${TEAM_DB_USERNAME}",
        "spring.datasource.password=${TEAM_DB_PASSWORD}",
        "spring.datasource.hikari.data-source-properties.sslmode=${TEAM_DB_SSLMODE:verify-full}",
        "spring.datasource.hikari.data-source-properties.sslrootcert=${TEAM_DB_CA_CERT:}",
        "spring.datasource.hikari.connection-timeout=10000",
        "spring.datasource.hikari.maximum-pool-size=1",
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.jpa.properties.hibernate.session_factory.statement_inspector=com.tikit.admin.controller.AdminMemberPostgresqlTest$TemporaryMemberTableInspector",
        "jwt.secret=admin-member-test-only-secret-key-at-least-32-bytes"
})
@ActiveProfiles(value = "team-member-test", inheritProfiles = false)
class AdminMemberPostgresqlTest extends AdminMemberControllerTest {

    @Autowired
    private JdbcTemplate teamJdbcTemplate;

    @Override
    protected void prepareDatabase() {
        assertThat(teamJdbcTemplate.queryForObject(
                "SELECT version()", String.class)).startsWith("PostgreSQL");

        // 초기 스키마 검증은 public에서 마친 뒤 테스트 트랜잭션에서만 검색 경로를 바꿉니다.
        teamJdbcTemplate.execute("SET LOCAL search_path TO pg_temp, public");

        // INCLUDING IDENTITY로 임시 테이블만의 시퀀스를 생성합니다.
        // INCLUDING DEFAULTS는 원본 시퀀스를 참조할 수 있어 사용하지 않습니다.
        teamJdbcTemplate.execute("""
                CREATE TEMPORARY TABLE members (
                    LIKE public.members INCLUDING CONSTRAINTS INCLUDING INDEXES INCLUDING IDENTITY
                ) ON COMMIT DROP
                """);

        teamJdbcTemplate.execute("""
                CREATE TEMPORARY TABLE audit_logs (
                    LIKE public.audit_logs INCLUDING CONSTRAINTS INCLUDING INDEXES INCLUDING IDENTITY
                ) ON COMMIT DROP
                """);
        // 감사 이력의 처리자 FK도 임시 회원 테이블을 참조하게 합니다.
        teamJdbcTemplate.execute("""
                ALTER TABLE pg_temp.audit_logs ADD FOREIGN KEY (actor_member_id)
                REFERENCES pg_temp.members(id)
                """);

        // JPA/JDBC가 public.members 대신 임시 members를 사용하는지 쓰기 전에 확인합니다.
        assertThat(teamJdbcTemplate.queryForObject("""
                SELECT to_regclass('members') = to_regclass('pg_temp.members')
                """, Boolean.class)).isTrue();
        assertThat(teamJdbcTemplate.queryForObject("""
                SELECT to_regclass('audit_logs') = to_regclass('pg_temp.audit_logs')
                """, Boolean.class)).isTrue();

        // 명시적인 테스트 데이터 쓰기가 public 테이블로 향하지 않도록 참조 FK가 없는지도 확인합니다.
        assertThat(teamJdbcTemplate.queryForObject("""
                SELECT count(*) FROM pg_constraint
                WHERE conrelid = 'pg_temp.members'::regclass AND contype = 'f'
                """, Long.class)).isZero();
    }

    // JPA SQL도 임시 테이블을 명시적으로 사용하게 하여 원본 데이터 쓰기를 방지합니다.
    public static class TemporaryMemberTableInspector implements StatementInspector {

        private static final Pattern MEMBER_TABLE = Pattern.compile(
                "(?i)\\b(from|into|update|join)\\s+(?:(?:public|pg_temp)\\.)?(members|audit_logs)\\b");

        @Override
        public String inspect(String sql) {
            return MEMBER_TABLE.matcher(sql).replaceAll("$1 pg_temp.$2");
        }
    }
}
