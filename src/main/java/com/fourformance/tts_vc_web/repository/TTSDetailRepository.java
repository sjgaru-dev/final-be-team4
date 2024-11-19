package com.fourformance.tts_vc_web.repository;

import com.fourformance.tts_vc_web.domain.entity.TTSDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TTSDetailRepository extends JpaRepository<TTSDetail, Long> {

    // 프로젝트 ID로 TTS 상세 값들을 찾아 리스트로 반환 - 승민
    List<TTSDetail> findByTtsProjectId(Long projectId);

    // TTS Detail Id가 담긴 List로 TTSDetail 객체 반환 받기 - 승민
    @Query("SELECT t FROM TTSDetail t WHERE t.id IN :ttsDetailIdList")
    List<TTSDetail> findByTtsDetailIds(@Param("ttsDetailIdList") List<Long> ttsDetailIdList);
}
