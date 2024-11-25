package com.fourformance.tts_vc_web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TtsVcWebApplication {

    public static void main(String[] args) {
        System.out.println("GOOGLE_APPLICATION_CREDENTIALS: " + System.getenv("GOOGLE_APPLICATION_CREDENTIALS"));
        SpringApplication.run(TtsVcWebApplication.class, args);
    }
}
