package com.fourformance.tts_vc_web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
public class TtsVcWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(TtsVcWebApplication.class, args);
	}

}
