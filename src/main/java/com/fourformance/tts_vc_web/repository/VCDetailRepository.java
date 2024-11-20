package com.fourformance.tts_vc_web.repository;

import com.fourformance.tts_vc_web.domain.entity.TTSDetail;
import com.fourformance.tts_vc_web.domain.entity.VCDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VCDetailRepository extends JpaRepository<VCDetail, Long> {

    // 프로젝트 ID로 TTS 상세 값들을 찾아 리스트로 반환 - 승민
    List<VCDetail> findByVcProjectId(Long projectId);
}
