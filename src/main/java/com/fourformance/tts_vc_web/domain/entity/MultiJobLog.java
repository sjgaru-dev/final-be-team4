package com.fourformance.tts_vc_web.domain.entity;

import com.fourformance.tts_vc_web.common.constant.MultiJobLogStatusConst;
import com.fourformance.tts_vc_web.common.constant.ProjectType;
import com.fourformance.tts_vc_web.domain.baseEntity.BaseEntity;
import jakarta.persistence.*;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    private String projectName;

    @Enumerated(EnumType.STRING)
    private ProjectType projectType;

    @Enumerated(EnumType.STRING)
    private MultiJobLogStatusConst multiJobLogStatusConst;

    private String failBy;
    private String comment;
    private Integer sequence;
    private LocalDateTime createdAt;

    private LocalDateTime endedAt;  // 컬럼명을 endedAt이라고 했지만 종료시점 보다는 API Status가 바뀌는 시점이라고 이해시하면 좋습니다!
    // API 상태가 변경되면 이 객체의 endedAt을 now로 업데이트 시키고 새로운 객체에 새로운 상태를 담으면 됩니다!


    public static MultiJobLog createMultiJobLog(Project project, MultiJobLogStatusConst multiJobLogStatusConst,
                                                String failBy, String comment, Integer sequence) {

        MultiJobLog multiJobLog = new MultiJobLog();
        multiJobLog.project = project;
        multiJobLog.projectName = project.getProjectName(); // Project에서 이름 가져오기

        // Project의 실제 타입에 따라 ProjectType 설정
        if (project instanceof VCProject) {
            multiJobLog.projectType = ProjectType.VC;
        } else if (project instanceof TTSProject) {
            multiJobLog.projectType = ProjectType.TTS;
        } else if (project instanceof ConcatProject) {
            multiJobLog.projectType = ProjectType.CONCAT;
        }

        multiJobLog.multiJobLogStatusConst = multiJobLogStatusConst;
        multiJobLog.failBy = failBy;
        multiJobLog.createdAt = LocalDateTime.now();
        multiJobLog.comment = comment;
        multiJobLog.sequence = sequence;
        return multiJobLog;
    }

    // 종료시점 저장 메서드
    public void endAPIStatus() {
        this.endedAt = LocalDateTime.now();
    }

}

