package com.mimirgate.persistence;


import com.mimirgate.model.WallMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface WallMessageRepository extends JpaRepository<WallMessage, Long> {

    List<WallMessage> findByUsernameIgnoreCase(String username);

    @Query("SELECT m FROM WallMessage m ORDER BY m.timestamp DESC LIMIT ?1")
    List<WallMessage> findLastMessages(int limit);
}
