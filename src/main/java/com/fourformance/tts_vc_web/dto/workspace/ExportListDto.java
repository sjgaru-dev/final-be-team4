package com.fourformance.tts_vc_web.dto.workspace;

import com.fourformance.tts_vc_web.service.common.S3Service;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;


@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class ExportListDto {


    private S3Service s3Service;

    Long outputAudioMetaId;
    String projectType;
    String projectName;
    String fileName;
    String script;
    String unitStatus;
    String downloadLink; // generatedPresignedUrL로 만들어전달
    LocalDateTime updateAt;

    public ExportListDto(String projectType, String projectName, String filename, String script,
                         String unitStatus, LocalDateTime updateAt, String audioPath) {
        this.projectType = projectType;
        this.projectName = projectName;
        this.fileName = fileName;
        this.script = script;
        this.unitStatus = unitStatus;
        this.updateAt = updateAt;
        this.downloadLink = s3Service.generatePresignedUrl(audioPath); // Presigned URL 생성
    }
}
