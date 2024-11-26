package com.fourformance.tts_vc_web.repository.workspace;

import com.fourformance.tts_vc_web.domain.entity.ConcatProject;
import com.fourformance.tts_vc_web.domain.entity.QConcatDetail;
import com.fourformance.tts_vc_web.domain.entity.QConcatProject;
import com.fourformance.tts_vc_web.domain.entity.QProject;
import com.fourformance.tts_vc_web.domain.entity.QTTSDetail;
import com.fourformance.tts_vc_web.domain.entity.QTTSProject;
import com.fourformance.tts_vc_web.domain.entity.QVCDetail;
import com.fourformance.tts_vc_web.domain.entity.QVCProject;
import com.fourformance.tts_vc_web.domain.entity.TTSProject;
import com.fourformance.tts_vc_web.domain.entity.VCProject;
import com.fourformance.tts_vc_web.dto.workspace.ProjectListDto;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.util.List;

public class ProjectRepositoryCustomImpl implements ProjectRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public ProjectRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<ProjectListDto> findProjectsBySearchCriteria(Long memberId, String keyword) {
        QProject project = QProject.project;
        QTTSProject ttsProject = QTTSProject.tTSProject;
        QVCProject vcProject = QVCProject.vCProject;
        QConcatProject concatProject = QConcatProject.concatProject;

        // 공통 필터 조건
        BooleanBuilder whereClause = new BooleanBuilder();
        whereClause.and(project.member.id.eq(memberId)); // 멤버 ID 조건
        whereClause.and(project.isDeleted.isFalse());    // 삭제되지 않은 프로젝트

        // 키워드 검색
        if (keyword != null && !keyword.isEmpty()) {
            whereClause.and(project.projectName.containsIgnoreCase(keyword));
        }

        // QueryDSL로 데이터 조회
        List<ProjectListDto> result = queryFactory
                .selectFrom(project)
                .where(whereClause)
                .orderBy(project.updatedAt.desc())
                .fetch()
                .stream()
                .map(p -> {
                    ProjectListDto dto = new ProjectListDto();
                    dto.setProjectId(p.getId());
                    dto.setProjectName(p.getProjectName());
                    dto.setUpdatedAt(p.getUpdatedAt());
                    dto.setCreatedAt(p.getCreatedAt());

                    // 구체적인 타입별 분기 처리
                    if (p instanceof TTSProject) {
                        TTSProject tts = (TTSProject) p;

                        // TTS의 첫 번째 디테일 가져오기
                        String firstScript = queryFactory
                                .select(QTTSDetail.tTSDetail.unitScript)
                                .from(QTTSDetail.tTSDetail)
                                .where(QTTSDetail.tTSDetail.ttsProject.id.eq(tts.getId())
                                        .and(QTTSDetail.tTSDetail.isDeleted.isFalse()))
                                .orderBy(QTTSDetail.tTSDetail.unitSequence.asc())
                                .fetchFirst();

                        dto.setScript(firstScript);
                        dto.setProjectStatus(tts.getApiStatus().toString());
                        dto.setProjectType("TTS");
                    } else if (p instanceof VCProject) {
                        VCProject vc = (VCProject) p;

                        // VC의 첫 번째 디테일 가져오기
                        String firstScript = queryFactory
                                .select(QVCDetail.vCDetail.unitScript)
                                .from(QVCDetail.vCDetail)
                                .where(QVCDetail.vCDetail.vcProject.id.eq(vc.getId())
                                        .and(QVCDetail.vCDetail.isDeleted.isFalse()))
//                                .orderBy(QVCDetail.vCDetail.unitSequence.asc())
                                .fetchFirst();

                        dto.setScript(firstScript);
                        dto.setProjectStatus(vc.getApiStatus().toString());
                        dto.setProjectType("VC");
                    } else if (p instanceof ConcatProject) {
                        ConcatProject concat = (ConcatProject) p;

                        // CONCAT의 첫 번째 디테일 가져오기
                        String firstScript = queryFactory
                                .select(QConcatDetail.concatDetail.unitScript)
                                .from(QConcatDetail.concatDetail)
                                .where(QConcatDetail.concatDetail.concatProject.id.eq(concat.getId())
                                        .and(QConcatDetail.concatDetail.isDeleted.isFalse()))
                                .orderBy(QConcatDetail.concatDetail.audioSeq.asc())
                                .fetchFirst();

                        dto.setScript(firstScript);
                        dto.setProjectStatus(null); // CONCAT 프로젝트의 상태는 null
                        dto.setProjectType("CONCAT");
                    }
                    return dto;
                })
                .toList();

        return result;
    }
}
