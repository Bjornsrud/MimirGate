package com.mimirgate.service;

import com.mimirgate.model.Conference;
import com.mimirgate.model.ThreadEntity;
import com.mimirgate.model.User;
import com.mimirgate.persistence.ThreadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ThreadService {

    private final ThreadRepository threadRepository;

    @Autowired
    public ThreadService(ThreadRepository threadRepository) {
        this.threadRepository = threadRepository;
    }

    public List<ThreadEntity> listThreads(Conference conference) {
        return threadRepository.findByConferenceOrderByCreatedAtDesc(conference);
    }

    public ThreadEntity createThread(Conference conference, User creator, String title) {
        ThreadEntity thread = new ThreadEntity(conference, creator, title);
        return threadRepository.save(thread);
    }
}
