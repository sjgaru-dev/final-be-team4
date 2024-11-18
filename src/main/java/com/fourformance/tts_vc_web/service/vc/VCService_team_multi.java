package com.fourformance.tts_vc_web.service.vc;

import com.fourformance.tts_vc_web.domain.entity.VCProject;
import com.fourformance.tts_vc_web.dto.vc.VCSaveDetailDto;
import com.fourformance.tts_vc_web.dto.vc.VCSaveDto;
import com.fourformance.tts_vc_web.repository.VCDetailRepository;
import com.fourformance.tts_vc_web.repository.VCProjectRepository;
import com.fourformance.tts_vc_web.service.common.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class VCService_team_multi {
    private final VCProjectRepository vcProjectRepository;
    private final VCDetailRepository vcDetailRepository;
    private final S3Service s3Service;

    // VC 프로젝트 생성 메서드
    public Long createNewVCProject(VCSaveDto dto){
        // s3Service.uploadAndSaveMemberFile(List<MultipartFile> files, Long memberId, Long projectId,
        //                                                AudioType audioType)를 호출해서 s3에 사용자 오디오(src, trg)를 저장해야함.

        /** VCProject
         * 1. vcProject 생성 : 이때 저장할 매개변수는 member, projectName
         * 2. 생성한 vcProject를 db에 저장
         *
         * VCDetail
         * 1. dto에 VCDetail에 정보가 있는지 확인. 없으면 끝, 있으면 detail관련 정보 저장 로직 호출
         * 2. detail에 대한 정보가 있으면 for문으로 detail 하나씩 저장하는 메서드 호출
         */
        VCProject vcProject = VCProject.createVCProject(null, dto.getProjectName());

        vcProjectRepository.save(vcProject);

        // vc detail이 존재한다면 저장
        if(dto.getVcDetails() != null){
            for (VCSaveDetailDto vcDetail : dto.getVcDetails()) {
                createVCDetail(vcDetail); // detail 저장 메서드 호출
            }
        }

        return vcProject.getId();
    }

    // VC 프로젝트 업데이트 메서드
    public Long updateVCProject(VCSaveDto dto){


        return 1l;
    }

    private void createVCDetail(VCSaveDetailDto dto){
        /**
         * 1. src 오디오가 null이면 로컬 업로드라는 뜻
         */
    }


}
