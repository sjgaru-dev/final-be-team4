package com.fourformance.tts_vc_web.domain.entity;

import com.fourformance.tts_vc_web.common.constant.ProjectType;
import com.fourformance.tts_vc_web.repository.OutputAudioMetaRepository;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
@Transactional
@Rollback(value=false)
class OutputAudioMetaTest {


    @Autowired
    OutputAudioMetaRepository OutputAudioMetaRepository;

    @Autowired
    EntityManager em;

    public OutputAudioMeta createTestOutputAudioMeta() {
        return OutputAudioMeta.createOutputAudioMeta(1,null,null, ProjectType.TTS,);
    }
  
}