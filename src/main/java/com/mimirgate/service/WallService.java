package com.mimirgate.service;

import com.mimirgate.model.WallMessage;
import com.mimirgate.persistence.WallMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WallService {

    private final WallMessageRepository wallMessageRepository;

    private static final int MAX_CHARS = 160;

    @Autowired
    public WallService(WallMessageRepository wallMessageRepository) {
        this.wallMessageRepository = wallMessageRepository;
    }

    public int getMaxChars() {
        return MAX_CHARS;
    }

    public void addMessage(String username, String content) {
        WallMessage msg = new WallMessage(username, content);
        wallMessageRepository.save(msg);
    }

    public List<WallMessage> getMessagesForWidth(int width) {
        // For nå: hent alle i kronologisk rekkefølge
        return wallMessageRepository.findAll(Sort.by(Sort.Direction.ASC, "timestamp"));
    }

    public List<WallMessage> getLastMessages(int n) {
        return wallMessageRepository.findAll(
                PageRequest.of(0, n, Sort.by(Sort.Direction.DESC, "timestamp"))
        ).getContent();
    }

    public List<WallMessage> getMessagesByUser(String username) {
        return wallMessageRepository.findByUsernameIgnoreCase(username);
    }

    public List<WallMessage> findAll() {
        return wallMessageRepository.findAll();
    }

    public void deleteMessage(Long id) {
        wallMessageRepository.deleteById(id);
    }

}
