package com.fourformance.tts_vc_web.repository;

import com.fourformance.tts_vc_web.domain.entity.VCProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface VCProjectRepository extends JpaRepository<VCProject, Long> {

    // 프로젝트 ID로 member_audio_id 찾기 - 승민
    @Query("SELECT v.memberTargetAudioMeta.id FROM VCProject v WHERE v.id = :projectId")
    Long findMemberAudioIdByProjectId(Long projectId);
}
