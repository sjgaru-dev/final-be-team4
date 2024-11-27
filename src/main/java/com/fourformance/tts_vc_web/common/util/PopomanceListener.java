package com.fourformance.tts_vc_web.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PopomanceListener {

    private static final Logger log = LoggerFactory.getLogger(PopomanceListener.class);

    @RabbitListener(queues = "popomance.queue")
    public void reciveMessage(final Message message) {
        String msgBody = new String(message.getBody());
        log.info(message.toString());
    }
}
