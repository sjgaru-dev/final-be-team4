package com.fourformance.tts_vc_web.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class VCAppConfig {
    @Bean
    public RestTemplate restTemplate() {
        // 요청을 메모리에 버퍼링하여 대용량 파일 전송을 지원
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(60000); // 연결 타임아웃 60초
        requestFactory.setReadTimeout(60000);    // 읽기 타임아웃 60초
        return new RestTemplate(new BufferingClientHttpRequestFactory(requestFactory));
    }
}
