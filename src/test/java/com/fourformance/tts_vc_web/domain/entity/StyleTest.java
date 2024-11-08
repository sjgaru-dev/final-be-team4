package com.fourformance.tts_vc_web.domain.entity;

import com.fourformance.tts_vc_web.repository.StyleRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@Transactional
@Rollback(value=false)
class StyleTest {

    @Autowired
    private StyleRepository styleRepository;

    @PersistenceContext
    EntityManager em;


    Style style = Style.createStyle("ko","nara","happy","woman",20,true);


    public Style createStyle() {
        return styleRepository.save(style);
    }

    @Test
    @DisplayName("Style생성")
    public void createTest() {
        styleRepository.saveAndFlush(style);
        assertNotNull(em.find(Style.class, style.getId()));

    }


}