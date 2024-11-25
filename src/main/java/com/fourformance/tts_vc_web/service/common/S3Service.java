package com.fourformance.tts_vc_web.service.common;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

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

    private final AmazonS3 amazonS3;


    //프로젝트 단위 삭제
    public void deleteAudioPerProject(Long projectId) {

        // 2. 디렉토리 경로 추출 "Generated/" + memberid
//        String filePath = outputAudioMeta.getBucketRoute(); // 예: "Generated/123/TTS/456/789.wav"
        String directoryPrefix = extractProjectDirectoryPrefix(filePath); // "Generated/123/TTS/456/"

        /**
         * 안전한 처리 순서  : 안전한 처리 순서: S3 삭제 -> DB 업데이트
         * 이 순서가 일반적으로 더 안전합니다.
         * S3 삭제 작업이 우선인 이유는, S3에서의 삭제 실패가 비가역적이기 때문입니다.
         * 파일이 삭제되면 복구하기 어렵지만, DB 상태는 나중에 처리할 수 있기 때문입니다.
         */
        // 3. S3 디렉토리 삭제
        deleteDirectoryFromS3(directoryPrefix);

        // 4. OutputAudioMeta 업데이트
        outputAudioMeta.deleteOutputAudioMeta(); // 업데이트 치고,
        outputAudioMetaRepository.save(outputAudioMeta); // 저장


    }

    // MemberAudioMeta isdeleted update, S3File Delete//  멤버오디오메타 경로추출 + 삭제
    public void deleteMemberAudio(Long memberId, Long projectId) {
        MemberAudioMeta memberAudioMeta = memberAudioMetaRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("멤버 오디오를 찾을 수 없습니다."));

        String prefix = "member/" + memberId + "/";

        if ("VC_SRC".equals(memberAudioMeta.getAudioType())) {
            prefix += "VC_SRC/" + projectId + "/";
        } else if ("VC_TRG".equals(memberAudioMeta.getAudioType())) {
            prefix += "VC_TRG/" + projectId + "/";
        } else if ("CONCAT".equals(memberAudioMeta.getAudioType())) {
            prefix += "CONCAT/" + projectId + "/";
        }
        // S3 디렉토리수준 삭제
        deleteDirectoryFromS3(prefix);
        // DB 업데이트
        memberAudioMeta.delete();// isDeleted = ture, 삭제시간 업데이트 메서드
        memberAudioMetaRepository.save(memberAudioMeta);
    }

//    // 경로 추출 헬프메서드 - OutputAudioMeta경로 추출
//    private String extractProjectDirectoryPrefix(String filePath) {
//        // 슬래시(`/`) 기준으로 분리
//        String[] parts = filePath.split("/");
//
//        // 경로의 최소 길이를 확인
//        if (parts.length < 4) {
//            throw new RuntimeException("Invalid file path: " + filePath);
//        }
//
//        // projectId까지 포함된 경로를 반환
//        return String.join("/", parts[0], parts[1], parts[2], parts[3]) + "/";
//    }


    // Prefix + bucket 값만 있으면 S3에서 삭제 가능
    public void deleteDirectoryFromS3(String directoryPrefix) {
        try {
            // listObjects라는 메서드를 통해서 버킷에서 object리스트를 가지고 옴.
            ObjectListing objectListing = amazonS3.listObjects(bucket, directoryPrefix);

            while (true) {
                // 삭제할 키들을 저장 할 곳
                List<DeleteObjectsRequest.KeyVersion> keysToDelete = new ArrayList<>();

                // 객체의 키에 삭제할 ObjectList들의 정보들을 담는다.
                for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
                    keysToDelete.add(new DeleteObjectsRequest.KeyVersion(objectSummary.getKey()));
                    System.out.println("Deleting: " + objectSummary.getKey());
                }

                // 만약 삭제할 키가 있다면
                if (!keysToDelete.isEmpty()) {
                    DeleteObjectsRequest deleteRequest = new DeleteObjectsRequest(bucket)
                            .withKeys(keysToDelete)
                            .withQuiet(false); // 기본값 false  삭제된 각 객체에 대한 정보를 로그로 출력
                    amazonS3.deleteObjects(deleteRequest);// 객체를 삭제하는 요청을 실제로 실행하는 메서드
                    System.out.println("배치 삭제 완료");
                }

                if (objectListing.isTruncated()) {  // 목록을 처리하면서 더 많은 객체가 있는지 확인.true면 더 많은 객체 존재함. false면 마지막 목록이다라는 뜻
                    objectListing = amazonS3.listNextBatchOfObjects(objectListing);
                } else {
                    break;
                }
            }
            System.out.println("All objects under directory deleted: " + directoryPrefix);
        } catch (Exception e) {
            System.err.println("Failed to delete objects from S3: " + e.getMessage());
        }
    }

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
                                                AudioType audioType, String voiceId) {

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
                String finalVoiceId = (audioType == AudioType.VC_TRG) ? voiceId : null;
                MemberAudioMeta memberAudioMeta = MemberAudioMeta.createMemberAudioMeta(member, filename, fileUrl,
                        audioType, finalVoiceId);
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