package com.fourformance.tts_vc_web.repository.workspace;

import com.fourformance.tts_vc_web.domain.entity.*;
import com.fourformance.tts_vc_web.dto.workspace.ExportListDto;
import com.fourformance.tts_vc_web.service.workspace.WorkspaceService;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import com.querydsl.core.BooleanBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

public class OutputAudioMetaRepositoryCustomImpl implements OutputAudioMetaRepositoryCustom {

    private JPAQueryFactory queryFactory;

    private WorkspaceService workspaceService;

    public OutputAudioMetaRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    QProject project = QProject.project;
    QTTSProject ttsProject = QTTSProject.tTSProject;
    QVCProject vcProject = QVCProject.vCProject;
    QConcatProject concatProject = QConcatProject.concatProject;
    QTTSDetail ttsDetail = QTTSDetail.tTSDetail;
    QVCDetail vcDetail = QVCDetail.vCDetail;
    QConcatDetail concatDetail = QConcatDetail.concatDetail;
    QOutputAudioMeta outputAudioMeta = QOutputAudioMeta.outputAudioMeta;
    QAPIStatus apiStatus = QAPIStatus.aPIStatus;


    public List<OutputAudioMeta> findExportHistoryBySearchCriteria(Long memberId, String keyword){
        BooleanBuilder whereClause = new BooleanBuilder();
        whereClause.and(outputAudioMeta.ttsDetail.ttsProject.member.id.eq(memberId))
                .or(outputAudioMeta.vcDetail.vcProject.member.id.eq(memberId))
                .or(outputAudioMeta.concatProject.member.id.eq(memberId));
        whereClause.and(outputAudioMeta.isDeleted.isFalse()); // 삭제되지않은 히스토리 기준



        if (keyword != null && !keyword.trim().isEmpty()) {
            BooleanBuilder keywordConditions = new BooleanBuilder();
            //각 프로젝트명
            keywordConditions.or(outputAudioMeta.ttsDetail.ttsProject.projectName.containsIgnoreCase(keyword));
            keywordConditions.or(outputAudioMeta.vcDetail.vcProject.projectName.containsIgnoreCase(keyword));
            keywordConditions.or(outputAudioMeta.concatProject.projectName.containsIgnoreCase(keyword));

            // 각 프로젝트의 파일명
            keywordConditions.or(
                    Expressions.stringTemplate(
                            "substring({0}, locate('/', reverse({0})) + 1)",
                            outputAudioMeta.bucketRoute
                    ).containsIgnoreCase(keyword)
            );
            // 각 프로젝트의 상태(응답기준 최신상태)
            keywordConditions.or(
                    Expressions.booleanTemplate(
                            "lower({0}) like {1}",
                            JPAExpressions.select(apiStatus.apiUnitStatusConst.stringValue())
                                    .from(apiStatus)
                                    .where(apiStatus.ttsDetail.eq(ttsDetail))
                                    .orderBy(apiStatus.responseAt.desc())
                                    .limit(1),
                            Expressions.constant("%" + keyword.toLowerCase() + "%")
                    )
            );

            // 각 프로젝트의 스크립트
            keywordConditions.or(outputAudioMeta.ttsDetail.unitScript.containsIgnoreCase(keyword));
            keywordConditions.or(outputAudioMeta.vcDetail.unitScript.containsIgnoreCase(keyword));
//            keywordConditions.or(outputAudioMeta.concatProject) // unitScript 뽑아내기.
            keywordConditions.or(
                    concatDetail.unitScript.containsIgnoreCase(keyword)
                            .and(concatDetail.concatProject.eq(outputAudioMeta.concatProject))
            ); // concatDetail의 ConcatProject와 outputaudioMeta의 CocnatProject를 ..

            // 프로젝트 타입
            keywordConditions.or(outputAudioMeta.ttsDetail.ttsProject.projectType.containsIgnoreCase(keyword));
            keywordConditions.or(outputAudioMeta.vcDetail.vcProject.projectType.containsIgnoreCase(keyword));
            keywordConditions.or(outputAudioMeta.concatProject.projectType.containsIgnoreCase(keyword));



        }
        List<ExportListDto> result = queryFactory.select(
                        Projections.constructor(ExportListDto.class,
                                outputAudioMeta.projectType,
                                concatProject.projectName.coalesce(ttsDetail.ttsProject.projectName, vcDetail.vcProject.projectName),
                                Expressions.stringTemplate(
                                        "substring({0}, locate('/', reverse({0})) + 1)",
                                        outputAudioMeta.bucketRoute
                                ), // 파일명 추출
                                concatDetail.unitScript.coalesce(ttsDetail.unitScript, vcDetail.unitScript),
                                JPAExpressions.select(apiStatus.apiUnitStatusConst)
                                        .from(apiStatus)
                                        .where(apiStatus.ttsDetail.eq(ttsDetail)
                                                .or(apiStatus.vcDetail.eq(vcDetail)))
                                        .orderBy(apiStatus.responseAt.desc())
                                        .limit(1), // 최신 상태
                                outputAudioMeta.createdAt,
                                outputAudioMeta.audioUrl
                        )
                )
                .from(outputAudioMeta)
                .leftJoin(outputAudioMeta.ttsDetail, ttsDetail)
                .leftJoin(outputAudioMeta.vcDetail, vcDetail)
                .leftJoin(outputAudioMeta.concatProject, concatProject)
                .leftJoin(concatProject.concatDetail, concatDetail) // ConcatDetails를 조인
                .where(keywordConditions)
                .fetch();
        return null;
    }

}
