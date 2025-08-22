package com.kubrafelek.brokagefirm.service;

import com.kubrafelek.brokagefirm.entity.User;
import com.kubrafelek.brokagefirm.enums.Role;
import com.kubrafelek.brokagefirm.exception.UsernameAlreadyExistsException;
import com.kubrafelek.brokagefirm.repository.UserRepository;
import com.kubrafelek.brokagefirm.constants.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User authenticate(String username, String password) {
        logger.info("Attempting to authenticate user: {}", username);

        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            logger.info("User found in database: {}, role: {}", username, user.getRole());

            boolean passwordMatches = passwordEncoder.matches(password, user.getPassword());
            logger.info("Password matches for user {}: {}", username, passwordMatches);

            if (passwordMatches) {
                logger.info("User authenticated successfully: {}", username);
                return user;
            } else {
                logger.warn("Password mismatch for user: {}", username);
            }
        } else {
            logger.warn("User not found: {}", username);
        }
        return null;
    }

    public User createUser(String username, String password, Role role) {
        if (userRepository.existsByUsername(username)) {
            throw new UsernameAlreadyExistsException(Constants.ErrorMessages.USERNAME_ALREADY_EXISTS);
        }

        String encodedPassword = passwordEncoder.encode(password);
        User user = new User(username, encodedPassword, role);

        return userRepository.save(user);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
}
