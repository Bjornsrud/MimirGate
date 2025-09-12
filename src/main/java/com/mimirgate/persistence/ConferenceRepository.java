package com.mimirgate.persistence;

import com.mimirgate.model.Conference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConferenceRepository extends JpaRepository<Conference, Long> {
    Optional<Conference> findByNameIgnoreCase(String name);
}
