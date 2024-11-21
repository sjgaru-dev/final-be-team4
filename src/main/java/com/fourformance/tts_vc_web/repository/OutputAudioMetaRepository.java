package com.fourformance.tts_vc_web.repository;

import com.fourformance.tts_vc_web.domain.entity.OutputAudioMeta;
import com.fourformance.tts_vc_web.dto.workspace.RecentExportDto;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface OutputAudioMetaRepository extends JpaRepository<OutputAudioMeta, Long> {



    //
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
}