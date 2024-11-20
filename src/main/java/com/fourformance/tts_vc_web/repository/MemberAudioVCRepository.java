package com.fourformance.tts_vc_web.repository;

import com.fourformance.tts_vc_web.domain.entity.MemberAudioVC;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemberAudioVCRepository extends JpaRepository<MemberAudioVC, Long> {

    // VC 프로젝트 Id 로 member audio meta ID 추출하여 리스트로 반환 - 승민
    @Query("SELECT m.memberAudioMeta.id FROM MemberAudioVC m WHERE m.vcProject.id = :vcProjectId")
    List<Long> findMemberAudioMetaByVcProjectId(@Param("vcProjectId") Long vcProjectId);
}
