package com.fourformance.tts_vc_web.service.tts;

import com.fourformance.tts_vc_web.repository.StyleRepository;
import com.fourformance.tts_vc_web.repository.TTSDetailRepository;
import com.fourformance.tts_vc_web.repository.TTSProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class TTSService_team_multiTest {
    @Autowired
    private TTSProjectRepository ttsProjectRepository;

    @Autowired
    private TTSDetailRepository ttsDetailRepository;

    @Autowired
    private StyleRepository styleRepository;

    @Autowired
    private TTSService_team_multi ttsService;

}