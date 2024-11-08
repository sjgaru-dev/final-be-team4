package com.fourformance.tts_vc_web.domain.entity;

import com.fourformance.tts_vc_web.common.constant.APIStatusConst;
import com.fourformance.tts_vc_web.repository.StyleRepository;
import com.fourformance.tts_vc_web.repository.TTSDetailRepository;
import com.fourformance.tts_vc_web.repository.TTSProjectRepository;
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
class TTSDetailTest {

    @Autowired
    private TTSDetailRepository ttsDetailRepository;

    @Autowired
    private TTSProjectRepository ttsProjectRepository;

    @Autowired
    StyleRepository styleRepository;


    @PersistenceContext
    EntityManager em;

    Style style = Style.createStyle("ko","nara","happy","woman",20,true);
    TTSProject ttsProject = TTSProject.createTTSProject("MyFirstTTSProject", style, "안녕하세요! 반가워여!",0.0f,0.1f,0.5f, APIStatusConst.SUCCESS);
    TTSDetail ttsDetail = TTSDetail.createTTSDetail(ttsProject,"안녕하세요",1);

    public TTSDetail createTTSDetail() {
        return ttsDetailRepository.save(ttsDetail);
    }

    @Test
    @DisplayName("TTSDetail생성테스트") // styleId가 null인게 맞음.

    public void createTest() {
        styleRepository.saveAndFlush(style);
        ttsProjectRepository.saveAndFlush(ttsProject);
        ttsDetailRepository.saveAndFlush(ttsDetail);

        em.clear();

        assertNotNull(em.find(TTSDetail.class, ttsDetail.getId()));
    }

    @Test
    @DisplayName("TTSDetail 스타일 업데이트 테스트")
    public void UpdateTest() {


        styleRepository.saveAndFlush(style);
        ttsProjectRepository.saveAndFlush(ttsProject);
        ttsDetailRepository.saveAndFlush(ttsDetail);
        em.clear();

        TTSDetail ttsDetail1 = em.find(TTSDetail.class, ttsDetail.getId());
        ttsDetail1.updateTTSDetails(style, "하이하이",0.3f,0.7f,0.9f,2,false);



        ttsDetailRepository.saveAndFlush(ttsDetail1);
        em.clear();

        TTSDetail updatedTTSDetail = em.find(TTSDetail.class, ttsDetail1.getId());
        assertEquals(2, updatedTTSDetail.getUnitSequence());
    }
}