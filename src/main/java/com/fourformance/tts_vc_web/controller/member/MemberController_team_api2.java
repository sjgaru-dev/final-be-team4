package com.fourformance.tts_vc_web.controller.member;

import com.fourformance.tts_vc_web.dto.member.*;
import com.fourformance.tts_vc_web.service.member.MemberService_team_api2;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Tag(name = "Member API", description = "회원 관련 API")
public class MemberController_team_api2 {

    private final MemberService_team_api2 memberService;

    @Operation(summary = "회원가입", description = "회원가입 API로 이메일, 비밀번호, 이름, 성별, 생년월일, 전화번호를 통해 회원을 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원가입 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/signup")
    public ResponseEntity<MemberSignUpResponseDto> signUp(@RequestBody MemberSignUpRequestDto requestDto) {
        MemberSignUpResponseDto responseDto = memberService.signUp(requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "이메일 중복 체크", description = "입력한 이메일의 중복 여부를 확인합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "중복 여부 확인 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/check-id")
    public ResponseEntity<MemberIdCheckResponseDto> checkId(@RequestBody MemberIdCheckRequestDto requestDto) {
        MemberIdCheckResponseDto responseDto = memberService.checkEmailDuplicate(requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "회원 ID 찾기", description = "이름과 전화번호를 통해 회원 이메일을 찾습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원 ID 찾기 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "404", description = "회원 정보를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/find-id")
    public ResponseEntity<MemberIdFindResponseDto> findId(@RequestBody MemberIdFindRequestDto requestDto) {
        MemberIdFindResponseDto responseDto = memberService.findId(requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "비밀번호 찾기", description = "이메일과 전화번호를 통해 임시 비밀번호를 발급받습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "임시 비밀번호 발급 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "404", description = "회원 정보를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/find-password")
    public ResponseEntity<MemberPasswordFindResponseDto> findPassword(@RequestBody MemberPasswordFindRequestDto requestDto) {
        MemberPasswordFindResponseDto responseDto = memberService.findPassword(requestDto);
        return ResponseEntity.ok(responseDto);
    }
}
