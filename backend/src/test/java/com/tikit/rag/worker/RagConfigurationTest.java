package com.tikit.rag.worker;

import com.tikit.rag.config.RagConfiguration;
import com.tikit.rag.config.RagEmbeddingProperties;
import com.tikit.rag.config.RagIndexingProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.io.support.ResourcePropertySource;

import java.io.IOException;
import java.io.UncheckedIOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class RagConfigurationTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(RagConfiguration.class, RagIndexWorker.class)
            .withBean(RagIndexJobService.class, () -> mock(RagIndexJobService.class))
            .withInitializer(context -> {
                try {
                    // 운영 환경과 같이 개별 환경 설정이 공통 기본값보다 우선한다.
                    context.getEnvironment().getPropertySources()
                            .addLast(new ResourcePropertySource("classpath:application.properties"));
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });

    @Test
    @DisplayName("기본 임베딩 설정은 small 모델과 1536차원이며 자동 워커는 비활성화된다")
    void bindsDefaultConfigurationWithoutStartingWorker() {
        runner.run(context -> {
            assertThat(context).hasNotFailed().doesNotHaveBean(RagIndexWorker.class);
            var embedding = context.getBean(RagEmbeddingProperties.class);
            assertThat(embedding.model()).isEqualTo("text-embedding-3-small");
            assertThat(embedding.dimensions()).isEqualTo(1536);
            assertThat(context.getBean(RagIndexingProperties.class).enabled()).isFalse();
        });
    }

    @Test
    @DisplayName("DB 차원과 다른 임베딩 차원이나 미지원 모델을 설정하면 시작에 실패한다")
    void rejectsUnsupportedEmbeddingConfiguration() {
        runner.withPropertyValues("rag.embedding.dimensions=3072")
                .run(context -> assertThat(context).hasFailed());
        runner.withPropertyValues("rag.embedding.model=text-embedding-3-large")
                .run(context -> assertThat(context).hasFailed());
    }

    @Test
    @DisplayName("배치 크기나 폴링 간격이 유효하지 않으면 시작에 실패한다")
    void rejectsInvalidWorkerSettings() {
        for (String property : new String[]{"rag.indexing.batch-size=0", "rag.indexing.batch-size=101",
                "rag.indexing.poll-interval-ms=0"}) {
            runner.withPropertyValues(property).run(context -> assertThat(context).hasFailed());
        }
    }

    @Test
    @DisplayName("처리기 없이 워커를 활성화하면 시작에 실패하여 대기 작업을 소진하지 않는다")
    void enablingWorkerRequiresProcessor() {
        runner.withPropertyValues("rag.indexing.enabled=true")
                .run(context -> assertThat(context).hasFailed());
    }

    @Test
    @DisplayName("처리기를 연결하고 워커를 활성화하면 워커 빈이 등록된다")
    void enabledWorkerWithProcessorStarts() {
        runner.withPropertyValues("rag.indexing.enabled=true")
                .withBean(RagIndexProcessor.class, () -> mock(RagIndexProcessor.class))
                .run(context -> assertThat(context).hasNotFailed().hasSingleBean(RagIndexWorker.class));
    }

    @Test
    @DisplayName("임베딩 설정 문자열에는 API 키를 노출하지 않는다")
    void redactsApiKey() {
        assertThat(new RagEmbeddingProperties("text-embedding-3-small", 1536, "test-secret").toString())
                .doesNotContain("test-secret").contains("apiKey=***");
    }
}
