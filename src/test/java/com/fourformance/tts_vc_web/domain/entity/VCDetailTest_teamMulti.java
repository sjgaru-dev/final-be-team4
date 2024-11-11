package com.fourformance.tts_vc_web.domain.entity;

import com.fourformance.tts_vc_web.repository.VCDetailRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import static com.fourformance.tts_vc_web.domain.entity.VCDetail.createVCDetailFromUserAudio;
import static com.fourformance.tts_vc_web.domain.entity.VCProject.createVCProject;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

//@Mock: vcProject와 memberAudioMeta를 Mock 객체로 설정하여 테스트 대상(VCDetail)의 의존성을 제거
//@InjectMocks: vcDetail 객체를 테스트할 대상 객체로 생성하고, @Mock으로 생성된 vcProject와 memberAudioMeta를 자동으로 주입
@SpringBootTest
@Transactional
@Rollback(value = false)
@ExtendWith(MockitoExtension.class)
class VCDetailTest_teamMulti {
    @Autowired
    VCDetailRepository vcDetailRepository;

    @PersistenceContext
    EntityManager em;

    @Mock
    private VCProject vcProject;

    @Mock
    private MemberAudioMeta memberAudioMeta;

    @InjectMocks
    private VCDetail vcDetail;

    @BeforeEach
    void setUp() {
        // VCProject에 대한 필요한 Mock 동작 설정
//        when(vcProject.getProjectName()).thenReturn("Test Project");
//        when(vcProject.getMember()).thenReturn(new Member()); // 단순히 Member 객체를 반환하도록 설정
    }
    @Test
    @DisplayName("vcDetail 생성 테스트")
    public void testVCDetailCreation() {
        // Given: VCDetail을 특정 상태로 초기화
        vcDetail = VCDetail.createVCDetailFromUserAudio(vcProject, memberAudioMeta);

        // When: VCDetail이 생성되었을 때

        // Then: 생성된 VCDetail의 필드 값이 올바르게 설정되었는지 검증
        assertNotNull(vcDetail);
        assertEquals(vcProject, vcDetail.getVcProject());
        assertEquals(memberAudioMeta, vcDetail.getMemberAudioMeta());
        assertFalse(vcDetail.getIsChecked());
    }

    @Test
    public void testUpdateDetails() {
        // Given: 초기 상태로 VCDetail 생성
        vcDetail = VCDetail.createVCDetailFromUserAudio(vcProject, memberAudioMeta);

        // When: 특정 필드를 업데이트
        vcDetail.updateDetails(false, "Updated Script");

        // Then: 업데이트된 값이 제대로 반영되었는지 확인
//        assertTrue(vcDetail.getIsChecked());
        assertTrue(!vcDetail.getIsChecked());
        assertTrue(vcDetail.getVcProject().getId().equals(vcProject.getId()));
        assertEquals("Updated Script", vcDetail.getUnitScript());
    }

    @Test
    public void testMarkAsDeleted() {
        // Given: 초기 상태로 VCDetail 생성
        vcDetail = VCDetail.createVCDetailFromUserAudio(vcProject, memberAudioMeta);

        // When: 삭제 메서드 호출
        vcDetail.markAsDeleted();

        // Then: isDeleted 필드가 true로 변경되었는지 확인
        assertTrue(vcDetail.getIsDeleted());
        assertNotNull(vcDetail.getDeletedAt());
    }


}