package hr.algebra.rapid.logisticsandfleetmanagementsystem.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtService {

    // ✅ Injektuj SECRET i expiration iz application.properties
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration.time}")
    private long expirationTimeInSeconds;

    // ----------------------------------------------------------------------
    // PUBLIC API METODE
    // ----------------------------------------------------------------------

    /**
     * Ekstraktuje username iz JWT tokena
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Validira JWT token za danog korisnika
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     * Generiše novi JWT token za korisnika
     */
    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username);
    }

    // ----------------------------------------------------------------------
    // PRIVATNE HELPER METODE
    // ----------------------------------------------------------------------

    /**
     * Ekstraktuje datum isteka tokena
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Generička metoda za ekstrakciju claim-a iz tokena
     */
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Parsira i ekstraktuje sve claim-ove iz tokena
     * ✅ NOVO: Koristi moderni API bez deprecated metoda
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignKey()) // ✅ NOVO: zamjena za setSigningKey
                .build()
                .parseSignedClaims(token)  // ✅ NOVO: zamjena za parseClaimsJws
                .getPayload();              // ✅ NOVO: zamjena za getBody
    }

    /**
     * Provjerava je li token istekao
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Kreira novi JWT token s claim-ovima
     * ✅ NOVO: Koristi moderni builder API
     */
    private String createToken(Map<String, Object> claims, String username) {
        long expirationTimeInMillis = TimeUnit.SECONDS.toMillis(expirationTimeInSeconds);

        return Jwts.builder()
                .claims(claims)             // ✅ NOVO: claims() umjesto setClaims()
                .subject(username)          // ✅ NOVO: subject() umjesto setSubject()
                .issuedAt(new Date(System.currentTimeMillis()))  // ✅ NOVO: issuedAt()
                .expiration(new Date(System.currentTimeMillis() + expirationTimeInMillis)) // ✅ NOVO: expiration()
                .signWith(getSignKey())     // ✅ NOVO: Automatski detektuje algoritam
                .compact();
    }

    /**
     * Generiše signing key iz SECRET stringa
     * ✅ NOVO: Vraća SecretKey umjesto Key
     */
    private SecretKey getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}