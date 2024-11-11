package com.fourformance.tts_vc_web.domain.entity;

import com.fourformance.tts_vc_web.repository.MemberRepository;
import com.fourformance.tts_vc_web.repository.ProjectRepository;
import com.fourformance.tts_vc_web.repository.VCDetailRepository;
import com.fourformance.tts_vc_web.repository.VCProjectRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback
class VCProjectTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    private VCProjectRepository vcProjectRepository;

    @Autowired
    private VCDetailRepository vcDetailRepository;

    @PersistenceContext
    EntityManager em;

    Member member = Member.createMember("aaa@aaa.com", "1234", "imsi", 0, LocalDateTime.now(), "01012341234");

    // 1. 저장 테스트
    @Test
    @DisplayName("VCProjectTest")
    public void createVCProject() {
        memberRepository.saveAndFlush(member);
        VCProject vcProject = VCProject.createVCProject(member, "VC프로젝트1");
        vcProjectRepository.save(vcProject);
        em.flush();
        em.clear();

        VCProject afterVCProject = vcProjectRepository.findById(vcProject.getId()).orElse(null);
        assertNotNull(afterVCProject);
        assertEquals(vcProject.getId(), afterVCProject.getId());
    }
}
