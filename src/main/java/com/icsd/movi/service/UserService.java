package com.icsd.movi.service;

import com.icsd.movi.domain.model.User;
import com.icsd.movi.domain.repository.UserRepository;
import com.icsd.movi.exception.ConflictException;
import com.icsd.movi.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User registerUser(String username, String password, String fullName) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new ConflictException("Username already exists");
        }
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .fullName(fullName)
                .build();
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    @Transactional(readOnly = true)
    public User findByRefreshToken(String refreshToken) {
        return userRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    @Transactional
    public User updateRefreshToken(String username, String refreshToken) {
        User user = findByUsername(username);
        user.setRefreshToken(refreshToken);
        return userRepository.save(user);
    }
}
