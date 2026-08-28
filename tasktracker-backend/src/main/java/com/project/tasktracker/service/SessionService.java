package com.project.tasktracker.service;

import com.project.tasktracker.dto.SessionDto;
import com.project.tasktracker.error.ApiException;
import com.project.tasktracker.error.ErrorCode;
import com.project.tasktracker.model.Session;
import com.project.tasktracker.model.User;
import com.project.tasktracker.repository.SessionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.UUID;

@Service
@Transactional
public class SessionService {

    private static final int TOKEN_BYTE_LENGTH = 32; // 256 bits
    private static final long SESSION_EXPIRATION_DAYS = 30;

    private final SessionRepository sessionRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public SessionService(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    public SessionDto createSession() {
        String rawToken = generateRawToken();
        String tokenHash = hashToken(rawToken);

        UUID userId = UUID.randomUUID();
        User user = new User(userId, "anonymous-" + userId + "@tasktracker.local");

        Instant now = Instant.now();
        Instant expiresAt = now.plus(SESSION_EXPIRATION_DAYS, ChronoUnit.DAYS);

        Session session = new Session(tokenHash, user, now, now, expiresAt);
        sessionRepository.save(session);

        return new SessionDto(rawToken, expiresAt);
    }

    public User authenticate(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, ErrorCode.SESSION_MISSING, "Missing session token");
        }

        String tokenHash = hashToken(rawToken);
        Session session = sessionRepository.findByTokenHashWithUser(tokenHash)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, ErrorCode.SESSION_INVALID, "Invalid session token"));

        if (session.isExpired()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, ErrorCode.SESSION_EXPIRED, "Session has expired");
        }

        Instant now = Instant.now();
        session.setLastSeenAt(now);
        session.setExpiresAt(now.plus(SESSION_EXPIRATION_DAYS, ChronoUnit.DAYS));
        sessionRepository.save(session);

        return session.getUser();
    }

    public String generateRawToken() {
        byte[] randomBytes = new byte[TOKEN_BYTE_LENGTH];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    public String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
