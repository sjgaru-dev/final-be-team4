package com.fourformance.tts_vc_web.service.common;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.fourformance.tts_vc_web.common.constant.AudioType;
import com.fourformance.tts_vc_web.common.constant.ProjectType;
import com.fourformance.tts_vc_web.common.exception.common.BusinessException;
import com.fourformance.tts_vc_web.common.exception.common.ErrorCode;
import com.fourformance.tts_vc_web.domain.entity.ConcatProject;
import com.fourformance.tts_vc_web.domain.entity.Member;
import com.fourformance.tts_vc_web.domain.entity.MemberAudioMeta;
import com.fourformance.tts_vc_web.domain.entity.OutputAudioMeta;
import com.fourformance.tts_vc_web.domain.entity.Project;
import com.fourformance.tts_vc_web.domain.entity.TTSDetail;
import com.fourformance.tts_vc_web.domain.entity.TTSProject;
import com.fourformance.tts_vc_web.domain.entity.VCDetail;
import com.fourformance.tts_vc_web.domain.entity.VCProject;
import com.fourformance.tts_vc_web.repository.ConcatProjectRepository;
import com.fourformance.tts_vc_web.repository.MemberAudioMetaRepository;
import com.fourformance.tts_vc_web.repository.MemberRepository;
import com.fourformance.tts_vc_web.repository.OutputAudioMetaRepository;
import com.fourformance.tts_vc_web.repository.ProjectRepository;
import com.fourformance.tts_vc_web.repository.TTSDetailRepository;
import com.fourformance.tts_vc_web.repository.VCDetailRepository;
import java.io.IOException;
import java.net.URL;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
    private final MemberAudioMetaRepository memberAudioMetaRepository;
    private final MemberRepository memberRepository;

    // TTS와 VC로 반환한 유닛 오디오를 S3 버킷에 저장
    public String uploadUnitSaveFile(MultipartFile file, Long userId, Long projectId, Long detailId) {

        try {
            // 파일이 비어 있는지 확인
            if (file.isEmpty()) {
                throw new BusinessException(ErrorCode.EMPTY_FILE);
            }

            // Project의 실제 타입에 따라 ProjectType 설정
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

            ProjectType projectType;
            String fileName;
            if (project instanceof TTSProject) {
                projectType = ProjectType.TTS;
                fileName = "Generated/" + userId + "/" + projectType + "/" + projectId + "/" + detailId + ".wav";
            } else if (project instanceof VCProject) {
                projectType = ProjectType.VC;
                // 오디오파일 이름으로 사용할 날짜 포맷 지정
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
                String timeStamp = sdf.format(new Date());
                fileName =
                        "Generated/" + userId + "/" + projectType + "/" + projectId + "/" + detailId + "/" + timeStamp
                                + ".wav";
            } else {
                throw new BusinessException(ErrorCode.UNSUPPORTED_PROJECT_TYPE);
            }

            // 메타데이터 저장
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());

            // S3에 파일 업로드 (전체 경로 포함)
            amazonS3Client.putObject(bucket, fileName, file.getInputStream(), metadata);

            // 파일의 전체 URL 반환
            String fileUrl = amazonS3Client.getUrl(bucket, fileName).toString();

            // 반환된 오디오 메타를 DB에 저장
            saveTTSOrVCOutputAudioMeta(fileName, detailId, projectType, fileUrl);

            return fileUrl;

        } catch (AmazonClientException e) {
            // S3 업로드 중 발생하는 예외
            throw new BusinessException(ErrorCode.S3_UPLOAD_FAILED);
        } catch (IOException e) {
            // 파일 처리 중 발생하는 예외
            throw new BusinessException(ErrorCode.FILE_PROCESSING_ERROR);
        }
    }

    // Concat 기능을 수행해서 반환한 오디오를 S3 버킷에 저장
    public String uploadConcatSaveFile(MultipartFile file, Long userId, Long projectId) {

        try {
            // 파일이 비어 있는지 확인
            if (file.isEmpty()) {
                throw new BusinessException(ErrorCode.EMPTY_FILE);
            }

            // 오디오파일 이름으로 사용할 날짜 포맷 지정
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String timeStamp = sdf.format(new Date());

            // 전체 경로를 포함한 파일 이름 설정
            String fileName = "Generated/" + userId + "/CONCAT" + "/" + projectId + "/" + timeStamp + ".wav";

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());

            // S3에 파일 업로드 (전체 경로 포함)
            amazonS3Client.putObject(bucket, fileName, file.getInputStream(), metadata);

            // 파일의 전체 URL 반환
            String fileUrl = amazonS3Client.getUrl(bucket, fileName).toString();

            // 반환된 오디오 메타를 DB에 저장
            saveConcatAudioMeta(fileName, projectId, fileUrl);

            return fileUrl;

        } catch (AmazonClientException e) {
            // S3 업로드 중 발생하는 예외
            throw new BusinessException(ErrorCode.S3_UPLOAD_FAILED);
        } catch (IOException e) {
            // 파일 처리 중 발생하는 예외
            throw new BusinessException(ErrorCode.FILE_PROCESSING_ERROR);
        }
    }

    // DB에 TTS와 VC에 대한 오디오 메타를 저장
    public OutputAudioMeta saveTTSOrVCOutputAudioMeta(String fileName, Long detailId, ProjectType projectType,
                                                      String audioUrl) {

        TTSDetail ttsDetail;
        VCDetail vcDetail;

        // TTS 프로젝트인지 VC 프로젝트인지 판별
        if (projectType.equals(ProjectType.TTS)) {
            ttsDetail = ttsDetailRepository.findById(detailId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.DETAIL_NOT_FOUND));
            vcDetail = null;
        } else if (projectType.equals(ProjectType.VC)) {
            vcDetail = vcDetailRepository.findById(detailId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.DETAIL_NOT_FOUND));
            ttsDetail = null;
        } else {
            throw new BusinessException(ErrorCode.UNSUPPORTED_PROJECT_TYPE);
        }

        // OutputAudioMeta 객체 생성
        OutputAudioMeta outputAudioMeta = OutputAudioMeta.createOutputAudioMeta(fileName, ttsDetail, vcDetail, null,
                projectType, audioUrl);

        // 생성한 OutputAudioMeta를 DB에 저장
        return outputAudioMetaRepository.save(outputAudioMeta);
    }

    // DB에 Concat 기능을 수행해서 반환한 오디오 메타를 저장
    public OutputAudioMeta saveConcatAudioMeta(String fileName, Long projectId, String audioUrl) {
        ConcatProject concatProject = concatProjectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

        // OutputAudioMeta 객체 생성
        OutputAudioMeta outputAudioMeta = OutputAudioMeta.createOutputAudioMeta(fileName, null, null, concatProject,
                ProjectType.CONCAT, audioUrl);

        // 생성한 OutputAudioMeta를 DB에 저장
        return outputAudioMetaRepository.save(outputAudioMeta);
    }

    // 다운로드 받을 오디오의 버킷 URL을 제공하는 메서드
    public String generatePresignedUrl(String bucketRoute) {
        try {
            // presigned url 생성
            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucket, bucketRoute);
            request.withMethod(com.amazonaws.HttpMethod.GET)
                    .withExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 5));
            URL presignedUrl = amazonS3Client.generatePresignedUrl(request);

            return presignedUrl.toString();
        } catch (AmazonClientException e) {
            // Presigned URL 생성 실패 예외
            throw new BusinessException(ErrorCode.S3_PRESIGNED_URL_FAILED);
        }
    }

    // 유저 오디오를 S3에 업로드하고 DB에 저장하는 메서드
    public List<String> uploadAndSaveMemberFile(List<MultipartFile> files, Long memberId, Long projectId,
                                                AudioType audioType) {

        try {
            // url을 담을 리스트
            List<String> uploadedUrls = new ArrayList<>();
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));
            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

            // 개별 파일 url을 List로 저장.
            for (MultipartFile file : files) {
                if (file.isEmpty()) {
                    throw new BusinessException(ErrorCode.EMPTY_FILE);
                }

                String originFilename = Normalizer.normalize(file.getOriginalFilename(), Normalizer.Form.NFC);
                String filename = "member/" + memberId + "/" + audioType + "/" + projectId + "/" + originFilename;

                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentType(file.getContentType());
                metadata.setContentLength(file.getSize());

                // 버킷에 업로드
                amazonS3Client.putObject(bucket, filename, file.getInputStream(), metadata);
                String fileUrl = amazonS3Client.getUrl(bucket, filename).toString();

                // url 리스트에 추가
                uploadedUrls.add(fileUrl);

                // 오디오 메타 객체 생성 및 DB 저장
                MemberAudioMeta memberAudioMeta = MemberAudioMeta.createMemberAudioMeta(member, filename, fileUrl,
                        audioType,null);  // 수정필요합니다
                memberAudioMetaRepository.save(memberAudioMeta);
            }

            return uploadedUrls;

        } catch (AmazonClientException e) {
            // S3 업로드 중 발생하는 예외
            throw new BusinessException(ErrorCode.S3_UPLOAD_FAILED);
        } catch (IOException e) {
            // 파일 처리 중 발생하는 예외
            throw new BusinessException(ErrorCode.FILE_PROCESSING_ERROR);
        }
    }
}