package com.finalProjectLedZeppelin.auth.service;

import com.finalProjectLedZeppelin.auth.dto.AuthResponse;
import com.finalProjectLedZeppelin.auth.dto.LoginRequest;
import com.finalProjectLedZeppelin.auth.dto.RegisterRequest;
import com.finalProjectLedZeppelin.auth.jwt.JwtService;
import com.finalProjectLedZeppelin.auth.model.User;
import com.finalProjectLedZeppelin.auth.model.UserRole;
import com.finalProjectLedZeppelin.auth.repo.UserRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest req) {
        String email = req.email().toLowerCase().trim();
        log.info("Auth register attempt (email={})", email);
        if (userRepository.existsByEmail(email)) {
            log.warn("Auth register rejected: email already exists (email={})", email);
            throw new EmailAlreadyExistsException(email);
        }
        User u = new User();
        u.setEmail(email);
        u.setPasswordHash(passwordEncoder.encode(req.password()));
        u.setRole(UserRole.USER);
        u = userRepository.save(u);
        log.info("Auth register success (userId={}, email={}, role={})", u.getId(), u.getEmail(), u.getRole());
        String token = jwtService.generateAccessToken(u.getId(), u.getEmail(), u.getRole().name());
        return new AuthResponse(token);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        String email = req.email().toLowerCase().trim();
        log.info("Auth login attempt (email={})", email);
        User u = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Auth login failed: user not found (email={})", email);
                    return new BadCredentialsException();
                });

        if (!passwordEncoder.matches(req.password(), u.getPasswordHash())) {
            log.warn("Auth login failed: bad credentials (userId={}, email={})", u.getId(), email);
            throw new BadCredentialsException();
        }
        log.info("Auth login success (userId={}, email={}, role={})", u.getId(), u.getEmail(), u.getRole());
        String token = jwtService.generateAccessToken(u.getId(), u.getEmail(), u.getRole().name());
        return new AuthResponse(token);
    }

    public static class EmailAlreadyExistsException extends RuntimeException {
        public EmailAlreadyExistsException(String email) {
            super("Email already exists: " + email);
        }
    }

    public static class BadCredentialsException extends RuntimeException {
        public BadCredentialsException() {
            super("Invalid email or password");
        }
    }
}
