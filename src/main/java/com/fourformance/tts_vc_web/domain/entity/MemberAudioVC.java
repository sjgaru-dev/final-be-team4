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
public class MemberAudioVC extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_audio_vc_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_audio_id")
    private MemberAudioMeta memberAudioMeta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private VCProject vcProject;

    // 생성 메서드
    public static MemberAudioVC createMemberAudioVC(MemberAudioMeta memberAudioMeta, VCProject vcProject) {
        MemberAudioVC memberAudioVC = new MemberAudioVC();
        memberAudioVC.memberAudioMeta = memberAudioMeta;
        memberAudioVC.vcProject = vcProject;
        return memberAudioVC;
    }
}
