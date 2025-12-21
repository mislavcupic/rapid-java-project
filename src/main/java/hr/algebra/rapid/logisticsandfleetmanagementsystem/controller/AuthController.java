package hr.algebra.rapid.logisticsandfleetmanagementsystem.controller;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.UserInfo;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.AuthRequestDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.AuthResponseDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.RegisterRequestDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.JwtService;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.RefreshTokenService;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.RefreshToken;

// Importi za kolačiće
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import java.util.Arrays;


@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    public static final String REFRESH_TOKEN = "refreshToken";

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;

    // Injektiramo trajanje refresh tokena u milisekundama
    @Value("${refresh.token.expiration.ms}")
    private long refreshTokenExpirationMs;

    /**
     * Pomoćna metoda za postavljanje Refresh Token kolačića.
     * ✅ KRITIČNA KOREKCIJA: putanja je postavljena na root ("/").
     */
    private void setRefreshTokenCookie(String token, HttpServletResponse response) {
        // Konvertiraj ms u sekunde
        int maxAgeInSeconds = (int) (refreshTokenExpirationMs / 1000);

        Cookie cookie = new Cookie(REFRESH_TOKEN, token);
        cookie.setHttpOnly(true);   // Nedostupan JavaScriptu (XSS zaštita)
        cookie.setSecure(false);    // Koristite 'true' samo za HTTPS (produkcija)


        cookie.setPath("/");

        cookie.setMaxAge(maxAgeInSeconds);
        response.addCookie(cookie);
    }

    /**
     * Pomoćna metoda za brisanje kolačića.
     * Putanja mora odgovarati putanji postavljanja (Root).
     */
    private void clearRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(REFRESH_TOKEN, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);

        // Putanja mora biti "/" za brisanje!
        cookie.setPath("/");

        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    // REGISTER ENDPOINT
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> registerUser(
            @Valid @RequestBody RegisterRequestDTO registerRequest,
            HttpServletResponse response
    ) {
        try {
            UserInfo newUser = userService.registerUser(registerRequest);
            String username = newUser.getUsername();
            String accessToken = jwtService.generateToken(username);

            // 1. KREIRAJ I SPREMI REFRESH TOKEN U BAZU
            String refreshToken = refreshTokenService.createRefreshToken(username).getToken();

            // 2. POSTAVI REFRESH TOKEN U HTTP-ONLY COOKIE (Korigirana putanja "/")
            setRefreshTokenCookie(refreshToken, response);

            AuthResponseDTO authResponse = AuthResponseDTO.builder()
                    .accessToken(accessToken)
                    .username(username)
                    .message("Registracija uspješna! Dobrodošli, " + newUser.getFirstName() + "!")
                    .refreshToken(null)
                    .build();

            return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
        } catch (Exception err) {
            logger.error(err.getMessage(), err);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(AuthResponseDTO.builder().message("Greška pri registraciji").build());
        }
    }


    // LOGIN ENDPOINT
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> authenticateAndGetToken(
            @RequestBody AuthRequestDTO authRequest,
            HttpServletResponse response
    ) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
            );

            if (authentication != null && authentication.isAuthenticated()) {
                String username = authRequest.getUsername();
                String accessToken = jwtService.generateToken(username);

                // 1. KREIRAJ I SPREMI REFRESH TOKEN U BAZU
                String refreshToken = refreshTokenService.createRefreshToken(username).getToken();

                // 2. POSTAVI REFRESH TOKEN U HTTP-ONLY COOKIE (Korigirana putanja "/")
                setRefreshTokenCookie(refreshToken, response);

                // 3. VRATI RESPONSE BEZ REFRESH TOKENA U BODY-JU
                List<String> roles = authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority).toList();

                AuthResponseDTO authResponse = AuthResponseDTO.builder()
                        .accessToken(accessToken)
                        .username(username)
                        .roles(roles)
                        .message("Prijava uspješna!")
                        .refreshToken(null)
                        .build();

                return ResponseEntity.ok(authResponse);
            } else {
                throw new UsernameNotFoundException("Neispravni kredencijali!");


            }

        } catch (Exception err) {
            logger.error(err.getMessage(), err);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(AuthResponseDTO.builder().message("Neuspješna prijava").build());
        }
    }

    /**
     * ✅ ENDPOINT: Osvježava Access Token. Token se čita iz HTTP-Only Cookie-ja.
     */
    @PostMapping("/refreshToken")
    public ResponseEntity<AuthResponseDTO> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String requestRefreshToken = null;

        // 1. DOHVATI REFRESH TOKEN IZ KOLAČIĆA
        if (request.getCookies() != null) {
            requestRefreshToken = Arrays.stream(request.getCookies())
                    .filter(cookie -> cookie.getName().equals(REFRESH_TOKEN))
                    .findFirst()
                    .map(Cookie::getValue)
                    .orElse(null);
        }

        if (requestRefreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthResponseDTO.builder().message("Nedostajući Refresh Token u kolačiću!").build());
        }

        // 2. VERIFICIRAJ TOKEN, GENERIRAJ NOVE TOKENTE, ROTIRAJ
        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUserInfo)
                .map(userInfo -> {
                    // Generiraj novi Access Token
                    String newAccessToken = jwtService.generateToken(userInfo.getUsername());

                    // Rotiraj Refresh Token (Briše stari iz baze, kreira novi i sprema u bazu)
                    String newRefreshToken = refreshTokenService.createRefreshToken(userInfo.getUsername()).getToken();

                    // ✅ KRITIČNA KOREKCIJA: POSTAVI NOVI REFRESH TOKEN U HTTP-ONLY COOKIE (Korigirana putanja "/")
                    setRefreshTokenCookie(newRefreshToken, response);

                    // Pripremi Response DTO (Refresh token je null)
                    List<String> roles = userInfo.getRoles().stream()
                            .map(r -> r.getName()).toList();

                    AuthResponseDTO authResponse = AuthResponseDTO.builder()
                            .accessToken(newAccessToken)
                            .refreshToken(null)
                            .username(userInfo.getUsername())
                            .roles(roles)
                            .message("Tokeni uspješno osvježeni!")
                            .build();

                    return ResponseEntity.ok(authResponse);
                })
                .orElseGet(() -> {
                    clearRefreshTokenCookie(response); // Brišemo kolačić
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(AuthResponseDTO.builder().message("Neuspješno osvježavanje tokena: Token nevažeći ili istekao!").build());
                });
    }
}