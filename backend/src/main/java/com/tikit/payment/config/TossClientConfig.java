package com.tikit.payment.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Configuration
@EnableConfigurationProperties(TossProperties.class)
public class TossClientConfig {

    @Bean
    public RestClient tossRestClient(TossProperties properties, TossProperties tossProperties) {
        String credentials = Base64.getEncoder()
                .encodeToString((tossProperties.secretKey() + ":").getBytes(StandardCharsets.UTF_8));

        // 응답 대기 시간 설정
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(tossProperties.connectTimeout());
        requestFactory.setReadTimeout(tossProperties.readTimeout());

        return RestClient.builder()
                .baseUrl(tossProperties.baseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + credentials)
                .requestFactory(requestFactory)
                .build();
    }
}
