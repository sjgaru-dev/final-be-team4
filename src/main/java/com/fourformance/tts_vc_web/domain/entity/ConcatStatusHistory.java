package com.fourformance.tts_vc_web.domain.entity;

import com.fourformance.tts_vc_web.common.constant.ConcatStatusConst;
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
public class ConcatStatusHistory extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private ConcatProject concatProject;

    private ConcatStatusConst concatStatusConst;
    private LocalDateTime createdAt;

    // 상태 업데이트 메서드
    public void updateStatus(ConcatStatusConst concatStatusConst) {
        this.concatStatusConst = concatStatusConst;
    }

    // 생성 메서드
    public static ConcatStatusHistory createConcatStatusHistory(ConcatProject concatProject, ConcatStatusConst concatStatusConst) {
        ConcatStatusHistory history = new ConcatStatusHistory();
        history.concatProject = concatProject;
        history.concatStatusConst = concatStatusConst;
        history.createdAt = LocalDateTime.now();
        return history;
    }
}