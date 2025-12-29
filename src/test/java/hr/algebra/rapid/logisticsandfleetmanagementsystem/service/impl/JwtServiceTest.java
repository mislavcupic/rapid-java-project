package hr.algebra.rapid.logisticsandfleetmanagementsystem.service.impl;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.UserInfo;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.UserRole;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.UserRepository;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private JwtService jwtService;

    // Base64 kodirani ključ (mora biti dovoljno dug za HMAC-SHA)
    private final String SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private final String USERNAME = "testUser";

    @BeforeEach
    void setUp() {
        // Ručno postavljamo @Value polja jer Unit test ne čita application.properties
        ReflectionTestUtils.setField(jwtService, "secret", SECRET);
        ReflectionTestUtils.setField(jwtService, "expirationTimeInSeconds", 3600L);
    }

    @Test
    @DisplayName("generateToken - uspjeh (dohvaća role iz baze)")
    void generateToken_Success() {
        UserInfo userInfo = new UserInfo();
        userInfo.setUsername(USERNAME);
        UserRole role = new UserRole();
        role.setName("ROLE_USER");
        userInfo.setRoles(List.of(role));

        when(userRepository.findByUsername(USERNAME)).thenReturn(userInfo);

        String token = jwtService.generateToken(USERNAME);

        assertThat(token).isNotBlank();
        assertThat(jwtService.extractUsername(token)).isEqualTo(USERNAME);
    }

    @Test
    @DisplayName("generateToken - baca UsernameNotFoundException kad korisnik ne postoji")
    void generateToken_UserNotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(null);

        assertThatThrownBy(() -> jwtService.generateToken("unknown"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    @DisplayName("validateToken - vraća true za ispravan token")
    void validateToken_Valid() {
        // Priprema tokena
        UserInfo userInfo = new UserInfo();
        userInfo.setUsername(USERNAME);
        userInfo.setRoles(Collections.emptyList());
        when(userRepository.findByUsername(USERNAME)).thenReturn(userInfo);
        String token = jwtService.generateToken(USERNAME);

        UserDetails userDetails = new User(USERNAME, "password", Collections.emptyList());

        boolean isValid = jwtService.validateToken(token, userDetails);
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("validateToken - vraća false kad se username ne podudara")
    void validateToken_InvalidUsername() {
        UserInfo userInfo = new UserInfo();
        userInfo.setUsername(USERNAME);
        userInfo.setRoles(Collections.emptyList());
        when(userRepository.findByUsername(USERNAME)).thenReturn(userInfo);
        String token = jwtService.generateToken(USERNAME);

        UserDetails differentUser = new User("otherUser", "password", Collections.emptyList());

        boolean isValid = jwtService.validateToken(token, differentUser);
        assertThat(isValid).isFalse();
    }
}
