# RAG 인덱싱 (이슈 #23, 1차 작업)

## 임베딩 결정

- 제공자: OpenAI
- 모델: `text-embedding-3-small`
- 차원: `1536` (모델 기본값)
- DB: `rag_chunks.embedding vector(1536)` (`V3` 마이그레이션)
- 키: `OPENAI_API_KEY` 환경변수 → `rag.embedding.api-key`

초기 정책 문서 검색용으로 small 모델의 기본 차원을 사용한다. 별도 모델 서버 없이
API를 연결할 수 있고, 차원 축소를 도입하지 않아 설정과 저장 스키마를 일치시키기 쉽다.
한국어 정책 문서 검색 품질은 후속 검색 구현에서 실제 질문으로 평가한다.
모델 또는 차원 변경 시 새 마이그레이션과 기존 문서 재인덱싱이 필요하다.
기존 청크에 1536차원 이외 벡터가 있으면 V3는 실패하므로 데이터를 점검해야 한다.

공식 문서: https://developers.openai.com/api/docs/guides/embeddings

## 워커 흐름

1. `created_at, id` 순으로 QUEUED 작업 ID를 배치 크기만큼 조회한다.
2. 각 작업을 조건부 UPDATE (`status = QUEUED`)로 선점한다.
3. 같은 트랜잭션에서 작업/문서 버전을 RUNNING으로 바꾸고 시도 횟수 및 임베딩 설정을 기록한다.
4. 트랜잭션 커밋 후 본문 스냅샷을 `RagIndexProcessor`에 전달한다.
5. 처리기가 예외를 던지면 별도 트랜잭션에서 작업/버전을 FAILED로 기록한다.
   실패 코드는 `INDEX_PROCESSING_FAILED`이며 기존 활성 버전은 유지한다.
6. 한 작업이 실패해도 같은 배치의 다음 작업은 처리한다.

중복 선점 시 UPDATE 결과가 0인 워커는 해당 작업을 건너뛴다.
외부 API 호출 동안 선점 트랜잭션이나 DB 잠금을 유지하지 않는다.
일반 예외 메시지에는 API 키나 본문이 포함될 수 있으므로 실패 코드와 예외 유형만 기록한다.

## 실행 설정과 후속 연결

현재는 `rag.indexing.enabled=false`가 기본값이다. **청크 분할·임베딩 API·청크 저장·
성공 상태 및 활성 버전 갱신은 아직 구현하지 않았다.** 성공으로 처리하는 임시 구현체도 없다.

후속 작업에서 `RagIndexProcessor` 구현체를 Spring Bean으로 등록한 뒤
`RAG_INDEXING_ENABLED=true`로 활성화한다. 처리기 없이 활성화하면 의존성 주입 단계에서
시작에 실패하여 대기 작업을 소진하지 않는다. 처리기는 모든 청크를 저장한 경우에만
작업 SUCCEEDED, 버전 READY, 문서 activeVersion을 한 트랜잭션에서 갱신해야 한다.

| 환경변수 | 기본값 | 의미 |
| --- | --- | --- |
| `OPENAI_API_KEY` | 빈 값 | 후속 임베딩 API 구현에서 사용 |
| `RAG_INDEXING_ENABLED` | `false` | 자동 폴링 활성화 |
| `RAG_INDEXING_BATCH_SIZE` | `10` | 폴링당 조회할 작업 수 (1~100) |
| `RAG_INDEXING_POLL_INTERVAL_MS` | `5000` | 배치 종료 후 다음 폴링까지 대기 시간 |

현재 선점은 QUEUED → RUNNING 전환만 지원한다. 서버 강제 종료 후 RUNNING 작업 복구,
lease 갱신/만료 회수, 재시도 정책은 후속 작업이며 `lease_until`은 아직 사용하지 않는다.
자동 재시도는 하지 않는다. 선점 트랜잭션 자체가 실패하면 롤백되어 QUEUED로 남는다.

## 검증

`./gradlew test --tests 'com.tikit.rag.*'`

H2 기반 실제 JPA 트랜잭션 테스트로 동시 선점, 상태 커밋/롤백, 실패 기록을 검증한다.
pgvector 타입 변경은 PostgreSQL과 pgvector가 있는 환경에서 Flyway 적용을 별도로 확인한다.
