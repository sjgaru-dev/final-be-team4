package com.fourformance.tts_vc_web.repository;

import com.fourformance.tts_vc_web.domain.entity.OutputAudioMeta;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OutputAudioMetaRepository extends JpaRepository<OutputAudioMeta, Long> {

    // 최근 생성된 5개의 OutputAudioMeta 조회 (삭제되지 않은 데이터만)
    @Query("SELECT o FROM OutputAudioMeta o " +
            "WHERE o.isDeleted = false " +
            "ORDER BY o.createdAt DESC")
    List<OutputAudioMeta> findTop5RecentOutputAudioMeta();

    // Id로 생성된 오디오 찾아서 반환 - 승민
    @Query("SELECT o FROM OutputAudioMeta o WHERE o.id IN :Ids AND o.isDeleted = false")
    List<OutputAudioMeta> findByIds(@Param("Ids") List<Long> Ids);

    // TTS Detail Id로 생성된 오디오들을 찾아 리스트로 반환 - 승민
    @Query("SELECT o FROM OutputAudioMeta o WHERE o.ttsDetail.id = :ttsDetailIds AND o.isDeleted = false")
    List<OutputAudioMeta> findByTtsDetailIdAndIsDeletedFalse(@Param("ttsDetailIds") Long ttsDetailIds);

    // TTS Detail Id 리스트로 생성된 오디오들을 찾아 리스트로 반환 - 승민
    @Query("SELECT o FROM OutputAudioMeta o WHERE o.ttsDetail.id IN :ttsDetailIds AND o.isDeleted = false")
    List<OutputAudioMeta> findByTtsDetailAndIsDeletedFalse(@Param("ttsDetailIds") List<Long> ttsDetailIds);

    // VC Detail Id로 생성된 오디오들을 찾아 리스트로 반환 - 승민
    @Query("SELECT o FROM OutputAudioMeta o WHERE o.vcDetail.id = :vcDetailId AND o.isDeleted = false")
    List<OutputAudioMeta> findAudioUrlsByVcDetail(@Param("vcDetailId") Long vcDetailId);

    // TTS Detail Id로 생성된 오디오들을 찾아 리스트로 반환 - 승민
    @Query("SELECT o FROM OutputAudioMeta o WHERE o.vcDetail.id IN :vcDetailIds AND o.isDeleted = false")
    List<OutputAudioMeta> findByVcDetailAndIsDeletedFalse(@Param("vcDetailIds") List<Long> vcDetailIds);

    // Concat Project Id로 생성된 오디오를 찾아 리스트로 변환 - 의준
    @Query("SELECT o FROM OutputAudioMeta o WHERE o.concatProject.id = :concatProjectId AND o.isDeleted = false")
    List<OutputAudioMeta> findAudioUrlsByConcatProject(@Param("concatProjectId") Long concatProjectId);

    @Query(""" 
            SELECT o
            FROM OutputAudioMeta o
            LEFT JOIN o.ttsDetail t
            LEFT JOIN t.ttsProject tp
            LEFT JOIN o.vcDetail v
            LEFT JOIN v.vcProject vp
            LEFT JOIN o.concatProject c
            WHERE (tp.member.id = :memberId AND tp IS NOT NULL)
               OR (vp.member.id = :memberId AND vp IS NOT NULL)
               OR (c.member.id = :memberId AND c IS NOT NULL)
            ORDER BY o.createdAt DESC
            """)
    List<OutputAudioMeta> findTop5ByMemberId(@Param("memberId") Long memberId);

    // 최근 생성된 5개의 OutputAudioMeta 조회 (삭제되지 않은 데이터만)
//    @Query("SELECT o FROM OutputAudioMeta o " +
//            "WHERE o.isDeleted = false " +
//            "ORDER BY o.createdAt DESC")
//    List<OutputAudioMeta> findTop5RecentOutputAudioMeta();
//
//    // TTS Detail Id로 생성된 오디오들을 찾아 리스트로 반환 - 승민
//    @Query("SELECT o FROM OutputAudioMeta o WHERE o.ttsDetail.id IN :ttsDetailIds AND o.isDeleted = false")
//    List<OutputAudioMeta> findByTtsDetailAndIsDeletedFalse(@Param("ttsDetailIds") List<Long> ttsDetailIds);
//
//    // VC Detail Id로 생성된 오디오들을 찾아 리스트로 반환 - 승민
//    @Query("SELECT o FROM OutputAudioMeta o WHERE o.vcDetail.id = :vcDetailId AND o.isDeleted = false")
//    List<OutputAudioMeta> findAudioUrlsByVcDetail(@Param("vcDetailId") Long vcDetailId);
//
//    // TTS Detail Id로 생성된 오디오들을 찾아 리스트로 반환 - 승민
//    @Query("SELECT o FROM OutputAudioMeta o WHERE o.vcDetail.id IN :vcDetailIds AND o.isDeleted = false")
//    List<OutputAudioMeta> findByVcDetailAndIsDeletedFalse(@Param("vcDetailIds") List<Long> vcDetailIds);
//
//    // Concat Project Id로 생성된 오디오를 찾아 리스트로 변환 - 의준
//    @Query("SELECT o FROM OutputAudioMeta o WHERE o.concatProject.id = :concatProjectId AND o.isDeleted = false")
//    List<OutputAudioMeta> findAudioUrlsByConcatProject(@Param("concatProjectId") Long concatProjectId);


    @Query("SELECT oam FROM OutputAudioMeta oam " +
            "LEFT JOIN oam.ttsDetail td ON td.ttsProject.id = :projectId " +
            "LEFT JOIN oam.vcDetail vd ON vd.vcProject.id = :projectId " +
            "LEFT JOIN oam.concatProject cp ON cp.id = :projectId " +
            "WHERE td.ttsProject.id = :projectId OR vd.vcProject.id = :projectId OR cp.id = :projectId")
    List<OutputAudioMeta> findOutputAudioMetaByAnyProjectId(@Param("projectId") Long projectId);

}