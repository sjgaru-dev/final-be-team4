package com.fourformance.tts_vc_web.domain.entity;

import com.fourformance.tts_vc_web.common.constant.APIStatusConst;
import com.fourformance.tts_vc_web.repository.VCProjectRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class VCProjectTest {

    @Autowired
    VCProjectRepository repository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    public void VC프로젝트_생성_테스트() {

//        VCProject project = VCProject.createVCProject("vc프로젝트1");
//        entityManager.persist(project);


    }

}