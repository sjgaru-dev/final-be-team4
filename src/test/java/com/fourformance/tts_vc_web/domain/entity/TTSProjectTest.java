//package com.fourformance.tts_vc_web.domain.entity;
//
//import com.fourformance.tts_vc_web.common.constant.APIStatusConst;
//import com.fourformance.tts_vc_web.repository.MemberRepository;
//import com.fourformance.tts_vc_web.repository.StyleRepository;
//import com.fourformance.tts_vc_web.repository.TTSProjectRepository;
//import jakarta.persistence.EntityManager;
//import jakarta.persistence.PersistenceContext;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.annotation.Rollback;
//import org.springframework.transaction.annotation.Transactional;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest
//@Transactional
//@Rollback(value = false)
//class TTSProjectTest {
//
//    @PersistenceContext
//    private EntityManager em;
//
//    @Autowired
//    TTSProjectRepository ttsProjectRepository;
//
//    @Autowired
//    StyleRepository styleRepository;
//
//    @Autowired
//    MemberRepository memberRepository;
//
//    @Test
//    public void TTS프로젝트_생성_테스트() {
//
//        // 스타일 생성
//        Style style = new Style();
//        styleRepository.save(style);
//
//        // 멤버 생성
//        Member member = new Member();
//        memberRepository.save(member);
//
//        // TTS 프로젝트 생성 후 DB 저장
////        TTSProject project = TTSProject.createTTSProject(member, "프로젝트", "풀스크립트");
//        ttsProjectRepository.save(project);
//        em.flush();
//        em.clear();
//
//        // DB에 저장된 TTS 프로젝트 조회
//        TTSProject foundTTSProject = ttsProjectRepository.findById(project.getId()).orElse(null);
//
//        // 생성된 프로젝트와 조회된 프로젝트가 일치하는지 확인
//        assertNotNull(foundTTSProject);
//        assertEquals(project.getId(), foundTTSProject.getId());
//        assertEquals(project.getProjectName(), foundTTSProject.getProjectName());
//        assertEquals(project.getFullScript(), "풀스크립트");
//
//    }
//
//    @Test public void TTS프로젝트_업데이트_테스트() {
//
//        // 스타일 생성
//        Style style = new Style();
//        styleRepository.save(style);
//
//        // 멤버 생성
//        Member member = new Member();
//        memberRepository.save(member);
//
//        // TTS 프로젝트 생성 후 DB 저장
//        TTSProject project = TTSProject.createTTSProject(member, "프로젝트", "풀스크립트");
//        ttsProjectRepository.save(project);
//        em.flush();
//        em.clear();
//
//        // DB에 저장된 TTS 프로젝트 조회
//        TTSProject foundTTSProject = ttsProjectRepository.findById(project.getId()).orElse(null);
//
//        // 조회한 TTS 프로젝트 업데이트
//        foundTTSProject.updateTTSProject("프로젝트이름수정", "뉴풀스크립트",1f, 1f, 1f);
//        em.flush();
//        em.clear();
//
//        assertEquals(foundTTSProject.getId(), project.getId());
//        assertEquals(foundTTSProject.getProjectName(),"프로젝트이름수정");
//        assertEquals(foundTTSProject.getFullScript(), "뉴풀스크립트");
//        assertEquals(foundTTSProject.getGlobalSpeed(), 1f);
//
//    }
//
//    @Test
//    public void TTS프로젝트_삭제_테스트() {
//
//        // 스타일 생성
//        Style style = new Style();
//        styleRepository.save(style);
//
//        // 멤버 생성
//        Member member = new Member();
//        memberRepository.save(member);
//
//        // TTS 프로젝트 생성 후 DB 저장
//        TTSProject project = TTSProject.createTTSProject(member, "프로젝트", "풀스크립트");
//        ttsProjectRepository.save(project);
//        em.flush();
//        em.clear();
//
//        // 잘 저장 되었는지 확인
//        TTSProject foundTTSProject = ttsProjectRepository.findById(project.getId()).orElse(null);
//        assertNotNull(foundTTSProject);
//
//        // 삭제
//        ttsProjectRepository.deleteById(project.getId());
//        em.flush();
//        em.clear();
//
//        // 삭제 잘 되었나 확인
//        assertNull(ttsProjectRepository.findById(project.getId()).orElse(null));
//
//    }
//
//    @Test
//    public void 임시_테스트() {
//
//        // 스타일 생성
//        Style style = new Style();
//        styleRepository.save(style);
//
//        // 멤버 생성
//        Member member = new Member();
//        memberRepository.save(member);
//
//        // TTS 프로젝트 생성 후 DB 저장
//        TTSProject project = TTSProject.createTTSProject(member, "프로젝트111", "풀스크립트");
//        ttsProjectRepository.save(project);
//        em.flush();
//        em.clear();
//
//        TTSProject project2 = TTSProject.createTTSProject(member, "프로젝트222", "풀스크립트");
//        ttsProjectRepository.save(project);
//        em.flush();
//        em.clear();
//
//        TTSProject project3 = TTSProject.createTTSProject(member, "프로젝트333", "풀스크립트");
//        ttsProjectRepository.save(project);
//        em.flush();
//        em.clear();
//
//        // DB에 저장된 TTS 프로젝트 조회
//        TTSProject foundTTSProject = ttsProjectRepository.findById(project.getId()).orElse(null);
//
//        // 생성된 프로젝트와 조회된 프로젝트가 일치하는지 확인
//        assertNotNull(foundTTSProject);
//        assertEquals(project.getId(), foundTTSProject.getId());
//        assertEquals(project.getProjectName(), foundTTSProject.getProjectName());
//        assertEquals(project.getFullScript(), "풀스크립트");
//
//    }
//
//}