package com.mimirgate.persistence;

import com.mimirgate.model.PostEntity;
import com.mimirgate.model.ThreadEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<PostEntity, Long> {
    List<PostEntity> findByThreadOrderByCreatedAtAsc(ThreadEntity thread);
}
