package com.fourformance.tts_vc_web.repository;

import com.fourformance.tts_vc_web.common.constant.AudioType;
import com.fourformance.tts_vc_web.domain.entity.MemberAudioMeta;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberAudioMetaRepository extends JpaRepository<MemberAudioMeta, Long> {

    // 특정 사용자의 특정 AudioType을 가진 MemberAudioMeta를 조회
    List<MemberAudioMeta> findByMemberIdAndAudioType(Long memberId, AudioType audioType);

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

    /**
     * S3에 업로드된 오디오 URL을 통해 MemberAudioMeta를 조회하는 메서드
     *
     * @param audioUrl 업로드된 오디오 파일의 URL
     * @return MemberAudioMeta 엔티티 (없으면 Optional.empty())
     */
    Optional<MemberAudioMeta> findFirstByAudioUrl(String audioUrl);

    // Concat Detail Id로 업로드된 오디오들을 찾아 리스트로 반환 - 의준
    @Query("SELECT m " +
            "FROM ConcatDetail c " +
            "JOIN c.memberAudioMeta m " +
            "WHERE c.id IN :concatDetailIds ")
    List<MemberAudioMeta> findByConcatDetailIds(@Param("concatDetailIds") List<Long> concatDetailIds);
}
