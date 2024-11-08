package com.fourformance.tts_vc_web.domain.entity;

import com.fourformance.tts_vc_web.common.constant.AudioFormat;
import com.fourformance.tts_vc_web.common.constant.AudioType;
import com.fourformance.tts_vc_web.domain.baseEntity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@ToString
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberAudioMeta extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_audio_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private String audioUrl;
    private String script;
    @Enumerated(EnumType.STRING)
    private AudioType audioType;
    private Boolean isDeleted=false;
    @Enumerated(EnumType.STRING)
    private AudioFormat audioFormat = AudioFormat.WAV;
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;

    // 생성 메서드
    public static MemberAudioMeta createMemberAudioMeta(Member member, String audioUrl, AudioType audioType) {
        MemberAudioMeta memberAudioMeta = new MemberAudioMeta();
        memberAudioMeta.member = member;
        memberAudioMeta.audioUrl = audioUrl;
        memberAudioMeta.audioType = audioType;
        memberAudioMeta.createdAt = LocalDateTime.now();
        return memberAudioMeta;
    }

    // 삭제 메서드
    public void delete() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }
}