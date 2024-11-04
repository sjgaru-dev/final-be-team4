package com.fourformance.tts_vc_web.domain.entity;

import com.fourformance.tts_vc_web.common.constant.ConcatStatusConst;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
public class ConcatStatusHistory {
    @Id
    @GeneratedValue
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