package hr.algebra.rapid.logisticsandfleetmanagementsystem.service;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.RefreshToken;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.UserInfo;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.exceptions.TokenExpiredException;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.RefreshTokenRepository;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor // ✅ Lombok generiše konstruktor
public class RefreshTokenService {

    // ✅ Final fields - Constructor Injection
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    /**
     * Kreira novi refresh token za korisnika
     * Ako postoji stari token, briše ga prije kreiranja novog
     */
    public RefreshToken createRefreshToken(String username) {
        // 1. Dohvati UserInfo objekt
        UserInfo userInfo = userRepository.findByUsername(username);

        // 2. Provjeri postoji li već token za ovog korisnika
        Optional<RefreshToken> existingToken = refreshTokenRepository.findByUserInfo(userInfo);

        // 3. Obriši stari token ako postoji
        existingToken.ifPresent(refreshTokenRepository::delete);

        // 4. Kreiraj novi token
        RefreshToken refreshToken = RefreshToken.builder()
                .userInfo(userInfo)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(600000)) // 10 minuta
                .build();

        // 5. Spremi i vrati novi token
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
     * Ako jeste, briše ga i baca exception
     */
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new TokenExpiredException(token.getToken() + " Refresh token is expired. Please make a new login!");
        }
        return token;
    }
}