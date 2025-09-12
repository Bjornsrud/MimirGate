package com.mimirgate.persistence;

import com.mimirgate.model.ConferenceMembership;
import com.mimirgate.model.User;
import com.mimirgate.model.Conference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConferenceMembershipRepository extends JpaRepository<ConferenceMembership, Long> {
    List<ConferenceMembership> findByUser(User user);
    List<ConferenceMembership> findByConference(Conference conference);
    Optional<ConferenceMembership> findByUserAndConference(User user, Conference conference);
}
