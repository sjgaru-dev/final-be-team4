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
import org.springframework.web.multipart.MultipartFile;

import java.text.SimpleDateFormat;
import java.util.Date;

@Service
@RequiredArgsConstructor // 자동 autowired
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
//    public OutputAudioMeta saveTTSOrVCAudioMeta(Long DetailId, ProjectType projectType, String audioUrl, AudioFormat audioFormat) {
//        TTSDetail ttsDetail = (ttsDetailId != null)
//                ? ttsDetailRepository.findById(ttsDetailId).orElse(null)
//                : null;
//
//        VCDetail vcDetail = (vcDetailId != null)
//                ? vcDetailRepository.findById(vcDetailId).orElse(null)
//                : null;
//
//        if (ttsDetail == null && vcDetail == null) {
//            throw new IllegalArgumentException("TTSDetailId 또는 VCDetailId 중 하나는 반드시 존재해야 합니다.");
//        }
//
//
//        OutputAudioMeta outputAudioMeta = OutputAudioMeta.createOutputAudioMeta(
//                ttsDetail, vcDetail, null, projectType, audioUrl, audioFormat
//        );
//
//        return outputAudioMetaRepository.save(outputAudioMeta);
//    }


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



            return fileUrl;

        }catch (Exception e) {
            e.printStackTrace();
            throw new Exception();
        }
    }




    //myEntityRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Entity not found with id " + id));
}
