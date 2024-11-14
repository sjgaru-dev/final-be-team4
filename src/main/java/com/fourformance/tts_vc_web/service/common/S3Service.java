package com.fourformance.tts_vc_web.service.common;

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
    public String uploadUnitSaveFile(MultipartFile file, Long userId, Long projectId, Long detailId) throws Exception {

        try {
            // 오디오파일 이름으로 사용할 날짜 포맷 지정
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String timeStamp = sdf.format(new Date());

            // Project의 실제 타입에 따라 ProjectType 설정
            Project project = projectRepository.findById(projectId).orElse(null);
            ProjectType projectType = null;
            String fileName = null;
            if (project instanceof TTSProject) {
                projectType = ProjectType.TTS;
                fileName = "Generated/" + userId + "/" + projectType + "/" + projectId + "/" + detailId + ".wav";

            } else if (project instanceof VCProject) {
                projectType = ProjectType.VC;
                fileName =
                        "Generated/" + userId + "/" + projectType + "/" + projectId + "/" + detailId + "/" + timeStamp
                                + ".wav";
            } else {
                throw new BusinessException(ErrorCode.UNKNOWN_ERROR);
            }

            // 메타데이터 저장
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType()); // wav
            metadata.setContentLength(file.getSize()); // 업로드 할 때 사이즈 반드시 필요

            // S3에 파일 업로드 (전체 경로 포함)
            amazonS3Client.putObject(bucket, fileName, file.getInputStream(), metadata);

            // 파일의 전체 URL 반환
            String fileUrl = amazonS3Client.getUrl(bucket, fileName).toString();

            // 반환된 오디오 메타를 DB에 저장
            saveTTSOrVCOutputAudioMeta(fileName, detailId, projectType, fileUrl);

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
            saveConcatAudioMeta(fileName, projectId, fileUrl);

            return fileUrl;

        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception();
        }
    }


    // DB에 TTS와 VC에 대한 오디오 메타를 저장
    public OutputAudioMeta saveTTSOrVCOutputAudioMeta(String fileName, Long detailId, ProjectType projectType,
                                                      String audioUrl) {

        TTSDetail ttsDetail;
        VCDetail vcDetail;

        // TTS 프로젝트인지 VC 프로젝트인지 판별
        if (projectType.equals(ProjectType.TTS)) {
            ttsDetail = ttsDetailRepository.findById(detailId).orElse(null);
            vcDetail = null;
        } else if (projectType.equals(ProjectType.VC)) {
            vcDetail = vcDetailRepository.findById(detailId).orElse(null);
            ttsDetail = null;
        } else {
            throw new IllegalArgumentException("DetailId는 TTS 또는 VC 중 하나의 ID여야 합니다.");
        }

        // OutputAudioMeta 객체 생성
        OutputAudioMeta outputAudioMeta = OutputAudioMeta.createOutputAudioMeta(fileName, ttsDetail, vcDetail, null,
                projectType, audioUrl);

        // 생성한 OutputAudioMeta를 DB에 저장
        return outputAudioMetaRepository.save(outputAudioMeta);
    }

    // DB에 Concat 기능을 수행해서 반환한 오디오 메타를 저장
    public OutputAudioMeta saveConcatAudioMeta(String fileName, Long projectId, String audioUrl) {
        ConcatProject concatProject = concatProjectRepository.findById(projectId).orElse(null);

        // 존재하는 프로젝트인지 판별
        if (concatProject == null) {
            throw new IllegalArgumentException("ProjectId는 반드시 존재해야합니다.");
        }

        // OutputAudioMeta 객체 생성
        OutputAudioMeta outputAudioMeta = OutputAudioMeta.createOutputAudioMeta(fileName, null, null, concatProject,
                ProjectType.CONCAT, audioUrl);

        // 생성한 OutputAudioMeta를 DB에 저장
        return outputAudioMetaRepository.save(outputAudioMeta);
    }

//    // 다운로드 받을 오디오의 버킷 URL을 제공하는 메서드
//    public String generatePresignedUrl(Long userId, Long projectId, Long detailId, String fileName) throws Exception {
//        try {
//
//            // Project의 실제 타입에 따라 ProjectType 설정
//            Project project = projectRepository.findById(projectId).orElse(null);
//            ProjectType projectType = null;
//            String filePath = null;
//            if (project instanceof TTSProject) {
//                projectType = ProjectType.TTS;
//                filePath =
//                        "Generated/" + userId + "/" + projectType + "/" + projectId + "/" + detailId + "/" + fileName;
//            } else if (project instanceof VCProject) {
//                projectType = ProjectType.VC;
//                filePath =
//                        "Generated/" + userId + "/" + projectType + "/" + projectId + "/" + detailId + "/" + fileName;
//            } else if (project instanceof ConcatProject) {
//                projectType = ProjectType.CONCAT;
//                filePath = "Generated/" + userId + "/" + projectType + "/" + projectId + "/" + fileName;
//            } else {
//                throw new IllegalArgumentException("지정된 타입의 프로젝트가 들어와야 합니다.");
//            }
//
//            // presigned url 생성
//            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucket, filePath);
//            request.withMethod(com.amazonaws.HttpMethod.GET)
//                    .withExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 5));
//            URL presignedUrl = amazonS3Client.generatePresignedUrl(request);
//
//            return presignedUrl.toString();
//
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw new Exception();
//        }
//    }

    // 다운로드 받을 오디오의 버킷 URL을 제공하는 메서드
    public String generatePresignedUrl(String bucketRoute) throws Exception {
        try {
            // presigned url 생성
            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucket, bucketRoute);
            request.withMethod(com.amazonaws.HttpMethod.GET)
                    .withExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 5));
            URL presignedUrl = amazonS3Client.generatePresignedUrl(request);

            return presignedUrl.toString();
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception();
        }
    }

    // new things================================================== sojeong
    // 모든 파일 URL을 리스트로 반환하는 메서드
    // 수정할 사항 : 1 . 파일업로드시 파일 리스트가 비어있는지 확인 필요.
    // 2. 반환값에 대한 컨트롤러의 처리
    // saveMemberAudiometa에서의 null처리필요.

    public List<String> uploadAndSaveMemberFile(List<MultipartFile> files, Long memberId, Long projectId,
                                                AudioType audioType) throws Exception {

        try {
            List<String> uploadedUrls = new ArrayList<>(); // 빈 어레이리스트 생성
            Project project = projectRepository.findById(projectId).orElse(null);
            Member member = memberRepository.findById(memberId).orElse(null);

            for (MultipartFile file : files) {
                String fileUrl = uploadFileToS3(file, memberId, projectId, audioType); // 개별 파일 URL 반환
                uploadedUrls.add(fileUrl); // URL 리스트에 추가
            }

            String fileName = "null";

            if (project instanceof VCProject) {
                if (audioType == AudioType.VC_SRC) { // 얘는 리스트형식으로 저장
                    saveMemberAudioMeta(memberId, fileName, null, uploadedUrls, null, AudioType.VC_SRC, projectId);
                } else if (audioType == AudioType.VC_TRG) { // 얘는 파일 하나 꺼내서 경로를 하나의 변수에 저장
                    saveMemberAudioMeta(memberId, fileName, uploadedUrls.get(0), null, null, AudioType.VC_TRG,
                            projectId);
                }
            } else if (project instanceof ConcatProject) { // 얘도 리스트형식으로 저장처리
                saveMemberAudioMeta(memberId, fileName, null, null, uploadedUrls, AudioType.CONCAT, projectId);
            }

            return uploadedUrls; // 모든 파일의 URL 리스트 반환.

        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("업로드 실패", e);
        }
    }

    // 공통적으로 들어가는 코드를 뺴냄
    private String uploadFileToS3(MultipartFile file, Long memberId, Long projectId, AudioType audioType)
            throws Exception {
        String originFilename = Normalizer.normalize(file.getOriginalFilename(), Normalizer.Form.NFC);
        String filename = "member/" + memberId + "/" + audioType + "/" + projectId + "/" + originFilename;

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());

        amazonS3Client.putObject(bucket, filename, file.getInputStream(), metadata);
        return amazonS3Client.getUrl(bucket, filename).toString();
    }

    // DB에 유저 오디오 메타 저장
    public void saveMemberAudioMeta(Long memberId, String fileName, String targetAudioUrl, List<String> srcAudioUrls,
                                    List<String> concatAudioUrls, AudioType audioType, Long projectId) {

        Project project = projectRepository.findById(projectId).orElse(null);
        ProjectType projectType = null;
        Member member = memberRepository.findById(memberId).orElse(null);

        if (project instanceof VCProject) {
            projectType = ProjectType.VC;

            MemberAudioMeta targetAudioMeta = MemberAudioMeta.createMemberAudioMeta(member, fileName, targetAudioUrl,
                    AudioType.VC_TRG);
            memberAudioMetaRepository.save(targetAudioMeta);

            // 텍스트는 어떻게하지?
            for (String srcAudioUrl : srcAudioUrls) {
                MemberAudioMeta sourceAudioMeta = MemberAudioMeta.createMemberAudioMeta(member, fileName, srcAudioUrl,
                        AudioType.VC_SRC);
                memberAudioMetaRepository.save(sourceAudioMeta);
            }
        } else if (project instanceof ConcatProject) {
            projectType = ProjectType.CONCAT;
            for (String concatAudioUrl : concatAudioUrls) {
                MemberAudioMeta memberAudioMeta = MemberAudioMeta.createMemberAudioMeta(member, fileName,
                        concatAudioUrl, AudioType.CONCAT);
                memberAudioMetaRepository.save(memberAudioMeta);
            }
        } else {
            throw new UnsupportedOperationException("지원되지 않는 프로젝트 유형입니다.");
        }
    }

    //myEntityRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Entity not found with id " + id));
}
