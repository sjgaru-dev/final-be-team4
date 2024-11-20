//package com.fourformance.tts_vc_web.domain.entity;
//
//import com.fourformance.tts_vc_web.common.constant.AudioFormat;
//import com.fourformance.tts_vc_web.common.constant.ProjectType;
//import com.fourformance.tts_vc_web.repository.*;
//import jakarta.persistence.EntityManager;
//import jakarta.persistence.PersistenceContext;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.annotation.Rollback;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest
//@Transactional
//@Rollback()
//class OutputAudioMetaTest {
//
//    @Autowired
//    private OutputAudioMetaRepository outputAudioMetaRepository;
//    @Autowired
//    private TTSDetailRepository ttsDetailRepository;
//    @Autowired
//    private VCDetailRepository vcDetailRepository;
//    @Autowired
//    private ConcatProjectRepository concatProjectRepository;
//
//    @PersistenceContext
//    private EntityManager em;
//
//    // 테스트 실행 전 초기화 작업
//    @BeforeEach
//    public void 초기화() {
//        // 관련 테이블 초기화
//        outputAudioMetaRepository.deleteAll();
//        ttsDetailRepository.deleteAll();
//        vcDetailRepository.deleteAll();
//        concatProjectRepository.deleteAll();
//        em.flush();
//        em.clear();
//    }
//
//    // 1-1. TTS 프로젝트에서 OutputAudioMeta 생성 테스트
//    @Test
//    @DisplayName("OutputAudioMeta 생성 테스트 - TTS 프로젝트")
//    public void TTS_프로젝트에서_OutputAudioMeta_생성_테스트() {
//
//        // given - TTSDetail 생성 및 저장
//        TTSDetail ttsDetail = new TTSDetail();
//        ttsDetailRepository.saveAndFlush(ttsDetail);
//
//        // when - OutputAudioMeta 생성 및 저장
//        OutputAudioMeta audioMeta = OutputAudioMeta.createOutputAudioMeta(null, ttsDetail, null, null, ProjectType.TTS, "/audio/tts.wav");
//        outputAudioMetaRepository.saveAndFlush(audioMeta);
//        em.flush();
//        em.clear();
//
//        // then - 생성된 OutputAudioMeta 검증
//        OutputAudioMeta foundMeta = outputAudioMetaRepository.findById(audioMeta.getId()).orElse(null);
//        assertNotNull(foundMeta);
//        assertEquals(ProjectType.TTS, foundMeta.getProjectType());
//        assertEquals("/audio/tts.wav", foundMeta.getAudioUrl());
//        assertEquals(AudioFormat.WAV, foundMeta.getAudioFormat());
//        assertFalse(foundMeta.getIsDeleted());
//    }
//
//    // 1-2. VC 프로젝트에서 OutputAudioMeta 생성 테스트
//    @Test
//    @DisplayName("OutputAudioMeta 생성 테스트 - VC 프로젝트")
//    public void VC_프로젝트에서_OutputAudioMeta_생성_테스트() {
//
//        // given - VCDetail 생성 및 저장
//        VCDetail vcDetail = new VCDetail();
//        vcDetailRepository.saveAndFlush(vcDetail);
//
//        // when - OutputAudioMeta 생성 및 저장
//        OutputAudioMeta audioMeta = OutputAudioMeta.createOutputAudioMeta(null, null, vcDetail, null, ProjectType.VC, "/audio/vc.wav");
//        outputAudioMetaRepository.saveAndFlush(audioMeta);
//        em.flush();
//        em.clear();
//
//        // then - 생성된 OutputAudioMeta 검증
//        OutputAudioMeta foundMeta = outputAudioMetaRepository.findById(audioMeta.getId()).orElse(null);
//        assertNotNull(foundMeta);
//        assertEquals(ProjectType.VC, foundMeta.getProjectType());
//        assertEquals("/audio/vc.wav", foundMeta.getAudioUrl());
//        assertEquals(AudioFormat.WAV, foundMeta.getAudioFormat());
//        assertFalse(foundMeta.getIsDeleted());
//    }
//
//    // 1-3. Concat 프로젝트에서 OutputAudioMeta 생성 테스트
//    @Test
//    @DisplayName("OutputAudioMeta 생성 테스트 - Concat 프로젝트")
//    public void Concat_프로젝트에서_OutputAudioMeta_생성_테스트() {
//
//        // given - ConcatProject 생성 및 저장
//        ConcatProject concatProject = new ConcatProject();
//        concatProjectRepository.saveAndFlush(concatProject);
//
//        // when - OutputAudioMeta 생성 및 저장
//        OutputAudioMeta audioMeta = OutputAudioMeta.createOutputAudioMeta(null, null, null, concatProject, ProjectType.CONCAT, "/audio/concat.wav");
//        outputAudioMetaRepository.saveAndFlush(audioMeta);
//        em.flush();
//        em.clear();
//
//        // then - 생성된 OutputAudioMeta 검증
//        OutputAudioMeta foundMeta = outputAudioMetaRepository.findById(audioMeta.getId()).orElse(null);
//        assertNotNull(foundMeta);
//        assertEquals(ProjectType.CONCAT, foundMeta.getProjectType());
//        assertEquals("/audio/concat.wav", foundMeta.getAudioUrl());
//        assertEquals(AudioFormat.WAV, foundMeta.getAudioFormat());
//        assertFalse(foundMeta.getIsDeleted());
//    }
//
//    // 2-1. TTS 프로젝트에서 OutputAudioMeta 조회 테스트
//    @Test
//    @DisplayName("OutputAudioMeta 조회 테스트 - TTS 프로젝트")
//    public void TTS_프로젝트에서_OutputAudioMeta_조회_테스트() {
//
//        // given - TTSDetail 생성 및 저장
//        TTSDetail ttsDetail = new TTSDetail();
//        ttsDetailRepository.saveAndFlush(ttsDetail);
//
//        // OutputAudioMeta 생성 및 저장
//        OutputAudioMeta audioMeta = OutputAudioMeta.createOutputAudioMeta(null, ttsDetail, null, null, ProjectType.TTS, "/audio/tts.wav");
//        outputAudioMetaRepository.saveAndFlush(audioMeta);
//        em.flush();
//        em.clear();
//
//        // when - ID로 OutputAudioMeta 조회
//        OutputAudioMeta foundMeta = outputAudioMetaRepository.findById(audioMeta.getId()).orElse(null);
//
//        // then - 조회된 OutputAudioMeta 검증
//        assertNotNull(foundMeta);
//        assertEquals(audioMeta.getId(), foundMeta.getId());
//        assertEquals("/audio/tts.wav", foundMeta.getAudioUrl());
//        assertEquals(ProjectType.TTS, foundMeta.getProjectType());
//        assertEquals(AudioFormat.WAV, foundMeta.getAudioFormat()); // 항상 WAV 형식 검증
//    }
//
//    // 2-2. VC 프로젝트에서 OutputAudioMeta 조회 테스트
//    @Test
//    @DisplayName("OutputAudioMeta 조회 테스트 - VC 프로젝트")
//    public void VC_프로젝트에서_OutputAudioMeta_조회_테스트() {
//
//        // given - VCDetail 생성 및 저장
//        VCDetail vcDetail = new VCDetail();
//        vcDetailRepository.saveAndFlush(vcDetail);
//
//        // OutputAudioMeta 생성 및 저장
//        OutputAudioMeta audioMeta = OutputAudioMeta.createOutputAudioMeta(null, null, vcDetail, null, ProjectType.VC, "/audio/vc.wav");
//        outputAudioMetaRepository.saveAndFlush(audioMeta);
//        em.flush();
//        em.clear();
//
//        // when - ID로 OutputAudioMeta 조회
//        OutputAudioMeta foundMeta = outputAudioMetaRepository.findById(audioMeta.getId()).orElse(null);
//
//        // then - 조회된 OutputAudioMeta 검증
//        assertNotNull(foundMeta);
//        assertEquals(audioMeta.getId(), foundMeta.getId());
//        assertEquals("/audio/vc.wav", foundMeta.getAudioUrl());
//        assertEquals(ProjectType.VC, foundMeta.getProjectType());
//        assertEquals(AudioFormat.WAV, foundMeta.getAudioFormat()); // 항상 WAV 형식 검증
//    }
//
//    // 2-3. Concat 프로젝트에서 OutputAudioMeta 조회 테스트
//    @Test
//    @DisplayName("OutputAudioMeta 조회 테스트 - Concat 프로젝트")
//    public void Concat_프로젝트에서_OutputAudioMeta_조회_테스트() {
//
//        // given - ConcatProject 생성 및 저장
//        ConcatProject concatProject = new ConcatProject();
//        concatProjectRepository.saveAndFlush(concatProject);
//
//        // OutputAudioMeta 생성 및 저장
//        OutputAudioMeta audioMeta = OutputAudioMeta.createOutputAudioMeta(null, null, null, concatProject, ProjectType.CONCAT, "/audio/concat.wav");
//        outputAudioMetaRepository.saveAndFlush(audioMeta);
//        em.flush();
//        em.clear();
//
//        // when - ID로 OutputAudioMeta 조회
//        OutputAudioMeta foundMeta = outputAudioMetaRepository.findById(audioMeta.getId()).orElse(null);
//
//        // then - 조회된 OutputAudioMeta 검증
//        assertNotNull(foundMeta);
//        assertEquals(audioMeta.getId(), foundMeta.getId());
//        assertEquals("/audio/concat.wav", foundMeta.getAudioUrl());
//        assertEquals(ProjectType.CONCAT, foundMeta.getProjectType());
//        assertEquals(AudioFormat.WAV, foundMeta.getAudioFormat()); // 항상 WAV 형식 검증
//    }
//
//    // 3-1. TTS 프로젝트에서 OutputAudioMeta 삭제 테스트
//    @Test
//    @DisplayName("OutputAudioMeta 삭제 테스트 - TTS 프로젝트")
//    public void TTS_프로젝트에서_OutputAudioMeta_삭제_테스트() {
//
//        // given - TTSDetail 생성 및 저장
//        TTSDetail ttsDetail = new TTSDetail();
//        ttsDetailRepository.saveAndFlush(ttsDetail);
//
//        // OutputAudioMeta 생성 및 저장
//        OutputAudioMeta audioMeta = OutputAudioMeta.createOutputAudioMeta(null, ttsDetail, null, null, ProjectType.TTS, "/audio/tts.wav");
//        outputAudioMetaRepository.saveAndFlush(audioMeta);
//        em.flush();
//        em.clear();
//
//        // when - OutputAudioMeta 삭제
//        audioMeta.deleteOutputAudioMeta();
//        outputAudioMetaRepository.saveAndFlush(audioMeta);
//        em.flush();
//        em.clear();
//
//        // then - 삭제된 OutputAudioMeta 검증
//        OutputAudioMeta deletedMeta = outputAudioMetaRepository.findById(audioMeta.getId()).orElse(null);
//        assertNotNull(deletedMeta); // 삭제된 객체가 조회되는지 확인
//        assertTrue(deletedMeta.getIsDeleted()); // isDeleted 필드가 true인지 검증
//        assertNotNull(deletedMeta.getDeletedAt()); // 삭제 시각이 설정되었는지 검증
//    }
//
//    // 3-2. VC 프로젝트에서 OutputAudioMeta 삭제 테스트
//    @Test
//    @DisplayName("OutputAudioMeta 삭제 테스트 - VC 프로젝트")
//    public void VC_프로젝트에서_OutputAudioMeta_삭제_테스트() {
//
//        // given - VCDetail 생성 및 저장
//        VCDetail vcDetail = new VCDetail();
//        vcDetailRepository.saveAndFlush(vcDetail);
//
//        // OutputAudioMeta 생성 및 저장
//        OutputAudioMeta audioMeta = OutputAudioMeta.createOutputAudioMeta(null, null, vcDetail, null, ProjectType.VC, "/audio/vc.mp3");
//        outputAudioMetaRepository.saveAndFlush(audioMeta);
//        em.flush();
//        em.clear();
//
//        // when - OutputAudioMeta 삭제
//        audioMeta.deleteOutputAudioMeta();
//        outputAudioMetaRepository.saveAndFlush(audioMeta);
//        em.flush();
//        em.clear();
//
//        // then - 삭제된 OutputAudioMeta 검증
//        OutputAudioMeta deletedMeta = outputAudioMetaRepository.findById(audioMeta.getId()).orElse(null);
//        assertNotNull(deletedMeta); // 삭제된 객체가 조회되는지 확인
//        assertTrue(deletedMeta.getIsDeleted()); // isDeleted 필드가 true인지 검증
//        assertNotNull(deletedMeta.getDeletedAt()); // 삭제 시각이 설정되었는지 검증
//    }
//
//    // 3-3. Concat 프로젝트에서 OutputAudioMeta 삭제 테스트
//    @Test
//    @DisplayName("OutputAudioMeta 삭제 테스트 - Concat 프로젝트")
//    public void Concat_프로젝트에서_OutputAudioMeta_삭제_테스트() {
//
//        // given - ConcatProject 생성 및 저장
//        ConcatProject concatProject = new ConcatProject();
//        concatProjectRepository.saveAndFlush(concatProject);
//
//        // OutputAudioMeta 생성 및 저장
//        OutputAudioMeta audioMeta = OutputAudioMeta.createOutputAudioMeta(null, null, null, concatProject, ProjectType.CONCAT, "/audio/concat.aac");
//        outputAudioMetaRepository.saveAndFlush(audioMeta);
//        em.flush();
//        em.clear();
//
//        // when - OutputAudioMeta 삭제
//        audioMeta.deleteOutputAudioMeta();
//        outputAudioMetaRepository.saveAndFlush(audioMeta);
//        em.flush();
//        em.clear();
//
//        // then - 삭제된 OutputAudioMeta 검증
//        OutputAudioMeta deletedMeta = outputAudioMetaRepository.findById(audioMeta.getId()).orElse(null);
//        assertNotNull(deletedMeta); // 삭제된 객체가 조회되는지 확인
//        assertTrue(deletedMeta.getIsDeleted()); // isDeleted 필드가 true인지 검증
//        assertNotNull(deletedMeta.getDeletedAt()); // 삭제 시각이 설정되었는지 검증
//    }
//
//    // 4-1. TTS 프로젝트에서 OutputAudioMeta 복구 테스트
//    @Test
//    @DisplayName("OutputAudioMeta 복구 테스트 - TTS 프로젝트")
//    public void TTS_프로젝트에서_OutputAudioMeta_복구_테스트() {
//
//        // given - TTSDetail 생성 및 저장
//        TTSDetail ttsDetail = new TTSDetail();
//        ttsDetailRepository.saveAndFlush(ttsDetail);
//
//        // OutputAudioMeta 생성 및 소프트 삭제
//        OutputAudioMeta audioMeta = OutputAudioMeta.createOutputAudioMeta(null, ttsDetail, null, null, ProjectType.TTS, "/audio/tts.wav");
//        audioMeta.deleteOutputAudioMeta();
//        outputAudioMetaRepository.saveAndFlush(audioMeta);
//        em.flush();
//        em.clear();
//
//        // when - OutputAudioMeta 복구
//        audioMeta.restore();
//        outputAudioMetaRepository.saveAndFlush(audioMeta);
//        em.flush();
//        em.clear();
//
//        // then - 복구된 OutputAudioMeta 검증
//        OutputAudioMeta restoredMeta = outputAudioMetaRepository.findById(audioMeta.getId()).orElse(null);
//        assertNotNull(restoredMeta);
//        assertFalse(restoredMeta.getIsDeleted());
//        assertNull(restoredMeta.getDeletedAt());
//    }
//
//    // 4-2. VC 프로젝트에서 OutputAudioMeta 복구 테스트
//    @Test
//    @DisplayName("OutputAudioMeta 복구 테스트 - VC 프로젝트")
//    public void VC_프로젝트에서_OutputAudioMeta_복구_테스트() {
//
//        // given - VCDetail 생성 및 저장
//        VCDetail vcDetail = new VCDetail();
//        vcDetailRepository.saveAndFlush(vcDetail);
//
//        // OutputAudioMeta 생성 및 소프트 삭제
//        OutputAudioMeta audioMeta = OutputAudioMeta.createOutputAudioMeta(null, null, vcDetail, null, ProjectType.VC, "/audio/vc.mp3");
//        audioMeta.deleteOutputAudioMeta();
//        outputAudioMetaRepository.saveAndFlush(audioMeta);
//        em.flush();
//        em.clear();
//
//        // when - OutputAudioMeta 복구
//        audioMeta.restore();
//        outputAudioMetaRepository.saveAndFlush(audioMeta);
//        em.flush();
//        em.clear();
//
//        // then - 복구된 OutputAudioMeta 검증
//        OutputAudioMeta restoredMeta = outputAudioMetaRepository.findById(audioMeta.getId()).orElse(null);
//        assertNotNull(restoredMeta);
//        assertFalse(restoredMeta.getIsDeleted());
//        assertNull(restoredMeta.getDeletedAt());
//    }
//
//    // 4-3. Concat 프로젝트에서 OutputAudioMeta 복구 테스트
//    @Test
//    @DisplayName("OutputAudioMeta 복구 테스트 - Concat 프로젝트")
//    public void Concat_프로젝트에서_OutputAudioMeta_복구_테스트() {
//
//        // given - ConcatProject 생성 및 저장
//        ConcatProject concatProject = new ConcatProject();
//        concatProjectRepository.saveAndFlush(concatProject);
//
//        // OutputAudioMeta 생성 및 소프트 삭제
//        OutputAudioMeta audioMeta = OutputAudioMeta.createOutputAudioMeta(null, null, null, concatProject, ProjectType.CONCAT, "/audio/concat.aac");
//        audioMeta.deleteOutputAudioMeta();
//        outputAudioMetaRepository.saveAndFlush(audioMeta);
//        em.flush();
//        em.clear();
//
//        // when - OutputAudioMeta 복구
//        audioMeta.restore();
//        outputAudioMetaRepository.saveAndFlush(audioMeta);
//        em.flush();
//        em.clear();
//
//        // then - 복구된 OutputAudioMeta 검증
//        OutputAudioMeta restoredMeta = outputAudioMetaRepository.findById(audioMeta.getId()).orElse(null);
//        assertNotNull(restoredMeta);
//        assertFalse(restoredMeta.getIsDeleted());
//        assertNull(restoredMeta.getDeletedAt());
//    }
//
//}
