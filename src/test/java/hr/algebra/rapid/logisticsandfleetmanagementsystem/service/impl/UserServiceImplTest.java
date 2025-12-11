package hr.algebra.rapid.logisticsandfleetmanagementsystem.service.impl;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.Driver; // 👈 Novi Import
import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.UserInfo;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.UserRole;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.RegisterRequestDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.exceptions.ResourceNotFoundException;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.DriverRepository; // 👈 Novi Import
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.UserRepository;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong; // 👈 Novi Import
import static org.mockito.Mockito.*;

/**
 * UNIT TESTOVI ZA UserServiceImpl
 * Pokriva user registration, role management, validation
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    // 🎯 RJEŠENJE: Ovaj mock je bio ključan i nedostajao je u vašoj test klasi!
    @Mock
    private DriverRepository driverRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private UserInfo testUser;
    private UserRole driverRole;
    private UserRole adminRole;
    private RegisterRequestDTO registerRequest;
    private Driver testDriver; // Dodatni mock objekt za vozača

    @BeforeEach
    void setUp() {
        // Setup roles
        driverRole = new UserRole();
        driverRole.setId(1L);
        driverRole.setName("ROLE_DRIVER");

        adminRole = new UserRole();
        adminRole.setId(2L);
        adminRole.setName("ROLE_ADMIN");

        // Setup test user
        testUser = new UserInfo();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("hashedPassword");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john.doe@test.com");
        testUser.setIsEnabled(true);
        testUser.setRoles(new ArrayList<>(Arrays.asList(driverRole)));

        // Setup test driver povezan s testUser-om
        testDriver = new Driver();
        testDriver.setId(10L);
        testDriver.setUserInfo(testUser);

        // Setup registration request
        registerRequest = new RegisterRequestDTO();
        registerRequest.setUsername("newuser");
        registerRequest.setPassword("password123");
        registerRequest.setFirstName("Jane");
        registerRequest.setLastName("Smith");
        registerRequest.setEmail("jane.smith@test.com");
    }

    // ... (Ostatak vaših FIND, REGISTRATION, VALIDATION, i ROLE MANAGEMENT testova ostaje nepromijenjen) ...

    // ==========================================
    // DELETE TESTS (POPRAVLJENO)
    // ==========================================

    @Test
    void testDeleteUser_Success_UserIsDriver() {
        // Arrange
        Long userId = 1L;

        // 1. Mock: Pronađi korisnika
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // 2. Mock: Pronađi vozača povezanog s korisnikom (Vraća vozača)
        when(driverRepository.findByUserInfoId(userId)).thenReturn(Optional.of(testDriver));

        // 3. Mock: Brisanje vozača
        doNothing().when(driverRepository).delete(testDriver);

        // 4. Mock: Brisanje korisnika
        doNothing().when(userRepository).delete(testUser);

        // Act
        userService.deleteUser(userId);

        // Assert
        verify(userRepository, times(1)).findById(userId);
        // Provjeri da je tražio vozača
        verify(driverRepository, times(1)).findByUserInfoId(userId);
        // Provjeri da je obrisao vozača
        verify(driverRepository, times(1)).delete(testDriver);
        // Provjeri da je obrisao korisnika
        verify(userRepository, times(1)).delete(testUser);
    }

    @Test
    void testDeleteUser_Success_UserIsNotDriver() {
        // Arrange
        Long userId = 1L;

        // 1. Mock: Pronađi korisnika
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // 2. Mock: Pronađi vozača (Vraća prazan Optional jer korisnik nije vozač)
        when(driverRepository.findByUserInfoId(userId)).thenReturn(Optional.empty());

        // 3. Mock: Brisanje korisnika
        doNothing().when(userRepository).delete(testUser);

        // Act
        userService.deleteUser(userId);

        // Assert
        verify(userRepository, times(1)).findById(userId);
        // Provjeri da je tražio vozača
        verify(driverRepository, times(1)).findByUserInfoId(userId);
        // Provjeri da NIJE obrisao vozača
        verify(driverRepository, never()).delete(any(Driver.class));
        // Provjeri da je obrisao korisnika
        verify(userRepository, times(1)).delete(testUser);
    }

    @Test
    void testDeleteUser_NotFound() {
        // Arrange
        Long userId = 999L;

        // Mock: Pronađi korisnika (Vraća prazan Optional)
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.deleteUser(userId);
        });

        // Assert
        verify(userRepository, times(1)).findById(userId); // Pozvano u findById unutar deleteUser
        verify(userRepository, never()).delete(any(UserInfo.class));
        verify(driverRepository, never()).findByUserInfoId(anyLong());
        verify(driverRepository, never()).delete(any(Driver.class));
    }
}