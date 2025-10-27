package com.sampoom.user.api.invitation.service;

import com.sampoom.user.api.invitation.dto.InvitationCreateRequestDto;
import com.sampoom.user.api.invitation.dto.InvitationCreateResponseDto;
import com.sampoom.user.api.invitation.entity.Invitation;
import com.sampoom.user.api.invitation.entity.InvitationStatus;
import com.sampoom.user.api.invitation.repository.InvitationRepository;
import com.sampoom.user.api.invitation.util.InvitationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InvitationService {

    private final InvitationRepository invitationRepository;
//    private final DomainEventPublisher eventPublisher;

    @Value("${invitation.salt}")
    private String salt;

    @Transactional
    public InvitationCreateResponseDto create(InvitationCreateRequestDto req) {
        // 이메일 해시
        String emailHash = InvitationUtils.emailHash(req.getEmail(), salt);

        // 초대코드 중복 방지 생성
        String code;
        int guard = 0;
        do {
            code = InvitationUtils.generateCode(8);
            guard++;
            if (guard > 5) throw new IllegalStateException("Invite code generation failed");
        } while (invitationRepository.existsByInviteCode(code));

        Invitation inv = Invitation.builder()
                .inviteCode(code)
                .targetType(req.getTargetType())
                .targetId(req.getTargetId())
                .emailHash(emailHash)
                .role(req.getRole())
                .position(req.getPosition())
                .status(InvitationStatus.PENDING)
                .build();

        invitationRepository.save(inv);


        return new InvitationCreateResponseDto(code);
    }
}

//    /**
//     * 회원가입 직후 호출하는 "자동 매칭"용 (inviteCode 없을 때)
//     */
//    @Transactional
//    public void claimByEmailIfPending(Long userId, String emailPlain) {
//        String emailHash = InvitationUtils.emailHash(emailPlain, salt);
//        invitationRepository.findFirstPendingByEmailHash(emailHash)
//                .ifPresent(inv -> acceptAndPublish(inv, userId));
//    }
//
//    /**
//     * 초대코드로 명시적 클레임
//     */
//    @Transactional
//    public void claimByCode(Long userId, String inviteCode) {
//        Invitation inv = invitationRepository.findByInviteCode(inviteCode)
//                .orElseThrow(() -> new IllegalArgumentException("Invalid inviteCode"));
//        if (!inv.getStatus().equals(InvitationStatus.PENDING))
//            return; // 멱등 (이미 처리된 경우 무시)
//        acceptAndPublish(inv, userId);
//    }
//
//    private void acceptAndPublish(Invitation inv, Long userId) {
//        inv.accept(); // 상태: ACCEPTED
//        // 멱등하게 이벤트 발행(Outbox 기반 권장)
//        eventPublisher.publish(new InvitationClaimedEvent(
//                inv.getInviteCode(),
//                userId,
//                inv.getTargetType().name(),
//                inv.getTargetId(),
//                inv.getRole().name(),
//                inv.getPosition().name()
//        ));
//        invitationRepository.save(inv);
//    }
//}