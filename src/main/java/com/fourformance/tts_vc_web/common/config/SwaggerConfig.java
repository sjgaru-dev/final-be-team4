package com.fourformance.tts_vc_web.common.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

// Swagger UI 주소입니다.
// http://localhost:8080/swagger-ui/index.html#/

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("AIPark API").version("1.0.0")
                        .description("기업연계 파이널 프로젝트 API 문서 백엔드 개발용"));
    }

    // api들을 그룹으로 지정하는 기능입니다.
    // 당분간은 모든 api들을 하나의 그룹으로 지정하고 개발 중 api들이 많아지면 회의를 통해 어떻게 그룹화 할지 얘기하도록 해요!
    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("v1-definition") // 이 그룹으로 API들이 표시
                .pathsToMatch("/**") // 이 경로의 API들이 그룹에 속하게 됨
                .build();
    }
}
