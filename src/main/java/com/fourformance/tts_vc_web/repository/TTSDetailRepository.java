package com.fourformance.tts_vc_web.repository;

import com.fourformance.tts_vc_web.domain.entity.TTSDetail;
import com.fourformance.tts_vc_web.domain.entity.VoiceStyle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TTSDetailRepository extends JpaRepository<TTSDetail, Long> {

    // 프로젝트 ID로 TTS 상세 값들을 찾아 리스트로 반환 - 승민
    List<TTSDetail> findByTtsProjectId(Long projectId);

    // VoiceStyle ID로 VoiceStyle을 조회 - 원우
    @Query("SELECT vs FROM VoiceStyle vs WHERE vs.id = :id")
    VoiceStyle findVoiceStyleById(@Param("id") Long id);
}
