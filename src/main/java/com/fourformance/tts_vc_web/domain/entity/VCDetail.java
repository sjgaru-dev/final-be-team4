package com.fourformance.tts_vc_web.domain.entity;


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
@Table(name = "vc_detail")
public class VCDetail extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vc_detail_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private VCProject vcProject;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_audio_id")
    private MemberAudioMeta memberAudioMeta;

    private Boolean isChecked = false;
    private String unitScript;
    private Boolean isDeleted=false;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    // 생성메서드
    // 오디오를 유저오디오_메타에서 가져오든 로컬에서 가져오든 이 메서드로 VC디테일을 생성합니다.
    // 프로젝트 저장시점에 이 객체의 유저오디오_메타가 null이라면, 유저오디오_메타 DB에 저장하는 로직은 담당하신 분이 서비스에서 구현하셔야 합니다.
    // 유저오디오를 S3에 업로드하는 작업은 team_aws가 합니다. team_aws에서 만든 서비스를 호출해서 사용하시면 됩니다.
    public static VCDetail createVCDetail(VCProject vcProject, MemberAudioMeta memberAudioMeta) {
        VCDetail vcDetail = new VCDetail();
        vcDetail.vcProject = vcProject;
        vcDetail.memberAudioMeta = memberAudioMeta;
        vcDetail.createdAt = LocalDateTime.now();
        vcDetail.updatedAt = LocalDateTime.now();
        return vcDetail;
    }

    // 로컬 오디오 주입 메서드
    // 로컬에 있는 오디오를 업로드 하는 경우 생성 메서드에서는 memberAudioMeta를 null로 받습니다.
    // 이때 객체에 로컬 오디오 메타 정보를 주입해야하기 때문에 그때 사용하는 메서드입니다.
    // 마찬가지로 저장 버튼이 눌렸을때 DB에 저장하는 로직은 알아서 구현하시면 됩니다.
    public void injectLocalAudio(MemberAudioMeta localAudioMeta) {
        this.memberAudioMeta = localAudioMeta;
    }

    // 업데이트 메서드
    public void updateDetails(Boolean newIsChecked, String newUnitScript) {
        this.isChecked = newIsChecked;
        this.unitScript = newUnitScript;
        this.updatedAt = LocalDateTime.now();
    }

    // 삭제 메서드
    public void markAsDeleted() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }
}
