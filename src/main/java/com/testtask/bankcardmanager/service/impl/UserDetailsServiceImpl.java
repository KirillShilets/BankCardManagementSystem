package com.testtask.bankcardmanager.service.impl;

import com.testtask.bankcardmanager.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);
    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.debug("Attempt to upload a user by username (email): {}", username);
        return userRepository.findByEmail(username)
                .orElseThrow(() -> {
                    logger.warn("Пользователь не найден с именем пользователя (email): {}", username);
                    return new UsernameNotFoundException("User not found with username: " + username);
                });
    }
}
