package com.fourformance.tts_vc_web.domain.entity;

import com.fourformance.tts_vc_web.common.constant.MultiJobLogStatusConst;
import com.fourformance.tts_vc_web.common.constant.ProjectType;
import com.fourformance.tts_vc_web.domain.baseEntity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity(name = "multi_job_log")
@ToString
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MultiJobLog extends BaseEntity {
    @Id
    @GeneratedValue
    @Column(name = "log_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    private String projectName;

    private ProjectType projectType;

    private MultiJobLogStatusConst multiJobLogStatusConst;

    private String failBy;

    private LocalDateTime createdAt;

    private String comment;

    @Column(length = 100)
    private Integer orders;

    // MultiJobLog 생성 메서드
    public static MultiJobLog createMultiJobLog(Project project, String projectName, ProjectType projectType,
                                                MultiJobLogStatusConst multiJobLogStatusConst, String failBy,
                                                String comment, Integer orders) {
        MultiJobLog multiJobLog = new MultiJobLog();
        multiJobLog.project = project;
        multiJobLog.projectName = projectName;
        multiJobLog.projectType = projectType;
        multiJobLog.multiJobLogStatusConst = multiJobLogStatusConst;
        multiJobLog.failBy = failBy;
        multiJobLog.createdAt = LocalDateTime.now();
        multiJobLog.comment = comment;
        multiJobLog.orders = orders;
        return multiJobLog;
    }
}
