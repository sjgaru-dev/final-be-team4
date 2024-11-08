    package com.fourformance.tts_vc_web.domain.entity;

    import com.fourformance.tts_vc_web.common.constant.AudioFormat;
    import com.fourformance.tts_vc_web.common.constant.ProjectType;
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
    public class OutputAudioMeta extends BaseEntity {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "generated_audio_meta_id")
        private Long id;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "tts_detail_id")
        private TTSDetail ttsDetail;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "vc_detail_id")
        private VCDetail vcDetail;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "concat_project_id")
        private ConcatProject concatProject;

        @Enumerated(EnumType.STRING)
        private ProjectType projectType;
        private String audioUrl;
        private Boolean isDeleted=false;
        @Enumerated(EnumType.STRING)
        private AudioFormat audioFormat = AudioFormat.WAV;
        private LocalDateTime createdAt;
        private LocalDateTime deletedAt;

        // 생성 메서드
        public static OutputAudioMeta createOutputAudioMeta(TTSDetail ttsDetail, VCDetail vcDetail, ConcatProject concatProject,
                                                            ProjectType projectType, String audioUrl) {
            OutputAudioMeta outputAudioMeta = new OutputAudioMeta();
            outputAudioMeta.ttsDetail = ttsDetail;
            outputAudioMeta.vcDetail = vcDetail;
            outputAudioMeta.concatProject = concatProject;
            outputAudioMeta.projectType = projectType;
            outputAudioMeta.audioUrl = audioUrl;
            outputAudioMeta.createdAt = LocalDateTime.now();
            return outputAudioMeta;
        }

        // 삭제 메서드
        public void deleteOutputAudioMeta() {
            this.isDeleted = true;
            this.deletedAt = LocalDateTime.now();
        }

        // 복구 메서드
        public void restore() {
            this.isDeleted = false;
            this.deletedAt = null;
        }
    }
