package com.mimirgate.service;

import com.mimirgate.model.Conference;
import com.mimirgate.model.User;
import com.mimirgate.persistence.ConferenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;

@Service
public class ConferenceService {

    private final ConferenceRepository conferenceRepository;

    @Autowired
    public ConferenceService(ConferenceRepository conferenceRepository) {
        this.conferenceRepository = conferenceRepository;
    }

    @PostConstruct
    public void ensureMainConference() {
        conferenceRepository.findByNameIgnoreCase("Main")
                .orElseGet(() -> {
                    Conference main = new Conference("Main", "Default conference", false, false);
                    main.setUndeletable(true);
                    return conferenceRepository.save(main);
                });
    }

    public List<Conference> listAll() {
        return conferenceRepository.findAll();
    }

    public Optional<Conference> findByName(String name) {
        return conferenceRepository.findByNameIgnoreCase(name);
    }

    public Conference createConference(String name, String description) {
        Conference conf = new Conference(name, description, false, false);
        return conferenceRepository.save(conf);
    }

    public void deleteConference(Long id) {
        conferenceRepository.findById(id).ifPresent(conf -> {
            if (conf.isUndeletable()) {
                throw new IllegalStateException("The Main conference cannot be deleted.");
            }
            conferenceRepository.delete(conf);
        });
    }

    public void toggleRestricted(Conference conf) {
        conf.setRestricted(!conf.isRestricted());
        conferenceRepository.save(conf);
    }

    public void toggleVipOnly(Conference conf) {
        conf.setVipOnly(!conf.isVipOnly());
        conferenceRepository.save(conf);
    }

    /**
     * Sjekker om en bruker har tilgang til å se/join en konferanse.
     */
    public boolean hasAccess(User user, Conference conf) {
        switch (user.getRole()) {
            case SYSOP:
            case COSYSOP:
                return true; // full tilgang

            case VIP:
                if (conf.isRestricted()) return false;
                return true; // VIP ser alt unntatt restricted

            case USER:
            default:
                if (conf.isRestricted()) return false;
                if (conf.isVipOnly()) return false;
                return true; // vanlige brukere ser bare åpne konferanser
        }
    }
}
