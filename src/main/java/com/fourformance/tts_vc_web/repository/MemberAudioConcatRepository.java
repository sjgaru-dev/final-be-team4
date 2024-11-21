package com.fourformance.tts_vc_web.repository;

import com.fourformance.tts_vc_web.domain.entity.MemberAudioConcat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemberAudioConcatRepository extends JpaRepository<MemberAudioConcat, Long> {

    // Concat 프로젝트 Id 로 member audio meta ID 추출하여 리스트로 반환 - 승민
    @Query("SELECT m.memberAudioMeta.id  FROM MemberAudioConcat m WHERE m.concatProject.id = :concatProjectId")
    List<Long> findMemberAudioMetaByVcProjectId(@Param("concatProjectId") Long concatProjectId);
}
