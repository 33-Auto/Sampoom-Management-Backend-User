package com.sampoom.user.api.invitation.repository;

import com.sampoom.user.api.invitation.entity.Invitation;
import com.sampoom.user.api.invitation.entity.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, Long> {

    Optional<Invitation> findByInviteCode(String inviteCode);

    Optional<Invitation> findTopByEmailHashAndStatusOrderByIdAsc(
            String emailHash,
            InvitationStatus status
    );

    boolean existsByInviteCode(String inviteCode);
}