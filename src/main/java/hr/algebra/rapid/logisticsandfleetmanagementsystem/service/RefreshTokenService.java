//package hr.algebra.rapid.logisticsandfleetmanagementsystem.service;
//
//import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.RefreshToken;
//import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.RefreshTokenRepository;
//import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.UserRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.time.Instant;
//import java.util.Optional;
//import java.util.UUID;
//
//@Service
//public class RefreshTokenService {
//
//    @Autowired
//    RefreshTokenRepository refreshTokenRepository;
//
//    @Autowired
//    UserRepository userRepository;
//
//    public RefreshToken createRefreshToken(String username){
//        RefreshToken refreshToken = RefreshToken.builder()
//                .userInfo(userRepository.findByUsername(username))
//                .token(UUID.randomUUID().toString())
//                .expiryDate(Instant.now().plusMillis(600000)) // set expiry of refresh token to 10 minutes - you can configure it application.properties file
//                .build();
//        return refreshTokenRepository.save(refreshToken);
//    }
//
//    public Optional<RefreshToken> findByToken(String token){
//        return refreshTokenRepository.findByToken(token);
//    }
//
//    public RefreshToken verifyExpiration(RefreshToken token){
//        if(token.getExpiryDate().compareTo(Instant.now())<0){
//            refreshTokenRepository.delete(token);
//            throw new RuntimeException(token.getToken() + " Refresh token is expired. Please make a new login..!");
//        }
//        return token;
//    }
//
//}
package hr.algebra.rapid.logisticsandfleetmanagementsystem.service;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.RefreshToken;
// NAPOMENA: Dodajte ispravan import za Vašu User Entity klasu (npr. UserInfo, ApplicationUser)
// Pretpostavljam da je to UserInfo
import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.UserInfo;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.RefreshTokenRepository;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    @Autowired
    RefreshTokenRepository refreshTokenRepository;

    @Autowired
    UserRepository userRepository;

    public RefreshToken createRefreshToken(String username){

        // 1. Dohvaćanje UserInfo objekta
        // Pretpostavljamo da userRepository.findByUsername() vraća UserInfo
        UserInfo userInfo = userRepository.findByUsername(username);

        // **KRITIČNA KOREKCIJA:** Rješavanje 409 Conflict greške.
        // 2. Pronađi postojeći token za tog korisnika.
        // NAPOMENA: MORATE dodati metodu u RefreshTokenRepository:
        // Optional<RefreshToken> findByUserInfo(UserInfo userInfo);
        Optional<RefreshToken> existingToken = refreshTokenRepository.findByUserInfo(userInfo);

        // 3. Obriši stari token, ako postoji.
        if (existingToken.isPresent()) {
            refreshTokenRepository.delete(existingToken.get());
        }

        // 4. Kreiraj novi token
        RefreshToken refreshToken = RefreshToken.builder()
                .userInfo(userInfo) // Koristimo dohvaćeni UserInfo objekt
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(600000)) // 10 minuta
                .build();

        // 5. Spremi novi token (sada je jedinstven)
        return refreshTokenRepository.save(refreshToken);
    }

    public Optional<RefreshToken> findByToken(String token){
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken verifyExpiration(RefreshToken token){
        if(token.getExpiryDate().compareTo(Instant.now())<0){
            refreshTokenRepository.delete(token);
            throw new RuntimeException(token.getToken() + " Refresh token is expired. Please make a new login..!");
        }
        return token;
    }

}