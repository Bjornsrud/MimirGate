package com.mimirgate.service;

import com.mimirgate.model.User;
import com.mimirgate.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    public User createUser(String username, String passwordHash, String email, String bio, int terminalWidth) {
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordHash);
        user.setEmail(email);
        user.setBio(bio);
        user.setTerminalWidth(terminalWidth);

        // Hvis ingen brukere finnes, blir første bruker SYSOP
        if (userRepository.count() == 0) {
            user.setRole(User.Role.SYSOP);
            System.out.println("First user detected: assigning SYSOP role.");
        } else {
            user.setRole(User.Role.USER); // standard rolle
        }

        return userRepository.save(user);
    }


    public void updateEmail(User user, String email) {
        user.setEmail(email);
        userRepository.save(user);
    }

    public void updateBio(User user, String bio) {
        user.setBio(bio);
        userRepository.save(user);
    }

    public void updateTerminalWidth(User user, int width) {
        user.setTerminalWidth(width);
        userRepository.save(user);
    }

    public void updateRole(User user, User.Role role) {
        user.setRole(role);
        userRepository.save(user);
    }
}
