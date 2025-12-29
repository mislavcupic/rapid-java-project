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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private UserRoleRepository userRoleRepository;
    @Mock private DriverRepository driverRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private UserInfo testUser;
    private Driver testDriver;
    private RegisterRequestDTO regDto;
    private UserRole driverRole;

    @BeforeEach
    void setUp() {
        driverRole = new UserRole();
        driverRole.setId(1L);
        driverRole.setName("ROLE_DRIVER");

        testUser = new UserInfo();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@test.com");
        testUser.setRoles(new ArrayList<>(List.of(driverRole)));

        testDriver = new Driver();
        testDriver.setId(10L);
        testDriver.setUserInfo(testUser);

        regDto = new RegisterRequestDTO();
        regDto.setUsername("newuser");
        regDto.setPassword("password");
    }

    // ==========================================
    // 1. METODA: registerUser
    // ==========================================

    @Test
    @DisplayName("registerUser: Success")
    void registerUser_Success() {
        when(userRepository.findByUsername(anyString())).thenReturn(null);
        when(userRoleRepository.findByName("ROLE_DRIVER")).thenReturn(Optional.of(driverRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPass");
        when(userRepository.save(any(UserInfo.class))).thenReturn(testUser);

        UserInfo result = userService.registerUser(regDto);

        assertNotNull(result);
        verify(userRepository).save(any(UserInfo.class));
    }


    @Test
    @DisplayName("registerUser: Branch - User Already Exists")
    void registerUser_AlreadyExists_ThrowsRuntimeException() {
        // Mockamo da korisnik postoji
        when(userRepository.findByUsername("newuser")).thenReturn(testUser);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            userService.registerUser(regDto);
        });


        assertNotNull(ex.getMessage());
        assertTrue(ex.getMessage().contains("Korisničko ime već postoji"),
                "Stvarna poruka je : " + ex.getMessage());
    }

    @Test
    @DisplayName("registerUser: Branch - Role Not Found")
    void registerUser_RoleNotFound_ThrowsException() {
        when(userRepository.findByUsername(anyString())).thenReturn(null);
        when(userRoleRepository.findByName("ROLE_DRIVER")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.registerUser(regDto));
    }

    // ==========================================
    // 2. METODA: deleteUser
    // ==========================================

    @Test
    @DisplayName("deleteUser: Branch - Is Driver (True)")
    void deleteUser_IsDriver_True() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(driverRepository.findByUserInfoId(1L)).thenReturn(Optional.of(testDriver));

        userService.deleteUser(1L);

        verify(driverRepository).delete(testDriver);
        verify(userRepository).delete(testUser);
    }

    @Test
    @DisplayName("deleteUser: Branch - Not Driver (False)")
    void deleteUser_IsNotDriver_False() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(driverRepository.findByUserInfoId(1L)).thenReturn(Optional.empty());

        userService.deleteUser(1L);

        verify(driverRepository, never()).delete(any());
        verify(userRepository).delete(testUser);
    }

    // ==========================================
    // 3. METODA: updateUserRoles (NOVO - MNOGO GRANA)
    // ==========================================

    @Test
    @DisplayName("updateUserRoles: Success")
    void updateUserRoles_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRoleRepository.findByName("ROLE_ADMIN")).thenReturn(Optional.of(new UserRole()));
        when(userRepository.saveAndFlush(any())).thenReturn(testUser);

        UserInfo result = userService.updateUserRoles(1L, List.of("ROLE_ADMIN"));

        assertNotNull(result);
        verify(userRepository).saveAndFlush(any());
    }

    @Test
    @DisplayName("updateUserRoles: Branch - Empty Role List")
    void updateUserRoles_EmptyRoles_ThrowsIllegalArgument() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        assertThrows(IllegalArgumentException.class, () -> userService.updateUserRoles(1L, List.of()));
        assertThrows(IllegalArgumentException.class, () -> userService.updateUserRoles(1L, null));
    }

    @Test
    @DisplayName("updateUserRoles: Branch - Role Name Not Found")
    void updateUserRoles_RoleNotFound_ThrowsResourceNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRoleRepository.findByName("FAKE_ROLE")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.updateUserRoles(1L, List.of("FAKE_ROLE")));
    }

    // ==========================================
    // 4. METODA: existsByEmail (NOVO - STREAM COVERAGE)
    // ==========================================

    @Test
    @DisplayName("existsByEmail: Branch - Found (True)")
    void existsByEmail_Found_ReturnsTrue() {
        when(userRepository.findAll()).thenReturn(List.of(testUser));

        boolean result = userService.existsByEmail("test@test.com");

        assertTrue(result);
    }

    @Test
    @DisplayName("existsByEmail: Branch - Not Found (False)")
    void existsByEmail_NotFound_ReturnsFalse() {
        when(userRepository.findAll()).thenReturn(List.of(testUser));

        boolean result = userService.existsByEmail("wrong@email.com");

        assertFalse(result);
    }

    // ==========================================
    // 5. OSTALE METODE (findById, findAllUsers)
    // ==========================================

    @Test
    @DisplayName("findById: Success")
    void findById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        UserInfo result = userService.findById(1L);
        assertEquals(1L, result.getId());
    }

    @Test
    @DisplayName("findById: Throws Exception")
    void findById_NotFound_ThrowsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.findById(99L));
    }

    @Test
    @DisplayName("findAllUsers: Returns List")
    void findAllUsers_ReturnsList() {
        when(userRepository.findAll()).thenReturn(List.of(testUser));
        List<UserInfo> result = userService.findAll();
        assertEquals(1, result.size());
    }
}