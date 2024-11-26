package com.fourformance.tts_vc_web.controller.member;

import com.fourformance.tts_vc_web.common.exception.common.BusinessException;
import com.fourformance.tts_vc_web.common.exception.common.ErrorCode;
import com.fourformance.tts_vc_web.dto.member.*;
import com.fourformance.tts_vc_web.dto.response.DataResponseDto;
import com.fourformance.tts_vc_web.dto.response.ResponseDto;
import com.fourformance.tts_vc_web.service.member.MemberService_team_api1;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController_team_api1 {

    private final MemberService_team_api1 memberService;

    /**
     * 회원 로그인
     * 세션을 생성하여 로그인 상태를 유지합니다.
     */
    @PostMapping("/login")
    public ResponseDto login(
            @RequestBody MemberLoginRequestDto requestDto,
            HttpSession session) {

        // 로그인 처리
        MemberLoginResponseDto memberLoginResponseDto = memberService.login(requestDto);

        // 세션에 로그인 정보 저장
        session.setAttribute("memberId", memberLoginResponseDto.getId());
        session.setAttribute("email", memberLoginResponseDto.getEmail());

        return DataResponseDto.of(memberLoginResponseDto);
    }

    /**
     * 회원 로그아웃
     * 세션을 무효화하여 로그아웃 처리합니다.
     */
    @PostMapping("/logout")
    public ResponseDto logout(HttpSession session) {
        // 세션 무효화
        session.invalidate();
        return DataResponseDto.of("로그아웃 성공");
    }


    /**
     * 회원 정보 조회
     * 세션에서 이메일을 얻고, 비밀번호를 확인하여 회원 정보를 반환합니다.
     */
    @PostMapping("/info")
    public ResponseDto getMemberInfo(
            @RequestBody MemberInfoRequestDto requestDto,
            HttpSession session) {

        // 세션에서 이메일 가져오기
        String email = (String) session.getAttribute("email");

        if (email == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED); // 세션 없음
        }

        // 비밀번호 확인 후 회원 정보 조회
        MemberUpdateResponseDto memberUpdateResponseDto = memberService.getMemberInfo(email, requestDto.getPwd());
        return DataResponseDto.of(memberUpdateResponseDto);
    }

    /**
     * 회원 정보 수정
     * 이름과 전화번호를 수정할 수 있습니다.
     */
    @PutMapping("/info/update")
    public ResponseEntity<MemberUpdateResponseDto> updateMemberInfo(
            @RequestBody MemberUpdateRequestDto requestDto, HttpSession session) {
        String email = (String) session.getAttribute("email");

        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null); // 세션 없음
        }

        // 회원 정보 수정
        MemberUpdateResponseDto responseDto = memberService.updateMemberInfo(email, requestDto);
        return ResponseEntity.ok(responseDto);
    }


    /**
     * 비밀번호 수정
     * 현재 비밀번호와 새 비밀번호, 새 비밀번호 확인을 처리합니다.
     */
    @PutMapping("/password/update")
    public ResponseEntity<String> updatePassword(
            @RequestBody PasswordUpdateRequestDto requestDto, HttpSession session) {
        String email = (String) session.getAttribute("email");

        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        // 비밀번호 수정
        memberService.updatePassword(email, requestDto);
        return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
    }
}

