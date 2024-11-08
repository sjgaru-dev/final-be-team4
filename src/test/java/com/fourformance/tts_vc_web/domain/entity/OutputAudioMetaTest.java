package com.fourformance.tts_vc_web.domain.entity;

import com.fourformance.tts_vc_web.common.constant.APIStatusConst;
import com.fourformance.tts_vc_web.common.constant.ProjectType;
import com.fourformance.tts_vc_web.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static java.time.LocalTime.now;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
@Transactional
@Rollback(value=false)
class OutputAudioMetaTest {


    @Autowired
    OutputAudioMetaRepository OutputAudioMetaRepository;

    @Autowired
    VCProjectRepository vcProjectRepository;

    @Autowired
    VCDetailRepository vcDetailRepository;

    @Autowired
    TTSProjectRepository ttsProjectRepository;

    @Autowired
    StyleRepository styleRepository;

    @Autowired
    TTSDetailRepository ttsDetailRepository;

    @Autowired
    MemberRepository memberRepository;

    @PersistenceContext
    EntityManager em;


    private final String BASE_ROUTE = "https://popomance.s3.amazonaws.com/";

    Style style = Style.createStyle("ko","nara","happy","woman",20,true);
    TTSProject ttsProject = TTSProject.createTTSProject("MyFirstTTSProject", style, "안녕하세요! 반가워여!",0.0f,0.1f,0.5f, APIStatusConst.SUCCESS);
    // 여기에다가 Member를 매개변수에 추가해줘야함.
    TTSDetail ttsDetail = TTSDetail.createTTSDetail(ttsProject,"안녕하세요",1);

    Member member = Member.createMember("aaa@AAA.com","1234","imsi",0, LocalDateTime.now(),"01012341234","A",null);
    VCProject vcProject = VCProject.createVCProject(member,"MyFirstVCProject2");
    VCDetail vcDetail = VCDetail.createVCDetail(vcProject);

    @Autowired
    private OutputAudioMetaRepository outputAudioMetaRepository;



    // OutputAudioMeta생성
    public OutputAudioMeta createTestTTSOutputAudioMeta() {
        return OutputAudioMeta.createOutputAudioMeta(ttsDetail,null,null, ProjectType.TTS,BASE_ROUTE+"/s3경로");
    }

    public OutputAudioMeta createTestVCOutputAudioMeta() {
        return OutputAudioMeta.createOutputAudioMeta(null,vcDetail, null,ProjectType.VC, BASE_ROUTE+"/s3VC경로");
    }


    @Test
    @DisplayName("TTS오디오메타생성")
    public void createTTSTest() {

        // 먼저 TTSDetail을 저장
        styleRepository.save(style);
        OutputAudioMeta outputAudioMeta = createTestTTSOutputAudioMeta();// OutputAudioMeta와 TTSDetail 연결
        ttsProjectRepository.save(ttsProject);
        ttsDetailRepository.save(ttsDetail);

        // OutputAudioMeta 저장
        outputAudioMetaRepository.save(outputAudioMeta);
        em.persist(outputAudioMeta);

        em.flush();
        em.clear();

        OutputAudioMeta outputAudioMeta1 = outputAudioMetaRepository.findById(outputAudioMeta.getId()).orElse(null);
        assertNotNull(outputAudioMeta1);
        System.out.println("outputAudioMeta1 = " + outputAudioMeta1);
    }


    @Test
    @DisplayName("VC 오디오메타 delete")
    public void deleteVCTest() {
        // 먼저 사전 준비
        styleRepository.save(style);
        ttsProjectRepository.save(ttsProject);
        ttsDetailRepository.save(ttsDetail);

        OutputAudioMeta outputAudioMeta = createTestTTSOutputAudioMeta();

        // OutputAudioMeta 저장
        outputAudioMetaRepository.save(outputAudioMeta);
        em.persist(outputAudioMeta);

        em.flush();
        em.clear();

        OutputAudioMeta outputAudioMeta1 = outputAudioMetaRepository.findById(outputAudioMeta.getId()).orElse(null);


        assertNotNull(outputAudioMeta1);
        outputAudioMetaRepository.delete(outputAudioMeta1);

        assertNull(outputAudioMetaRepository.findById(outputAudioMeta.getId()).orElse(null));

    }
        @Test
    @DisplayName("VC 오디오 메타 create")
    public void createVCTest() {
        memberRepository.saveAndFlush(member);
        vcProjectRepository.saveAndFlush(vcProject);
        vcDetailRepository.saveAndFlush(vcDetail);
        OutputAudioMeta outputAudioMeta = createTestVCOutputAudioMeta();

        outputAudioMetaRepository.save(outputAudioMeta);
        em.persist(outputAudioMeta);

        em.flush();
        em.clear();
        OutputAudioMeta outputAudioMeta1 = outputAudioMetaRepository.findById(outputAudioMeta.getId()).orElse(null);
        assertNotNull(outputAudioMeta1);
        System.out.println("outputAudioMeta1 = " + outputAudioMeta1);
    }
    @Test
    @DisplayName("TTS 오디오 메타 delete")
    public void deleteTTSTest() {
        // 먼저 사전 준비
        styleRepository.save(style);
        ttsProjectRepository.save(ttsProject);
        ttsDetailRepository.save(ttsDetail);

        OutputAudioMeta outputAudioMeta = createTestTTSOutputAudioMeta();

        // OutputAudioMeta 저장
        outputAudioMetaRepository.save(outputAudioMeta);
        em.persist(outputAudioMeta);

        em.flush();
        em.clear();

        OutputAudioMeta outputAudioMeta1 = outputAudioMetaRepository.findById(outputAudioMeta.getId()).orElse(null);


        assertNotNull(outputAudioMeta1);
        outputAudioMetaRepository.delete(outputAudioMeta1);

        assertNull(outputAudioMetaRepository.findById(outputAudioMeta.getId()).orElse(null));
    }


}