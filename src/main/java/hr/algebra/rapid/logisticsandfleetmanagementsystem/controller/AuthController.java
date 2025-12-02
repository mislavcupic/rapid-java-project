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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor // ✅ Lombok generiše konstruktor
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    //  Final polja - immutable dependencije
    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;




    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> registerUser(@Valid @RequestBody RegisterRequestDTO registerRequest) {
        try {
            UserInfo newUser = userService.registerUser(registerRequest);
            String accessToken = jwtService.generateToken(newUser.getUsername());

            AuthResponseDTO response = AuthResponseDTO.builder()
                    .accessToken(accessToken)
                    .username(newUser.getUsername())
                    .message("Registracija uspješna! Dobrodošli, " + newUser.getFirstName() + "!")
                    .build();

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            logger.warn("Registration failed: {}", e.getMessage());

            AuthResponseDTO errorResponse = AuthResponseDTO.builder()
                    .message(e.getMessage())
                    .build();

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);

        } catch (Exception e) {
            logger.error("Registration error", e);

            AuthResponseDTO errorResponse = AuthResponseDTO.builder()
                    .message("Greška pri registraciji: " + e.getMessage())
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }


    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> authenticateAndGetToken(@RequestBody AuthRequestDTO authRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.getUsername(),
                            authRequest.getPassword()
                    )
            );

            if (authentication.isAuthenticated()) {
                String accessToken = jwtService.generateToken(authRequest.getUsername());

                AuthResponseDTO response = AuthResponseDTO.builder()
                        .accessToken(accessToken)
                        .username(authRequest.getUsername())
                        .message("Prijava uspješna!")
                        .build();

                return ResponseEntity.ok(response);
            } else {
                throw new UsernameNotFoundException("Neispravni kredencijali!");
            }

        } catch (Exception e) {
            logger.warn("Login failed for user: {} - Reason: {}",
                    authRequest.getUsername(),
                    e.getMessage());

            AuthResponseDTO errorResponse = AuthResponseDTO.builder()
                    .message("Neuspješna prijava: " + e.getMessage())
                    .build();

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }
}