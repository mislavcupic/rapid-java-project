// File: RefreshTokenService.java - MODIFICIRANO
package hr.algebra.rapid.logisticsandfleetmanagementsystem.service;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.RefreshToken;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.UserInfo;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.exceptions.TokenExpiredException;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.RefreshTokenRepository;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value; // ✅ DODANO
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Value("${refresh.token.expiration.ms}") // ✅ DODANO: Inject 24h trajanje
    private long refreshTokenExpirationMs;

    /**
     * Kreira novi refresh token za korisnika (trajanje 24h)
     */
    public RefreshToken createRefreshToken(String username) {
        UserInfo userInfo = userRepository.findByUsername(username);

        // Rotacija tokena: Obriši stari token ako postoji
        refreshTokenRepository.findByUserInfo(userInfo).ifPresent(refreshTokenRepository::delete);

        // Kreiraj novi token
        RefreshToken refreshToken = RefreshToken.builder()
                .userInfo(userInfo)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshTokenExpirationMs))
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Pronalazi refresh token po token stringu
     */
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    /**
     * Verificira da li je token istekao
     */
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new TokenExpiredException(token.getToken() + " Refresh token is expired. Please make a new login!");
        }
        return token;
    }
}