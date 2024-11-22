package com.fourformance.tts_vc_web.service.concat;

import com.fourformance.tts_vc_web.common.constant.AudioType;
import com.fourformance.tts_vc_web.common.exception.common.BusinessException;
import com.fourformance.tts_vc_web.common.exception.common.ErrorCode;
import com.fourformance.tts_vc_web.domain.entity.*;
import com.fourformance.tts_vc_web.dto.concat.ConcatDetailDto;
import com.fourformance.tts_vc_web.dto.concat.ConcatSaveDto;
import com.fourformance.tts_vc_web.dto.vc.AudioFileDto;
import com.fourformance.tts_vc_web.repository.ConcatDetailRepository;
import com.fourformance.tts_vc_web.repository.ConcatProjectRepository;
import com.fourformance.tts_vc_web.repository.MemberAudioMetaRepository;
import com.fourformance.tts_vc_web.repository.MemberRepository;
import com.fourformance.tts_vc_web.service.common.S3Service;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@RequiredArgsConstructor
public class ConcatService_team_aws {

    private final ConcatProjectRepository concatProjectRepository;
    private final ConcatDetailRepository concatDetailRepository;
    private final MemberAudioMetaRepository memberAudioMetaRepository;
    private final MemberRepository memberRepository;
    private final S3Service s3Service;

    // concat 프로젝트 저장하는 메서드
    public Long saveConcatProject(ConcatSaveDto dto, List<MultipartFile> localFiles, Member member) {
        // 1. ConcatProject 생성/업데이트
        ConcatProject concatProject = dto.getProjectId() == null
                ? createNewConcatProject(dto, member)
                : updateExistingConcatProject(dto);

        // 2. Concat Detail(==Concat src) 생성&저장
        processFiles(dto.getConcatDetails(), localFiles, concatProject);

        return concatProject.getId();
    }

    // Concat 프로젝트 생성
    private ConcatProject createNewConcatProject(ConcatSaveDto dto, Member member) {

        ConcatProject concatProject = ConcatProject.createConcatProject(member, dto.getProjectName());
        concatProjectRepository.save(concatProject);
        return concatProject;
    }

    // Concat 프로젝트 업데이트
    private ConcatProject updateExistingConcatProject(ConcatSaveDto dto){
        ConcatProject concatProject = concatProjectRepository.findById(dto.getProjectId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTS_PROJECT));

        concatProject.updateConcatProject(dto.getProjectName(), dto.getGlobalFrontSilenceLength(), dto.getGlobalTotalSilenceLength());

        return concatProject;
    }

    // detail 저장하는 메서드
    /**
     * MultipartFile이 들어오면 로컬 파일이므로 s3에 저장하고 db에 저장해야함
     *
     * @param fileDtos
     * @param files
     * @param concatProject
     */
    private void processFiles(List<ConcatDetailDto> fileDtos, List<MultipartFile> files, ConcatProject concatProject) {

        if (fileDtos == null || fileDtos.isEmpty()) { // 업로드 된 파일이 없을 때
            return;
        }

        for (ConcatDetailDto fileDto : fileDtos) {
            MemberAudioMeta audioMeta = null;

            if (fileDto.getLocalFileName() != null) {
            // 로컬 파일 처리
            MultipartFile localFile = findMultipartFileByName(files, fileDto.getLocalFileName());
            String uploadedUrl = s3Service.uploadAndSaveMemberFile(
                    localFile, concatProject.getMember().getId(), concatProject.getId(), AudioType.CONCAT); // voiceId를 받아오는 api 호출해서 null을 반환값으로 채우면 될 듯

            audioMeta = memberAudioMetaRepository.findFirstByAudioUrl(uploadedUrl)
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTS_AUDIO));
            }

            if (audioMeta == null) {
                throw new BusinessException(ErrorCode.INVALID_PROJECT_DATA);
            }


            //파일은 concatDetail에 저장
            ConcatDetail concatDetail = ConcatDetail.createConcatDetail(concatProject, fileDto.getAudioSeq(), fileDto.getIsChecked(),fileDto.getUnitScript(), fileDto.getEndSilence(), audioMeta);

            concatDetail.updateDetails(fileDto.getAudioSeq(), fileDto.getIsChecked(),fileDto.getUnitScript(),fileDto.getEndSilence());

            concatDetailRepository.save(concatDetail);

        }
    }
    private MultipartFile findMultipartFileByName(List<MultipartFile> files, String localFileName) {
        return files.stream()
                .filter(file -> file.getOriginalFilename().equals(localFileName))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.FILE_PROCESSING_ERROR));
    }


    // DTO 유효성 검증
    private void validateSaveDto(ConcatSaveDto dto) {
        if (dto.getProjectId() == null) {
            if (dto.getConcatDetails() != null) {
                for (ConcatDetailDto detail : dto.getConcatDetails()) {
                    if (detail.getId() != null) {
                        throw new BusinessException(ErrorCode.PROJECT_DETAIL_NOT_MATCH);
                    }
                }
            }
        }

        // 시퀀스 검증
        validateAudioSequence(dto.getConcatDetails());
    }

    // 오디오 시퀀스 유효성 검증
    private void validateAudioSequence(List<ConcatDetailDto> detailDtos) {
        Set<Integer> audioSeqSet = new HashSet<>();

        for (ConcatDetailDto detailDto : detailDtos) {
            if (!audioSeqSet.add(detailDto.getAudioSeq())) {
                throw new BusinessException(ErrorCode.DUPLICATE_UNIT_SEQUENCE);
            }
        }

        List<Integer> sequences = detailDtos.stream()
                .map(ConcatDetailDto::getAudioSeq)
                .sorted()
                .collect(Collectors.toList());

        for (int i = 0; i < sequences.size(); i++) {
            if (sequences.get(i) != i + 1) {
                throw new BusinessException(ErrorCode.INVALID_UNIT_SEQUENCE_ORDER);
            }
        }
    }

}
