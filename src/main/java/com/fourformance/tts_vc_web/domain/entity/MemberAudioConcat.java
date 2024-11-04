package com.fourformance.tts_vc_web.domain.entity;

import com.fourformance.tts_vc_web.domain.baseEntity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
public class MemberAudioConcat extends BaseEntity {
    @Id
    @GeneratedValue @Column(name = "member_audio_concat_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_auido_id")
    private MemberAudioMeta memberAudioMeta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private ConcatProject concatProject;

    // MemberAudioConcat 생성 메서드
    public static MemberAudioConcat createMemberAudioConcat(MemberAudioMeta memberAudioMeta, ConcatProject concatProject) {
        MemberAudioConcat memberAudioConcat = new MemberAudioConcat();
        memberAudioConcat.memberAudioMeta = memberAudioMeta;
        memberAudioConcat.concatProject = concatProject;
        return memberAudioConcat;
    }
}
