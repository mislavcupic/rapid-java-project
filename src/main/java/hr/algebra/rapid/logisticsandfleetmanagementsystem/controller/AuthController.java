package hr.algebra.rapid.logisticsandfleetmanagementsystem.controller;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.UserInfo;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.AuthRequestDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.AuthResponseDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.RegisterRequestDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.JwtService;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")// Omogući CORS za sve izvore (prilagodi u produkciji)
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    /**
     * 🆕 REGISTER ENDPOINT
     * POST /auth/register
     * Body: { username, password, firstName, lastName, email }
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequestDTO registerRequest) {
        try {
            // 1. Registriraj korisnika kroz service
            UserInfo newUser = userService.registerUser(registerRequest);
            
            // 2. Generiraj JWT token za novog korisnika (automatska prijava nakon registracije)
            String accessToken = jwtService.generateToken(newUser.getUsername());
            
            // 3. Vrati response s tokenom
            AuthResponseDTO response = AuthResponseDTO.builder()
                    .accessToken(accessToken)
                    .username(newUser.getUsername())
                    .message("Registracija uspješna! Dobrodošli, " + newUser.getFirstName() + "!")
                    .build();
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            // Username ili email već postoji
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Greška pri registraciji: " + e.getMessage()));
        }
    }

    /**
     * LOGIN ENDPOINT (postojeći)
     * POST /auth/login
     * Body: { username, password }
     */
    @PostMapping("/login")
    public ResponseEntity<?> authenticateAndGetToken(@RequestBody AuthRequestDTO authRequest) {
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
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Neuspješna prijava: " + e.getMessage()));
        }
    }

    // DTO za error response
    private record ErrorResponse(String message) {}
}
