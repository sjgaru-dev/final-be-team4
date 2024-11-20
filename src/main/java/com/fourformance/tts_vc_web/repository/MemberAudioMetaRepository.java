package com.fourformance.tts_vc_web.repository;

import com.fourformance.tts_vc_web.common.constant.AudioType;
import com.fourformance.tts_vc_web.domain.entity.MemberAudioMeta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemberAudioMetaRepository extends JpaRepository<MemberAudioMeta, Long> {

    // VC TRG 오디오 url 추출
    @Query("""
        SELECT m.audioUrl 
        FROM MemberAudioMeta m 
        WHERE m.id IN :audioMetaIds 
          AND m.isDeleted = false 
          AND m.audioType = :audioType 
          AND m.audioUrl IS NOT NULL
    """)
    List<String> findAudioUrlsByAudioMetaIds(
            @Param("audioMetaIds") List<Long> audioMetaIds,
            @Param("audioType") AudioType audioType
    );
}
