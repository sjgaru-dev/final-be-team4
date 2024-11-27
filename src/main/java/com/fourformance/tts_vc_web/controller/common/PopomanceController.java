package com.fourformance.tts_vc_web.controller.common;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PopomanceController {

    private static final String EXCAHGE_NAME = "popomance.exchange";

    @Autowired
    RabbitTemplate rabbitTemplate;

    @GetMapping("popomance/queue")
    public String samplePublish() {
        rabbitTemplate.convertAndSend(EXCAHGE_NAME, "popomance.routing.#", "RabbitMQ + SpringBoot = Success");
        return "Message seding!";
    }

}
