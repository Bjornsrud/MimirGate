package com.mimirgate.service;

import com.mimirgate.model.Conference;
import com.mimirgate.model.ConferenceMembership;
import com.mimirgate.model.User;
import com.mimirgate.persistence.ConferenceMembershipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConferenceMembershipService {

    private final ConferenceMembershipRepository membershipRepository;

    @Autowired
    public ConferenceMembershipService(ConferenceMembershipRepository membershipRepository) {
        this.membershipRepository = membershipRepository;
    }

    public boolean isMember(User user, Conference conference) {
        return membershipRepository.findByUserAndConference(user, conference).isPresent();
    }

    public void joinConference(User user, Conference conference) {
        if (!isMember(user, conference)) {
            membershipRepository.save(new ConferenceMembership(user, conference));
        }
    }

    public void leaveConference(User user, Conference conference) {
        membershipRepository.findByUserAndConference(user, conference)
                .ifPresent(membershipRepository::delete);
    }

    public List<ConferenceMembership> listUserMemberships(User user) {
        return membershipRepository.findByUser(user);
    }

    public List<ConferenceMembership> listConferenceMembers(Conference conference) {
        return membershipRepository.findByConference(conference);
    }
}
