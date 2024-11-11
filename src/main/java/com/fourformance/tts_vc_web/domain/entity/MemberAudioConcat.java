package com.fourformance.tts_vc_web.domain.entity;

import com.fourformance.tts_vc_web.domain.baseEntity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@ToString
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberAudioConcat extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_audio_concat_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_audio_id")
    private MemberAudioMeta memberAudioMeta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private ConcatProject concatProject;

    // 생성 메서드
    public static MemberAudioConcat createMemberAudioConcat(MemberAudioMeta memberAudioMeta, ConcatProject concatProject) {
        MemberAudioConcat memberAudioConcat = new MemberAudioConcat();
        memberAudioConcat.memberAudioMeta = memberAudioMeta;
        memberAudioConcat.concatProject = concatProject;
        return memberAudioConcat;
    }
}
