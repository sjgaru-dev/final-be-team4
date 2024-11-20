package com.fourformance.tts_vc_web.repository;

import com.fourformance.tts_vc_web.common.constant.AudioType;
import com.fourformance.tts_vc_web.domain.entity.MemberAudioMeta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemberAudioMetaRepository extends JpaRepository<MemberAudioMeta, Long> {
    // 특정 사용자의 특정 AudioType을 가진 MemberAudioMeta를 조회
    List<MemberAudioMeta> findByMemberIdAndAudioType(Long memberId, AudioType audioType);

}
