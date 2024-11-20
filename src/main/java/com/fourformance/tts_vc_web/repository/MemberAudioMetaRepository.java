package com.fourformance.tts_vc_web.repository;

import com.fourformance.tts_vc_web.domain.entity.MemberAudioMeta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberAudioMetaRepository extends JpaRepository<MemberAudioMeta, Long> {
    /**
     * S3에 업로드된 오디오 URL을 통해 MemberAudioMeta를 조회하는 메서드
     *
     * @param audioUrl 업로드된 오디오 파일의 URL
     * @return MemberAudioMeta 엔티티 (없으면 Optional.empty())
     */
    Optional<MemberAudioMeta> findFirstByAudioUrl(String audioUrl);
}
