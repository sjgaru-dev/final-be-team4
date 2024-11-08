//package com.fourformance.tts_vc_web.domain.entity;
//
//import com.fourformance.tts_vc_web.repository.VCDetailRepository;
//import com.fourformance.tts_vc_web.repository.VCProjectRepository;
//import jakarta.persistence.EntityManager;
//import jakarta.persistence.PersistenceContext;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.transaction.annotation.Transactional;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest
//@Transactional
//class VCDetailTest {
//
//    @Autowired
//    private VCDetailRepository vcDetailRepository;
//
//    @Autowired
//    private VCProjectRepository vcProjectRepository;
//
//    @PersistenceContext
//    private EntityManager em;
//
//    public VCProject VC프로젝트_생성() {
//
//        VCProject vcProject = new VCProject();
//        vcProjectRepository.save(vcProject);
//        return vcProject;
//    }
//
//    @Test
//    public void VC디테일_생성_테스트() {
//
//        // VC 프로젝트 생성
//        VCProject project = VC프로젝트_생성();
//
//        // VC detail 생성후 저장
//        VCDetail vcDetail = VCDetail.createVCDetail(project);
//        vcDetailRepository.save(vcDetail);
//
//        em.flush();
//        em.clear();
//
//        // VC 디테일 조회
//
//        VCDetail foundVCDetail1 = em.find(VCDetail.class, vcDetail.getId());
//        VCDetail foundVCDetail2 = vcDetailRepository.findById(vcDetail.getId()).orElse(null);
//
//        // 조회한 VC 디테일이 일치하는지 검증
//
//        assertEquals(foundVCDetail1.getId(), vcDetail.getId());
//        assertEquals(foundVCDetail2.getId(), vcDetail.getId());
//
//    }
//
//    @Test
//    public void VC디테일_업데이트_테스트() {
//
//        // VC 프로젝트 생성
//        VCProject project = VC프로젝트_생성();
//
//        // VC detail 생성후 저장
//        VCDetail vcDetail = VCDetail.createVCDetail(project);
//        vcDetailRepository.save(vcDetail);
//
//        em.flush();
//        em.clear();
//
//        // VC디테일 업데이트 후 DB 저장
//        vcDetail.updateDetails(true,"스크립트");
//        vcDetailRepository.save(vcDetail);
//
//        em.flush();
//        em.clear();
//
//        // 제대로 업데이트 되었느지 조회 후 확인
//
//        VCDetail foundVCDetail = vcDetailRepository.findById(vcDetail.getId()).orElse(null);
//        assertEquals(foundVCDetail.getIsChecked(), true);
//        assertEquals(foundVCDetail.getUnitScript(), "스크립트");
//
//    }
//
//    @Test
//    public void VC디테일_삭제_테스트() {
//
//        // VC 프로젝트 생성
//        VCProject project = VC프로젝트_생성();
//
//        // VC detail 생성후 저장
//        VCDetail vcDetail = VCDetail.createVCDetail(project);
//        vcDetailRepository.save(vcDetail);
//
//        em.flush();
//        em.clear();
//
//        // VCDetail 삭제
//        vcDetailRepository.deleteById(vcDetail.getId());
//
//        em.flush();
//        em.clear();
//
//        // 삭제 후 다시 조회
//        VCDetail foundVCDetail = vcDetailRepository.findById(vcDetail.getId()).orElse(null);
//
//        // 조회 결과가 null인지 확인 (삭제되었는지 확인)
//        assertNull(foundVCDetail, "VCDetail이 성공적으로 삭제되어야 합니다.");
//    }
//
//
//
//}