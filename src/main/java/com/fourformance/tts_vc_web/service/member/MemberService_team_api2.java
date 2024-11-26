package com.fourformance.tts_vc_web.service.member;



import com.fourformance.tts_vc_web.domain.entity.Member;

import com.fourformance.tts_vc_web.dto.member.*;
import com.fourformance.tts_vc_web.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MemberService_team_api2 {

    private final MemberRepository memberRepository;

    /**
     * java.util.Date -> java.time.LocalDateTime 변환 유틸리티 메서드
     */
    private LocalDateTime convertToLocalDateTime(Date date) {
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    /**
     * 회원가입
     */
    public MemberSignUpResponseDto signUp(MemberSignUpRequestDto requestDto) {
        // 이메일 중복 체크
        if (memberRepository.existsByEmail(requestDto.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // java.util.Date → java.time.LocalDateTime 변환
        LocalDateTime birthDateTime = convertToLocalDateTime(requestDto.getBirthDate());

        // 엔티티 생성
        Member member = Member.createMember(
                requestDto.getEmail(),
                requestDto.getPwd(),
                requestDto.getName(),
                requestDto.getGender(),
                birthDateTime,
                requestDto.getPhoneNumber()
        );

        // 데이터베이스에 저장
        Member savedMember = memberRepository.save(member);

        // 응답 DTO 생성
        return MemberSignUpResponseDto.builder()
                .id(savedMember.getId())
                .email(savedMember.getEmail())
                .name(savedMember.getName())
                .createdAt(savedMember.getCreatedAt())
                .build();
    }

    /**
     * 이메일 중복 체크
     */
    public MemberIdCheckResponseDto checkEmailDuplicate(MemberIdCheckRequestDto requestDto) {
        boolean isDuplicate = memberRepository.existsByEmail(requestDto.getEmail());
        return MemberIdCheckResponseDto.builder()
                .isDuplicate(isDuplicate)
                .build();
    }

    /**
     * 회원 ID 찾기
     */
    public MemberIdFindResponseDto findId(MemberIdFindRequestDto requestDto) {
        // 이름과 전화번호로 회원 검색
        Member member = memberRepository.findByNameAndPhoneNumber(requestDto.getName(), requestDto.getPhoneNumber())
                .orElseThrow(() -> new IllegalArgumentException("해당 정보를 가진 사용자를 찾을 수 없습니다."));

        // 응답 DTO 생성
        return MemberIdFindResponseDto.builder()
                .email(member.getEmail())
                .build();
    }

    /**
     * 비밀번호 찾기
     */
    public MemberPasswordFindResponseDto findPassword(MemberPasswordFindRequestDto requestDto) {
        // 이메일로 회원 검색
        Member member = memberRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일을 가진 사용자가 존재하지 않습니다."));

        // 전화번호 검증
        if (!member.getPhoneNumber().equals(requestDto.getPhoneNumber())) {
            throw new IllegalArgumentException("전화번호가 일치하지 않습니다.");
        }

        // 임시 비밀번호 생성
        String temporaryPassword = UUID.randomUUID().toString().substring(0, 8);

        // 비밀번호 업데이트
        member.updateMember(temporaryPassword, member.getPhoneNumber(), member.getBirthDate());
        memberRepository.save(member);

        // 응답 DTO 생성
        return MemberPasswordFindResponseDto.builder()
                .email(member.getEmail())
                .temporaryPassword(temporaryPassword)
                .build();
    }
}
