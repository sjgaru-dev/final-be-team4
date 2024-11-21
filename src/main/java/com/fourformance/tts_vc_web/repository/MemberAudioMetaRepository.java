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

    // id 리스트로 특정 오디오 타입 반환 - 승민
    @Query("""
        SELECT m 
        FROM MemberAudioMeta m 
        WHERE m.id IN :memberAudioIds 
          AND m.isDeleted = false 
          AND m.audioType = :audioType
    """)
    List<MemberAudioMeta> findByMemberAudioIds(
            @Param("memberAudioIds") List<Long> memberAudioIds,
            @Param("audioType") AudioType audioType
    );



}
