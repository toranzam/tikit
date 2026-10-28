package com.tikit.rag.worker;

/**
 * 후속 청크 분할/임베딩/저장 구현의 연결 지점.
 * 구현체는 모든 청크 저장 후 성공 상태와 활성 버전을 트랜잭션으로 갱신해야 한다.
 * 실패 시 예외를 던지며, 외부 API 호출에는 타임아웃을 적용해야 한다.
 * 아직 구현체가 없으므로 자동 워커는 기본 비활성화 상태다.
 */
@FunctionalInterface
public interface RagIndexProcessor {
    void process(RagIndexTask task);
}
