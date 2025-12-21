package hr.algebra.rapid.logisticsandfleetmanagementsystem.service.impl;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.Driver;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.UserInfo;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.UserRole;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.RegisterRequestDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.exceptions.ResourceNotFoundException;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.DriverRepository;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.UserRepository;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private UserRoleRepository userRoleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private DriverRepository driverRepository;

    @InjectMocks private UserServiceImpl userService;

    private UserInfo testUser;
    private Driver testDriver;
    private RegisterRequestDTO regDto;

    @BeforeEach
    void setUp() {
        testUser = new UserInfo();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        testDriver = new Driver();
        testDriver.setUserInfo(testUser);

        // Inicijalizacija izvan lambde za Sonar S5778
        regDto = new RegisterRequestDTO();
        regDto.setUsername("mislav");
        regDto.setPassword("password123");
    }

    // ==========================================
    // METODA: registerUser
    // ==========================================

    @Test
    void registerUser_Success() {
        UserRole role = new UserRole();
        role.setName("ROLE_USER");

        // Ako findByUsername u repozitoriju vraća UserInfo (ne Optional), koristimo null
        when(userRepository.findByUsername(anyString())).thenReturn(null);
        when(userRoleRepository.findByName(anyString())).thenReturn(Optional.of(role));
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_pass");
        when(userRepository.save(any(UserInfo.class))).thenReturn(testUser);

        UserInfo result = userService.registerUser(regDto);
        assertNotNull(result);
        verify(userRepository).save(any(UserInfo.class));
    }

    @Test
    void registerUser_Duplicate() {
        // Tvoj kod na liniji 50 baca IllegalArgumentException
        when(userRepository.findByUsername(anyString())).thenReturn(testUser);

        // Promjena očekivane iznimke u IllegalArgumentException + Sonar lambda fix
        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(regDto));
    }

    @Test
    void registerUser_RoleNotFound() {
        when(userRepository.findByUsername(anyString())).thenReturn(null);
        when(userRoleRepository.findByName(anyString())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.registerUser(regDto));
    }

    // ==========================================
    // METODA: deleteUser (BRANCH COVERAGE)
    // ==========================================

    @Test
    void deleteUser_IsDriver_TrueBranch() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(driverRepository.findByUserInfoId(1L)).thenReturn(Optional.of(testDriver));

        userService.deleteUser(1L);

        verify(driverRepository, times(1)).delete(testDriver);
        verify(userRepository, times(1)).delete(testUser);
    }

    @Test
    void deleteUser_IsNotDriver_FalseBranch() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        // Pokrivamo granu gdje korisnik NIJE vozač
        when(driverRepository.findByUserInfoId(1L)).thenReturn(Optional.empty());

        userService.deleteUser(1L);

        verify(driverRepository, never()).delete(any());
        verify(userRepository, times(1)).delete(testUser);
    }

    @Test
    void deleteUser_NotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(99L));
    }
}