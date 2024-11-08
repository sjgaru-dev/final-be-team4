//package com.fourformance.tts_vc_web.domain.entity;
//
//import com.fourformance.tts_vc_web.common.constant.APIStatusConst;
//import com.fourformance.tts_vc_web.repository.MemberRepository;
//import com.fourformance.tts_vc_web.repository.VCProjectRepository;
//import jakarta.persistence.EntityManager;
//import jakarta.persistence.PersistenceContext;
//import org.aspectj.lang.annotation.After;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
//import org.springframework.test.annotation.Rollback;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest
//@Transactional
//@Rollback(false)
//class VCProjectTest {
//
//    @Autowired
//    VCProjectRepository repository;
//
//    @Autowired
//    MemberRepository memberRepository;
//
//    @PersistenceContext
//    private EntityManager em;
//
//    public Member 멤버_생성기() {
////        memberRepository.deleteAll();
//        return new Member();
//    }
//
////    @AfterEach
////    public void afterEach() {
////        repository.deleteAll(); // 프로젝트 엔티티 먼저 삭제
////        memberRepository.deleteAll();
////    }
//
//    @Test
//    public void VC프로젝트_생성_테스트() {
//
//        // 멤버 객체 생성
//        Member member = 멤버_생성기();
//
//        // 멤버를 DB에 저장
//        memberRepository.save(member);
//
//        // VC 프로젝트 객체 생성
//        VCProject project = VCProject.createVCProject(member, "vc프로젝트2");
//
//        // VC 프로젝트 객체가 생성되었는지 확인
//        System.out.println("VC 프로젝트 : " + project);
//        assertNotNull(project);
//
//        // DB에 저장
//        repository.save(project);
//
//        // DB에서 VC 프로젝트를 조회
//        VCProject readProject = em.find(VCProject.class, project.getId());
//
//        // 생성한 프로젝트와 조회한 프로젝트가 같은지 검증
//        assertEquals(project, readProject);
//
//    }
//
//    @Test
//    public void VC프로젝트_업데이트_테스트() {
//
//        // 멤버 객체 생성
//        Member member = 멤버_생성기();
//
//        // 멤버를 DB에 저장
//        memberRepository.save(member);
//
//        // VC 프로젝트 객체 생성
//        VCProject project = VCProject.createVCProject(member, "vc프로젝트1");
//
//        // VC 프로젝트 객체가 생성되었는지 확인
//        System.out.println("업데이트 전 VC 프로젝트 : " + project);
//        assertNotNull(project);
//
//        // DB에 저장
//        repository.save(project);
//
//        // 업데이트 전 VC 프로젝트 객체가 DB에 잘 저장되었는지 검증
//        VCProject beforeVCProject = repository.findById(project.getId()).orElse(null);
//        assertEquals(beforeVCProject.getProjectName(),project.getProjectName());
//        assertEquals(beforeVCProject.getApiStatus(),project.getApiStatus());
//
//        // 생성한 VC 프로젝트의 객체의 이름과 API 상태 변경
//        project.updateVCProject("바뀐 VC 프로젝트");
//        System.out.println("업데이트 후 VC 프로젝트 : " + project);
//
//        // 업데이트된 VC 프로젝트 검증
//        assertEquals(project.getProjectName(),"바뀐 VC 프로젝트");
//
//    }
//
//    @Test
//    public void VC프로젝트_삭제_테스트() {
//
//        // 멤버 객체 생성
//        Member member = 멤버_생성기();
//
//        // 멤버를 DB에 저장
//        memberRepository.save(member);
//
//
//        // VC 프로젝트 객체 생성
//        VCProject project = VCProject.createVCProject(member, "vc프로젝트1");
//
//        // VC 프로젝트 객체가 생성되었는지 확인
//        System.out.println("업데이트 전 VC 프로젝트 : " + project);
//        assertNotNull(project);
//
//        // DB에 저장
//        repository.save(project);
//
//        // 업데이트 전 VC 프로젝트 객체가 DB에 잘 저장되었는지 검증
//        VCProject beforeVCProject = repository.findById(project.getId()).orElse(null);
//        assertEquals(beforeVCProject.getProjectName(), project.getProjectName());
//        assertEquals(beforeVCProject.getApiStatus(), project.getApiStatus());
//
//        em.flush();
//        em.clear();
//
//        // DB에 저장된 VC 프로젝트 삭제
//        repository.delete(project);
//
//        // 삭제 되었는지 검증 (Optional 사용으로 변경)
//        assertFalse(repository.findById(project.getId()).isPresent());
//    }
//
//}