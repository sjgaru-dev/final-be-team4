package com.fourformance.tts_vc_web.repository;

import com.fourformance.tts_vc_web.domain.entity.OutputAudioMeta;
import com.fourformance.tts_vc_web.domain.entity.TTSDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutputAudioMetaRepository extends JpaRepository<OutputAudioMeta, Long> {

    // 최근 생성된 5개의 OutputAudioMeta 조회 (삭제되지 않은 데이터만)
    @Query("SELECT o FROM OutputAudioMeta o " +
            "WHERE o.isDeleted = false " +
            "ORDER BY o.createdAt DESC")
    List<OutputAudioMeta> findTop5RecentOutputAudioMeta();

    // TTS Detail Id로 생성된 오디오들을 찾아 리스트로 반환 - 승민
    @Query("SELECT o FROM OutputAudioMeta o WHERE o.ttsDetail.id IN :ttsDetailIds AND o.isDeleted = false")
    List<OutputAudioMeta> findByTtsDetailAndIsDeletedFalse(@Param("ttsDetailIds") List<Long> ttsDetailIds);

    // VC Detail Id로 생성된 오디오들을 찾아 리스트로 반환 - 승민
    @Query("SELECT o FROM OutputAudioMeta o WHERE o.vcDetail.id = :vcDetailId AND o.isDeleted = false")
    List<OutputAudioMeta> findAudioUrlsByVcDetail(@Param("vcDetailId") Long vcDetailId);

}
