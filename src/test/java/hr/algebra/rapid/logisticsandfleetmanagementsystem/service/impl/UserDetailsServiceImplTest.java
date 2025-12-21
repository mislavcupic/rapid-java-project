package hr.algebra.rapid.logisticsandfleetmanagementsystem.service.impl;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.UserInfo;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.UserRole;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.UserRepository;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.UserDetailsServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserDetailsServiceImpl Brutal Coverage Test")
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    @DisplayName("Branch: User not found - Throws UsernameNotFoundException")
    void givenNonExistingUser_whenLoadByUsername_thenThrowsException() {
        String username = "nepostojeci";
        given(userRepository.findByUsername(username)).willReturn(null);

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername(username))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("Unknown user: " + username);
    }

    @Test
    @DisplayName("Branch: User exists with mixed roles - Coverage for removeRolePrefix")
    void givenUserWithMixedRoles_whenLoadByUsername_thenReturnsCorrectUserDetails() {
        // Pokriva sve grane u removeRolePrefix:
        // 1. roleName.startsWith("ROLE_") -> true
        // 2. roleName.startsWith("ROLE_") -> false
        String username = "testUser";

        UserInfo user = new UserInfo();
        user.setUsername(username);
        user.setPassword("password123");

        UserRole role1 = new UserRole();
        role1.setName("ROLE_ADMIN"); // Pokriva true granu (substring(5))

        UserRole role2 = new UserRole();
        role2.setName("DRIVER");     // Pokriva false granu (vraća roleName)

        user.setRoles(List.of(role1, role2));

        given(userRepository.findByUsername(username)).willReturn(user);

        // Act
        UserDetails result = userDetailsService.loadUserByUsername(username);

        // Assert
        assertThat(result.getUsername()).isEqualTo(username);
        assertThat(result.getPassword()).isEqualTo("password123");

        // Provjera jesu li role ispravno očišćene (Spring Security .roles() dodaje ROLE_ interno,
        // ali mi testiramo našu logiku micanja prefiksa prije slanja u builder)
        assertThat(result.getAuthorities()).extracting("authority")
                .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_DRIVER");

        verify(userRepository).findByUsername(username);
    }
}