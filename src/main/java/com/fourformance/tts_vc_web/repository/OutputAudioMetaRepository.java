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

    // TTS Detail Id로 생성된 오디오들을 찾아 리스트로 반환 - 승민
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


    @Query("SELECT oam " +
            "FROM OutputAudioMeta oam " +
            "LEFT JOIN oam.ttsDetail td " +
            "LEFT JOIN td.project p1 " +
            "LEFT JOIN oam.vcDetail vd " +
            "LEFT JOIN vd.project p2 " +
            "LEFT JOIN oam.concatProject cp " +
            "LEFT JOIN cp.project p3 " +
            "WHERE p1.id = :projectId OR p2.id = :projectId OR p3.id = :projectId")
    List<OutputAudioMeta> findOutputAudioMetaByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT oam FROM OutputAudioMeta oam " +
            "JOIN oam.ttsDetail td " +
            "JOIN td.project p " +
            "WHERE p.id = :projectId")
    List<OutputAudioMeta> findOutputAudioMetaByTTSProjectId(@Param("projectId") Long projectId);

    @Query("SELECT oam FROM OutputAudioMeta oam " +
            "JOIN oam.vcDetail vd " +
            "JOIN vd.vcProject p " +
            "WHERE p.id = :projectId")
    List<OutputAudioMeta> findOutputAudioMetaByVCProjectId(@Param("projectId") Long projectId);

    @Query("SELECT oam FROM OutputAudioMeta oam " +
            "JOIN oam.concatProject p " +
            "WHERE p.id = :projectId")
    List<OutputAudioMeta> findOutputAudioMetaByConcatProjectId(@Param("projectId") Long projectId);


}