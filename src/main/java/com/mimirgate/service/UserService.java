package com.mimirgate.service;

import com.mimirgate.model.User;
import com.mimirgate.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

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

    public User createUser(String username, String rawPassword, String email, String bio, int terminalWidth) {
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setEmail(email);
        user.setBio(bio);
        user.setTerminalWidth(terminalWidth);

        // Første bruker blir SYSOP
        if (userRepository.count() == 0) {
            user.setRole(User.Role.SYSOP);
            System.out.println("First user detected: assigning SYSOP role.");
        } else {
            user.setRole(User.Role.USER);
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

    public void updatePassword(User user, String rawPassword) {
        String hash = passwordEncoder.encode(rawPassword);
        user.setPasswordHash(hash);
        userRepository.save(user);
    }

    public boolean verifyPassword(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPasswordHash());
    }

    public void updateLastLogin(User user) {
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
    }
}
