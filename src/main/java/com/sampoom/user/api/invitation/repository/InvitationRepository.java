package com.sampoom.user.api.invitation.repository;

import com.sampoom.user.api.invitation.entity.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, Long> {

    Optional<Invitation> findByInviteCode(String inviteCode);

    @Query("""
        select i from Invitation i
        where i.emailHash = :emailHash
          and i.status = com.sampoom.user.api.invitation.entity.InvitationStatus.PENDING
        order by i.id asc
    """)
    Optional<Invitation> findFirstPendingByEmailHash(String emailHash);

    boolean existsByInviteCode(String inviteCode);
}