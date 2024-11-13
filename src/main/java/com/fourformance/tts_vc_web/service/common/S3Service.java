package com.fourformance.tts_vc_web.service.common;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.fourformance.tts_vc_web.common.constant.AudioFormat;
import com.fourformance.tts_vc_web.common.constant.ProjectType;
import com.fourformance.tts_vc_web.domain.entity.*;
import com.fourformance.tts_vc_web.repository.ConcatProjectRepository;
import com.fourformance.tts_vc_web.repository.OutputAudioMetaRepository;
import com.fourformance.tts_vc_web.repository.ProjectRepository;
import com.fourformance.tts_vc_web.repository.TTSDetailRepository;
import com.fourformance.tts_vc_web.repository.VCDetailRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Transactional
public class S3Service {

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final OutputAudioMetaRepository outputAudioMetaRepository;
    private final TTSDetailRepository ttsDetailRepository;
    private final VCDetailRepository vcDetailRepository;
    private final ConcatProjectRepository concatProjectRepository;
    private final AmazonS3Client amazonS3Client;
    private final ProjectRepository projectRepository;

    // TTS와 VC로 반환한 유닛 오디오를 S3 버킷에 저장
    public String uploadUnitSaveFile(MultipartFile file, Long userId, Long projectId, Long detailId) throws Exception {

        try {
            // 오디오파일 이름으로 사용할 날짜 포맷 지정
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String timeStamp = sdf.format(new Date());

            // Project의 실제 타입에 따라 ProjectType 설정
            Project project = projectRepository.findById(projectId).orElse(null);
            ProjectType projectType = null;
            if (project instanceof VCProject) {
                 projectType = ProjectType.VC;
            } else if (project instanceof TTSProject) {
                 projectType = ProjectType.TTS;
            } else if (project instanceof ConcatProject) {
                 projectType = ProjectType.CONCAT;
            }

            // 전체 경로를 포함한 파일 이름 설정
            String fileName = "Generated/" + userId + "/" + projectType + "/" + projectId + "/" + detailId + "/" + timeStamp + ".wav";

            // 메타데이터 저장
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType()); // wav
            metadata.setContentLength(file.getSize()); // 업로드 할 때 사이즈 반드시 필요

            // S3에 파일 업로드 (전체 경로 포함)
            amazonS3Client.putObject(bucket, fileName, file.getInputStream(), metadata);

            // 파일의 전체 URL 반환
            String fileUrl = amazonS3Client.getUrl(bucket, fileName).toString();

            // 반환된 오디오 메타를 DB에 저장
            saveTTSOrVCOutputAudioMeta(detailId, projectType, fileUrl);

            return fileUrl;

        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("파일 업로드 실패", e);
        }
    }

    // Concat 기능을 수행해서 반환한 오디오를 S3 버킷에 저장
    public String uploadConcatSaveFile(MultipartFile file, Long userId, Long projectId) throws Exception {

        try {
            // 오디오파일 이름으로 사용할 날짜 포맷 지정
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String timeStamp = sdf.format(new Date());

            // 전체 경로를 포함한 파일 이름 설정

            String fileName = "Generated/" + userId + "/CONCAT" + "/" + projectId + "/" + timeStamp + ".wav";

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType()); // wav
            metadata.setContentLength(file.getSize()); // 업로드 할 때 반드시 사이즈 필요

            // S3에 파일 업로드 (전체 경로 포함)
            amazonS3Client.putObject(bucket, fileName, file.getInputStream(), metadata);

            // 파일의 전체 URL 반환
            String fileUrl = amazonS3Client.getUrl(bucket, fileName).toString();

            // 반환된 오디오 메타를 DB에 저장
            saveConcatAudioMeta(projectId, fileUrl);

            return fileUrl;

        }catch (Exception e) {
            e.printStackTrace();
            throw new Exception();
        }
    }


    // DB에 TTS와 VC에 대한 오디오 메타를 저장
    public OutputAudioMeta saveTTSOrVCOutputAudioMeta(Long detailId, ProjectType projectType, String audioUrl) {

        TTSDetail ttsDetail;
        VCDetail vcDetail;

        // TTS 프로젝트인지 VC 프로젝트인지 판별
        if (projectType.equals(ProjectType.TTS)) {
             ttsDetail = ttsDetailRepository.findById(detailId).orElse(null);
             vcDetail = null;
        }
        else if (projectType.equals(ProjectType.VC)) {
             vcDetail = vcDetailRepository.findById(detailId).orElse(null);
             ttsDetail = null;
        }
        else {
            throw new IllegalArgumentException("DetailId는 TTS 또는 VC 중 하나의 ID여야 합니다.");
        }

        // OutputAudioMeta 객체 생성
        OutputAudioMeta outputAudioMeta = OutputAudioMeta.createOutputAudioMeta(
                ttsDetail, vcDetail, null, projectType, audioUrl
        );

        // 생성한 OutputAudioMeta를 DB에 저장
        return outputAudioMetaRepository.save(outputAudioMeta);
    }

    // DB에 Concat 기능을 수행해서 반환한 오디오 메타를 저장
    public OutputAudioMeta saveConcatAudioMeta(Long projectId , String audioUrl) {
        ConcatProject concatProject = concatProjectRepository.findById(projectId).orElse(null);


        // 존재하는 프로젝트인지 판별
        if (concatProject == null) {
            throw new IllegalArgumentException("ProjectId는 반드시 존재해야합니다.");
        }

        // OutputAudioMeta 객체 생성
        OutputAudioMeta outputAudioMeta = OutputAudioMeta.createOutputAudioMeta(
                null,null, concatProject, ProjectType.CONCAT, audioUrl
        );

        // 생성한 OutputAudioMeta를 DB에 저장
        return outputAudioMetaRepository.save(outputAudioMeta);
    }



    public String generatePresignedUrl(Long userId,Long projectId, Long detailId, String fileName) throws Exception{
        try{

            // Project의 실제 타입에 따라 ProjectType 설정
            Project project = projectRepository.findById(projectId).orElse(null);
            ProjectType projectType = null;
            String filePath = null;
            if (project instanceof TTSProject) {
                projectType = ProjectType.TTS;
                filePath = "Generated/" + userId + "/" + projectType + "/"  + projectId + "/" + detailId + "/" + fileName;
            } else if (project instanceof VCProject) {
                projectType = ProjectType.VC;
                filePath = "Generated/" + userId + "/" + projectType + "/"  + projectId + "/" + detailId + "/" + fileName;
            } else if (project instanceof ConcatProject) {
                projectType = ProjectType.CONCAT;
                filePath = "Generated/" + userId + "/" + projectType + "/"  + projectId +  "/" + fileName;
            } else {
                throw new IllegalArgumentException("지정된 타입의 프로젝트가 들어와야 합니다.");
            }

            // presigned url 생성
            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucket, filePath);
            request.withMethod(com.amazonaws.HttpMethod.GET)
                    .withExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 5));
            URL presignedUrl = amazonS3Client.generatePresignedUrl(request);

            return presignedUrl.toString();


        }catch(Exception e){
            e.printStackTrace();
            throw new Exception();
        }
    }

    //myEntityRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Entity not found with id " + id));
}
