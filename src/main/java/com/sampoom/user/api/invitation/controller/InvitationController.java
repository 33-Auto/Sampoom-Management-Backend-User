package com.sampoom.user.api.invitation.controller;

import com.sampoom.user.api.invitation.dto.InvitationCreateRequestDto;
import com.sampoom.user.api.invitation.dto.InvitationCreateResponseDto;
import com.sampoom.user.api.invitation.service.InvitationService;
import com.sampoom.user.common.response.ApiResponse;
import com.sampoom.user.common.response.SuccessStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/invitations")
public class InvitationController {

    private final InvitationService invitationService;

    // 1) 초대 생성(관리자/HQ 권한 필요 - 시큐리티는 생략)
    @PostMapping
    public ResponseEntity<ApiResponse<InvitationCreateResponseDto>> create(@RequestBody @Valid InvitationCreateRequestDto req) {
        return ApiResponse.success(SuccessStatus.CREATED,invitationService.create(req));
    }

//    // 2) 가입 후 사용자가 코드로 클레임(명시적으로)
//    @PostMapping("/claim")
//    public ResponseEntity<Void> claim(@RequestBody @Valid InvitationClaimRequest req,
//                                      @RequestHeader("X-USER-ID") Long userId // 예시: 인증 컨텍스트에서 꺼내는 값
//    ) {
//        invitationService.claimByCode(userId, req.inviteCode());
//        return ResponseEntity.noContent().build();
//    }
}