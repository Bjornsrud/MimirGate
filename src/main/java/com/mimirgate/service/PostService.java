package com.mimirgate.service;

import com.mimirgate.model.PostEntity;
import com.mimirgate.model.ThreadEntity;
import com.mimirgate.model.User;
import com.mimirgate.persistence.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostService {

    private final PostRepository postRepository;

    @Autowired
    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public PostEntity createPost(ThreadEntity thread, User author, String content) {
        PostEntity post = new PostEntity(thread, author, content);
        return postRepository.save(post);
    }

    public List<PostEntity> listPosts(ThreadEntity thread) {
        return postRepository.findByThreadOrderByCreatedAtAsc(thread);
    }
}
