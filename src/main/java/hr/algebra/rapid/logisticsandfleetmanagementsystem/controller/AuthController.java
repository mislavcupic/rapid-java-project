package hr.algebra.rapid.logisticsandfleetmanagementsystem.controller;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.RefreshToken;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.AuthRequestDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.JwtResponseDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.RefreshTokenRequestDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.JwtService;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.RefreshTokenService;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("auth")
@AllArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    private AuthenticationManager authenticationManager;

    private JwtService jwtService;

    private RefreshTokenService refreshTokenService;

    @PostMapping("/login")
    public JwtResponseDTO authenticateAndGetToken(@RequestBody AuthRequestDTO authRequestDTO){
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequestDTO.getUsername(), authRequestDTO.getPassword()));
        if(authentication.isAuthenticated()){
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(authRequestDTO.getUsername());
            return JwtResponseDTO.builder()
                    .accessToken(jwtService.generateToken(authRequestDTO.getUsername()))
                    .token(refreshToken.getToken())
                    .build();
        } else {
            throw new UsernameNotFoundException("invalid user request..!!");
        }
    }

    @PostMapping("/api/v1/refreshToken")
    public JwtResponseDTO refreshToken(@RequestBody RefreshTokenRequestDTO refreshTokenRequestDTO){
        return refreshTokenService.findByToken(refreshTokenRequestDTO.getToken())
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUserInfo)
                .map(userInfo -> {
                    String accessToken = jwtService.generateToken(userInfo.getUsername());
                    return JwtResponseDTO.builder()
                            .accessToken(accessToken)
                            .token(refreshTokenRequestDTO.getToken()).build();
                }).orElseThrow(() ->new RuntimeException("Refresh Token is not in DB..!!"));
    }

    @PostMapping("/api/v1/logout")
    public void logout() {
        System.out.println("Logout...");
    }

}
