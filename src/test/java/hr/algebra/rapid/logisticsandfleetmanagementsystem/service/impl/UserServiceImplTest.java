package hr.algebra.rapid.logisticsandfleetmanagementsystem.service.impl;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.UserInfo;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.UserRole;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.RegisterRequestDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.exceptions.ResourceNotFoundException;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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

    @InjectMocks
    private UserServiceImpl userService;

    private UserInfo testUser;
    private UserRole driverRole;
    private UserRole adminRole;
    private RegisterRequestDTO registerRequest;

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

        // Setup registration request
        registerRequest = new RegisterRequestDTO();
        registerRequest.setUsername("newuser");
        registerRequest.setPassword("password123");
        registerRequest.setFirstName("Jane");
        registerRequest.setLastName("Smith");
        registerRequest.setEmail("jane.smith@test.com");
    }

    // ==========================================
    // FIND TESTS
    // ==========================================

    @Test
    void testFindAll_Success() {
        // Arrange
        List<UserInfo> users = Arrays.asList(testUser);
        when(userRepository.findAll()).thenReturn(users);

        // Act
        List<UserInfo> result = userService.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("testuser", result.get(0).getUsername());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void testFindById_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        UserInfo result = userService.findById(1L);

        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void testFindById_NotFound() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.findById(999L);
        });

        verify(userRepository, times(1)).findById(999L);
    }

    // ==========================================
    // REGISTRATION TESTS
    // ==========================================

    @Test
    void testRegisterUser_Success() {
        // Arrange
        when(userRepository.findByUsername("newuser")).thenReturn(null);
        when(userRepository.findAll()).thenReturn(new ArrayList<>());
        when(userRoleRepository.findByName("ROLE_DRIVER")).thenReturn(Optional.of(driverRole));
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword123");
        when(userRepository.save(any(UserInfo.class))).thenReturn(testUser);

        // Act
        UserInfo result = userService.registerUser(registerRequest);

        // Assert
        assertNotNull(result);
        verify(userRepository, times(1)).findByUsername("newuser");
        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepository, times(1)).save(any(UserInfo.class));
    }

    @Test
    void testRegisterUser_UsernameAlreadyExists() {
        // Arrange
        when(userRepository.findByUsername("newuser")).thenReturn(testUser);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser(registerRequest);
        });

        assertTrue(exception.getMessage().contains("zauzeto"));
        verify(userRepository, never()).save(any(UserInfo.class));
    }

    @Test
    void testRegisterUser_EmailAlreadyExists() {
        // Arrange
        when(userRepository.findByUsername("newuser")).thenReturn(null);
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser));

        registerRequest.setEmail(testUser.getEmail());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser(registerRequest);
        });

        assertTrue(exception.getMessage().contains("registriran"));
        verify(userRepository, never()).save(any(UserInfo.class));
    }

    @Test
    void testRegisterUser_RoleNotFound() {
        // Arrange
        when(userRepository.findByUsername("newuser")).thenReturn(null);
        when(userRepository.findAll()).thenReturn(new ArrayList<>());
        when(userRoleRepository.findByName("ROLE_DRIVER")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.registerUser(registerRequest);
        });

        verify(userRepository, never()).save(any(UserInfo.class));
    }

    @Test
    void testRegisterUser_PasswordIsEncoded() {
        // Arrange
        when(userRepository.findByUsername("newuser")).thenReturn(null);
        when(userRepository.findAll()).thenReturn(new ArrayList<>());
        when(userRoleRepository.findByName("ROLE_DRIVER")).thenReturn(Optional.of(driverRole));
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword123");
        when(userRepository.save(any(UserInfo.class))).thenReturn(testUser);

        // Act
        userService.registerUser(registerRequest);

        // Assert
        verify(passwordEncoder, times(1)).encode("password123");
    }

    @Test
    void testRegisterUser_DefaultRoleIsDriver() {
        // Arrange
        when(userRepository.findByUsername("newuser")).thenReturn(null);
        when(userRepository.findAll()).thenReturn(new ArrayList<>());
        when(userRoleRepository.findByName("ROLE_DRIVER")).thenReturn(Optional.of(driverRole));
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any(UserInfo.class))).thenReturn(testUser);

        // Act
        userService.registerUser(registerRequest);

        // Assert
        verify(userRoleRepository, times(1)).findByName("ROLE_DRIVER");
    }

    // ==========================================
    // VALIDATION TESTS
    // ==========================================

    @Test
    void testExistsByUsername_True() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(testUser);

        // Act
        boolean result = userService.existsByUsername("testuser");

        // Assert
        assertTrue(result);
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    void testExistsByUsername_False() {
        // Arrange
        when(userRepository.findByUsername("nonexistent")).thenReturn(null);

        // Act
        boolean result = userService.existsByUsername("nonexistent");

        // Assert
        assertFalse(result);
        verify(userRepository, times(1)).findByUsername("nonexistent");
    }

    @Test
    void testExistsByEmail_True() {
        // Arrange
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser));

        // Act
        boolean result = userService.existsByEmail(testUser.getEmail());

        // Assert
        assertTrue(result);
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void testExistsByEmail_False() {
        // Arrange
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser));

        // Act
        boolean result = userService.existsByEmail("nonexistent@test.com");

        // Assert
        assertFalse(result);
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void testExistsByEmail_CaseInsensitive() {
        // Arrange
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser));

        // Act
        boolean result = userService.existsByEmail("JOHN.DOE@TEST.COM");

        // Assert
        assertTrue(result);
    }

    // ==========================================
    // ROLE MANAGEMENT TESTS
    // ==========================================

    @Test
    void testUpdateUserRoles_Success() {
        // Arrange
        List<String> roleNames = Arrays.asList("ROLE_ADMIN", "ROLE_DISPATCHER");

        UserRole dispatcherRole = new UserRole();
        dispatcherRole.setId(3L);
        dispatcherRole.setName("ROLE_DISPATCHER");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRoleRepository.findByName("ROLE_ADMIN")).thenReturn(Optional.of(adminRole));
        when(userRoleRepository.findByName("ROLE_DISPATCHER")).thenReturn(Optional.of(dispatcherRole));
        when(userRepository.save(any(UserInfo.class))).thenReturn(testUser);

        // Act
        UserInfo result = userService.updateUserRoles(1L, roleNames);

        // Assert
        assertNotNull(result);
        verify(userRoleRepository, times(1)).findByName("ROLE_ADMIN");
        verify(userRoleRepository, times(1)).findByName("ROLE_DISPATCHER");
        verify(userRepository, times(1)).save(any(UserInfo.class));
    }

    @Test
    void testUpdateUserRoles_RoleNotFound() {
        // Arrange
        List<String> roleNames = Arrays.asList("ROLE_NONEXISTENT");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRoleRepository.findByName("ROLE_NONEXISTENT")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.updateUserRoles(1L, roleNames);
        });

        verify(userRepository, never()).save(any(UserInfo.class));
    }

    @Test
    void testUpdateUserRoles_UserNotFound() {
        // Arrange
        List<String> roleNames = Arrays.asList("ROLE_ADMIN");
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.updateUserRoles(999L, roleNames);
        });

        verify(userRepository, never()).save(any(UserInfo.class));
    }

    // ==========================================
    // DELETE TESTS
    // ==========================================

    @Test
    void testDeleteUser_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).delete(any(UserInfo.class));

        // Act
        userService.deleteUser(1L);

        // Assert
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).delete(testUser);
    }

    @Test
    void testDeleteUser_NotFound() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.deleteUser(999L);
        });

        verify(userRepository, never()).delete(any(UserInfo.class));
    }
}
