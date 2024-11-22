package com.fourformance.tts_vc_web.repository;

import com.fourformance.tts_vc_web.domain.entity.VoiceStyle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoiceStyleRepository extends JpaRepository<VoiceStyle, Long> {

    // 표시 여부가 true인 voice style 데이터 반환 - 승민
    @Query("SELECT v FROM VoiceStyle v WHERE v.isVisible = true")
    List<VoiceStyle> findVisibleVoiceStyles();
}
