package com.mimirgate.persistence;

import com.mimirgate.model.Conference;
import com.mimirgate.model.ThreadEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ThreadRepository extends JpaRepository<ThreadEntity, Long> {
    List<ThreadEntity> findByConferenceOrderByCreatedAtDesc(Conference conference);
}
