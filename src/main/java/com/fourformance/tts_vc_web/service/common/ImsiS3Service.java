package com.fourformance.tts_vc_web.service.common;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.fourformance.tts_vc_web.common.constant.AudioFormat;
import com.fourformance.tts_vc_web.common.constant.ProjectType;
import com.fourformance.tts_vc_web.domain.entity.*;
import com.fourformance.tts_vc_web.repository.ConcatProjectRepository;
import com.fourformance.tts_vc_web.repository.OutputAudioMetaRepository;
import com.fourformance.tts_vc_web.repository.TTSDetailRepository;
import com.fourformance.tts_vc_web.repository.VCDetailRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.text.SimpleDateFormat;
import java.util.Date;

@Service
@RequiredArgsConstructor // 자동 autowired
@Transactional
public class ImsiS3Service {

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final OutputAudioMetaRepository outputAudioMetaRepository; // save할 예정

    /**
     * tts/vc/concat에 대해서 저장할 때 id가 필요하므로 이렇게 주입받음.
     */
    private final TTSDetailRepository ttsDetailRepository;
    private final VCDetailRepository vcDetailRepository;
    private final ConcatProjectRepository concatProjectRepository;
    private final AmazonS3Client amazonS3Client;

    private final String BASE_ROUTE = "https://popomance.s3.amazonaws.com/";


    // DB에 저장하기 위한 메서드.
    public OutputAudioMeta saveTTSOrVCAudioMeta(Long detailId, ProjectType projectType, String audioUrl) {
        TTSDetail ttsDetail = ttsDetailRepository.findById(detailId).orElse(null);
        VCDetail vcDetail = null;

        // TTSDetail이 null인 경우 VCDetail을 찾는다.
        if (ttsDetail == null) {
            vcDetail = vcDetailRepository.findById(detailId).orElse(null);
        }

        // TTSDetail과 VCDetail 둘 다 null인 경우 예외 발생
        if (ttsDetail == null && vcDetail == null) {
            throw new IllegalArgumentException("DetailId는 TTS 또는 VC 중 하나의 ID여야 합니다.");
        }

        OutputAudioMeta outputAudioMeta = OutputAudioMeta.createOutputAudioMeta(
                ttsDetail, vcDetail, null, projectType, audioUrl
        );

        return outputAudioMetaRepository.save(outputAudioMeta);
    }

    public OutputAudioMeta saveConcatAudioMeta(Long projectId , String audioUrl) {
        ConcatProject concatProject = concatProjectRepository.findById(projectId).orElse(null);


        // TTSDetail과 VCDetail 둘 다 null인 경우 예외 발생
        if (concatProject == null) {
            throw new IllegalArgumentException("ProjectId는 반드시 존재해야합니다.");
        }

        OutputAudioMeta outputAudioMeta = OutputAudioMeta.createOutputAudioMeta(
                null,null, concatProject, ProjectType.CONCAT, audioUrl
        );

        return outputAudioMetaRepository.save(outputAudioMeta);
    }

    // 이제 컨트롤러에서 분리해보자.
    // 어떻게...?
    // 오디오 파일 업로드 메서드
    public String uploadUnitSaveFile(MultipartFile file, String userId, Long projectId, Long detailId, String projectType) throws Exception {

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String timeStamp = sdf.format(new Date());

            String fileName = timeStamp + ".wav";

            String fileUrl = BASE_ROUTE + userId + "/" + projectId + "/" + detailId + "/" + fileName;

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());
            amazonS3Client.putObject(bucket, fileName, file.getInputStream(), metadata);

            saveTTSOrVCAudioMeta(detailId, ProjectType.valueOf(projectType), fileUrl);

            return fileUrl;

        }catch (Exception e) {
            e.printStackTrace();
            throw new Exception();
        }
    }


    public String uploadConcatSaveFile(MultipartFile file, String userId, Long projectId) throws Exception {

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String timeStamp = sdf.format(new Date());

            String fileName = timeStamp + ".wav";

            String fileUrl = BASE_ROUTE + userId + "/" + projectId + "/" + fileName;

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType()); // wav
            metadata.setContentLength(file.getSize()); // 업로드 할 때 반드시 사이즈 필요
            amazonS3Client.putObject(bucket, fileName, file.getInputStream(), metadata);

            saveConcatAudioMeta(projectId, fileUrl);

            return fileUrl;

        }catch (Exception e) {
            e.printStackTrace();
            throw new Exception();
        }
    }


    //myEntityRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Entity not found with id " + id));
}
