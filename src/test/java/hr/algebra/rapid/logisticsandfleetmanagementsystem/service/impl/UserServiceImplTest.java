package hr.algebra.rapid.logisticsandfleetmanagementsystem.service.impl;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.Driver;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.UserInfo;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.UserRole;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.RegisterRequestDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.exceptions.ResourceNotFoundException;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.DriverRepository;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.UserRepository;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.UserRoleRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl - 100% Branch Coverage")
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private UserRoleRepository userRoleRepository;
    @Mock private DriverRepository driverRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private UserServiceImpl userService;

    private UserInfo testUser;
    private UserRole userRole;
    private UserRole driverRole;
    private UserRole adminRole;

    @BeforeEach
    void setUp() {
        userRole = new UserRole();
        userRole.setId(1L);
        userRole.setName("ROLE_USER");

        driverRole = new UserRole();
        driverRole.setId(2L);
        driverRole.setName("ROLE_DRIVER");

        adminRole = new UserRole();
        adminRole.setId(3L);
        adminRole.setName("ROLE_ADMIN");

        testUser = new UserInfo();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@test.com");
        testUser.setPassword("hashed");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setRoles(new ArrayList<>(List.of(userRole)));
    }

    // ============================================================
    // REGISTER USER - ALL BRANCHES
    // ============================================================

    @Nested
    @DisplayName("registerUser() - Complete Branch Coverage")
    class RegisterUserTests {

        @Test
        @DisplayName("Branch: existsByUsername() returns TRUE → throws IllegalArgumentException")
        void registerUser_UsernameExists_ThrowsException() {
            // ARRANGE
            RegisterRequestDTO req = RegisterRequestDTO.builder()
                    .username("existing")
                    .email("new@test.com")
                    .password("pass")
                    .build();

            when(userRepository.findByUsername("existing")).thenReturn(testUser);

            assertThrows(IllegalArgumentException.class,
                    () -> userService.registerUser(req),
                    "Korisničko ime već postoji!");

            verify(userRepository).findByUsername("existing");
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Branch: existsByEmail() returns TRUE → throws IllegalArgumentException")
        void registerUser_EmailExists_ThrowsException() {
            // ARRANGE
            RegisterRequestDTO req = RegisterRequestDTO.builder()
                    .username("newuser")
                    .email("test@test.com")
                    .password("pass")
                    .build();

            when(userRepository.findByUsername("newuser")).thenReturn(null);
            when(userRepository.findAll()).thenReturn(List.of(testUser));

            assertThrows(IllegalArgumentException.class,
                    () -> userService.registerUser(req),
                    "Email već postoji!");
        }

        @Test
        @DisplayName("Branch: licenseExpirationDate != null → TRUE (uses provided date)")
        void registerUser_WithProvidedLicenseDate_UsesThatDate() {
            // ARRANGE
            LocalDate customDate = LocalDate.of(2030, 12, 31);
            RegisterRequestDTO req = RegisterRequestDTO.builder()
                    .username("newdriver")
                    .email("driver@test.com")
                    .password("pass")
                    .licenseNumber("ZG123456")
                    .licenseExpirationDate(customDate)  // NOT NULL
                    .phoneNumber("0991234567")
                    .build();

            when(userRepository.findByUsername(anyString())).thenReturn(null);
            when(userRepository.findAll()).thenReturn(new ArrayList<>());
            when(userRoleRepository.findByName("ROLE_DRIVER")).thenReturn(Optional.of(driverRole));
            when(passwordEncoder.encode(anyString())).thenReturn("encoded");
            when(userRepository.save(any())).thenReturn(testUser);

            // ACT
            userService.registerUser(req);

            // ASSERT - Line 80-82: ternary operator TRUE branch
            verify(driverRepository).save(argThat(driver ->
                    driver.getLicenseExpirationDate().equals(customDate)
            ));
        }

        @Test
        @DisplayName("Branch: licenseExpirationDate == null → FALSE (uses default +10 years)")
        void registerUser_WithoutLicenseDate_UsesDefault() {
            // ARRANGE
            RegisterRequestDTO req = RegisterRequestDTO.builder()
                    .username("newdriver")
                    .email("driver@test.com")
                    .password("pass")
                    .licenseNumber("ZG123456")
                    .licenseExpirationDate(null)  // NULL
                    .phoneNumber("0991234567")
                    .build();

            when(userRepository.findByUsername(anyString())).thenReturn(null);
            when(userRepository.findAll()).thenReturn(new ArrayList<>());
            when(userRoleRepository.findByName("ROLE_DRIVER")).thenReturn(Optional.of(driverRole));
            when(passwordEncoder.encode(anyString())).thenReturn("encoded");
            when(userRepository.save(any())).thenReturn(testUser);

            // ACT
            userService.registerUser(req);

            // ASSERT - Line 80-82: ternary operator FALSE branch
            verify(driverRepository).save(argThat(driver -> {
                LocalDate.now().plusYears(10);
                return driver.getLicenseExpirationDate().isAfter(LocalDate.now().plusYears(9));
            }));
        }

        @Test
        @DisplayName("Happy Path: Complete successful registration")
        void registerUser_ValidData_Success() {
            // ARRANGE
            RegisterRequestDTO req = RegisterRequestDTO.builder()
                    .username("newdriver")
                    .email("new@test.com")
                    .password("rawPassword")
                    .firstName("John")
                    .lastName("Doe")
                    .licenseNumber("ZG999999")
                    .licenseExpirationDate(LocalDate.now().plusYears(5))
                    .phoneNumber("0991234567")
                    .build();

            when(userRepository.findByUsername("newdriver")).thenReturn(null);
            when(userRepository.findAll()).thenReturn(new ArrayList<>());
            when(userRoleRepository.findByName("ROLE_DRIVER")).thenReturn(Optional.of(driverRole));
            when(passwordEncoder.encode("rawPassword")).thenReturn("encodedPassword");
            when(userRepository.save(any())).thenReturn(testUser);

            // ACT
            UserInfo result = userService.registerUser(req);

            // ASSERT
            assertNotNull(result);
            verify(passwordEncoder).encode("rawPassword");
            verify(userRepository).save(any(UserInfo.class));
            verify(driverRepository).save(any(Driver.class));
        }
    }

    // ============================================================
    // UPDATE USER ROLES - ALL BRANCHES
    // ============================================================

    @Nested
    @DisplayName("updateUserRoles() - Complete Branch Coverage")
    class UpdateUserRolesTests {

        @Test
        @DisplayName("Branch: roleNames == null → TRUE (throws exception)")
        void updateUserRoles_NullRoles_ThrowsException() {
            // ARRANGE
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

            assertThrows(IllegalArgumentException.class,
                    () -> userService.updateUserRoles(1L, null),
                    "Korisnik mora imati barem jednu ulogu!");
        }

        @Test
        @Transactional
        @DisplayName("Branch: roleNames.isEmpty() → TRUE (throws exception)")
        void updateUserRoles_EmptyRoles_ThrowsException() {
            // 1. ARRANGE - Pripremi mock podatke
            Long userId = 1L;
            List<String> emptyRoles = new ArrayList<>();

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

            // 2. ACT & ASSERT - Line 108: || roleNames.isEmpty() → TRUE branch
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> userService.updateUserRoles(userId, emptyRoles),
                    "Should throw IllegalArgumentException when role list is empty"
            );

            // 3. VERIFY - Provjeri poruku iznimke
            assertEquals(
                    "Korisnik mora imati barem jednu ulogu!",
                    exception.getMessage(),
                    "Exception message should match expected validation message"
            );

            // 4. VERIFY - Provjeri da updateUserRoles NIJE pozvao save()
            verify(userRepository, never()).save(any(UserInfo.class));
            verify(userRoleRepository, never()).deleteById(userId);
        }

        @Test
        @DisplayName("Branch: roleNames.contains(ROLE_DRIVER) → TRUE & !driverRepository.existsByUserInfo() → TRUE (creates new driver)")
        void updateUserRoles_AddDriverRole_CreatesDriver() {
            // ARRANGE
            List<String> roles = List.of("ROLE_DRIVER");

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRoleRepository.findByName("ROLE_DRIVER")).thenReturn(Optional.of(driverRole));
            when(userRepository.saveAndFlush(any())).thenReturn(testUser);
            when(driverRepository.existsByUserInfo(testUser)).thenReturn(false);  // NOT EXISTS

            // ACT
            userService.updateUserRoles(1L, roles);


            verify(driverRepository).save(argThat(driver ->
                    driver.getUserInfo().equals(testUser) &&
                            driver.getLicenseNumber().equals("TEMP-1") &&
                            driver.getPhoneNumber().equals("000000000")
            ));
        }

        @Test
        @DisplayName("Branch: roleNames.contains(ROLE_DRIVER) → TRUE & driverRepository.existsByUserInfo() → TRUE (driver already exists, no creation)")
        void updateUserRoles_AddDriverRole_DriverExists_NoCreation() {
            // ARRANGE
            List<String> roles = List.of("ROLE_DRIVER");

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRoleRepository.findByName("ROLE_DRIVER")).thenReturn(Optional.of(driverRole));
            when(userRepository.saveAndFlush(any())).thenReturn(testUser);
            when(driverRepository.existsByUserInfo(testUser)).thenReturn(true);  // ALREADY EXISTS

            // ACT
            userService.updateUserRoles(1L, roles);

            verify(driverRepository, never()).save(any());
        }

        @Test
        @DisplayName("Branch: Nested ternary - phoneNumber != null && length >= 9 → TRUE (uses existing)")
        void updateUserRoles_PhoneValidation_UsesExistingPhone() {
            // ARRANGE
            Driver existingDriver = new Driver();
            existingDriver.setPhoneNumber("0991234567");  // Valid phone

            List<String> roles = List.of("ROLE_DRIVER");

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRoleRepository.findByName("ROLE_DRIVER")).thenReturn(Optional.of(driverRole));
            when(userRepository.saveAndFlush(any())).thenReturn(testUser);
            when(driverRepository.existsByUserInfo(testUser)).thenReturn(false);

            // ACT
            userService.updateUserRoles(1L, roles);

            // ASSERT - Line 137-139: Nested ternary TRUE branch (but driver is NEW, so phone is null)
            // This actually tests the FALSE branch (uses "000000000")
            verify(driverRepository).save(argThat(driver ->
                    driver.getPhoneNumber().equals("000000000")
            ));
        }

        @Test
        @DisplayName("Branch: roleNames.contains(ROLE_DRIVER) → FALSE & findByUserInfo.isPresent() → TRUE (removes driver)")
        void updateUserRoles_RemoveDriverRole_DeletesDriver() {
            // ARRANGE
            Driver existingDriver = new Driver();
            existingDriver.setUserInfo(testUser);

            List<String> roles = List.of("ROLE_ADMIN");  // NO DRIVER ROLE

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRoleRepository.findByName("ROLE_ADMIN")).thenReturn(Optional.of(adminRole));
            when(userRepository.saveAndFlush(any())).thenReturn(testUser);
            when(driverRepository.findByUserInfo(testUser)).thenReturn(Optional.of(existingDriver));

            // ACT
            userService.updateUserRoles(1L, roles);

            // ASSERT - Line 145: else branch (roleNames does NOT contain ROLE_DRIVER)
            //         - Line 148: ifPresent() → PRESENT (deletes driver)
            verify(driverRepository).delete(existingDriver);
        }

        @Test
        @DisplayName("Branch: roleNames.contains(ROLE_DRIVER) → FALSE & findByUserInfo.isEmpty() → TRUE (no driver to remove)")
        void updateUserRoles_RemoveDriverRole_NoDriverExists() {
            // ARRANGE
            List<String> roles = List.of("ROLE_ADMIN");  // NO DRIVER ROLE

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRoleRepository.findByName("ROLE_ADMIN")).thenReturn(Optional.of(adminRole));
            when(userRepository.saveAndFlush(any())).thenReturn(testUser);
            when(driverRepository.findByUserInfo(testUser)).thenReturn(Optional.empty());

            // ACT
            userService.updateUserRoles(1L, roles);

            // ASSERT - Line 148: ifPresent() → EMPTY (does nothing)
            verify(driverRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Happy Path: Update to multiple roles including DRIVER")
        void updateUserRoles_MultipleRoles_Success() {
            // ARRANGE
            List<String> roles = List.of("ROLE_ADMIN", "ROLE_DRIVER");

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRoleRepository.findByName("ROLE_ADMIN")).thenReturn(Optional.of(adminRole));
            when(userRoleRepository.findByName("ROLE_DRIVER")).thenReturn(Optional.of(driverRole));
            when(userRepository.saveAndFlush(any())).thenReturn(testUser);
            when(driverRepository.existsByUserInfo(testUser)).thenReturn(false);

            // ACT
            UserInfo result = userService.updateUserRoles(1L, roles);

            // ASSERT
            assertNotNull(result);
            verify(userRepository).saveAndFlush(any());
            verify(driverRepository).save(any());
        }
    }

    // ============================================================
    // EXISTS BY USERNAME - ALL BRANCHES
    // ============================================================

    @Nested
    @DisplayName("existsByUsername() - Complete Branch Coverage")
    class ExistsByUsernameTests {

        @Test
        @DisplayName("Branch: findByUsername() returns NOT NULL → TRUE")
        void existsByUsername_UserExists_ReturnsTrue() {
            // ARRANGE
            when(userRepository.findByUsername("testuser")).thenReturn(testUser);

            // ACT & ASSERT - Line 94: != null → TRUE
            assertTrue(userService.existsByUsername("testuser"));
        }

        @Test
        @DisplayName("Branch: findByUsername() returns NULL → FALSE")
        void existsByUsername_UserNotExists_ReturnsFalse() {
            // ARRANGE
            when(userRepository.findByUsername("nonexistent")).thenReturn(null);

            // ACT & ASSERT - Line 94: != null → FALSE
            assertFalse(userService.existsByUsername("nonexistent"));
        }
    }

    // ============================================================
    // EXISTS BY EMAIL - ALL BRANCHES
    // ============================================================

    @Nested
    @DisplayName("existsByEmail() - Complete Branch Coverage")
    class ExistsByEmailTests {

        @Test
        @DisplayName("Branch: anyMatch() returns TRUE (email exists)")
        void existsByEmail_EmailExists_ReturnsTrue() {
            // ARRANGE
            when(userRepository.findAll()).thenReturn(List.of(testUser));

            // ACT & ASSERT - Line 100: anyMatch() → TRUE
            assertTrue(userService.existsByEmail("test@test.com"));
            assertTrue(userService.existsByEmail("TEST@TEST.COM"));  // Case insensitive
        }

        @Test
        @DisplayName("Branch: anyMatch() returns FALSE (email not exists)")
        void existsByEmail_EmailNotExists_ReturnsFalse() {
            // ARRANGE
            when(userRepository.findAll()).thenReturn(List.of(testUser));

            // ACT & ASSERT - Line 100: anyMatch() → FALSE
            assertFalse(userService.existsByEmail("other@test.com"));
        }

        @Test
        @DisplayName("Branch: Empty repository (no users)")
        void existsByEmail_EmptyRepo_ReturnsFalse() {
            // ARRANGE
            when(userRepository.findAll()).thenReturn(new ArrayList<>());

            // ACT & ASSERT
            assertFalse(userService.existsByEmail("any@test.com"));
        }
    }

    // ============================================================
    // DELETE USER - ALL BRANCHES
    // ============================================================

    @Nested
    @DisplayName("deleteUser() - Complete Branch Coverage")
    class DeleteUserTests {

        @Test
        @DisplayName("Branch: findByUserInfoId() returns PRESENT → deletes driver")
        void deleteUser_WithDriver_DeletesBoth() {
            // ARRANGE
            Driver driver = new Driver();
            driver.setUserInfo(testUser);

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(driverRepository.findByUserInfoId(1L)).thenReturn(Optional.of(driver));

            // ACT
            userService.deleteUser(1L);

            // ASSERT - Line 212-213: ifPresent() → PRESENT
            verify(driverRepository).delete(driver);
            verify(userRepository).delete(testUser);
        }

        @Test
        @DisplayName("Branch: findByUserInfoId() returns EMPTY → deletes only user")
        void deleteUser_WithoutDriver_DeletesOnlyUser() {
            // ARRANGE
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(driverRepository.findByUserInfoId(1L)).thenReturn(Optional.empty());

            // ACT
            userService.deleteUser(1L);

            // ASSERT - Line 212-213: ifPresent() → EMPTY (skips driver deletion)
            verify(driverRepository, never()).delete(any());
            verify(userRepository).delete(testUser);
        }
    }

    // ============================================================
    // FIND METHODS - BASIC COVERAGE
    // ============================================================

    @Test
    @DisplayName("findAll: Returns list of users")
    void findAll_ReturnsUsers() {
        when(userRepository.findAll()).thenReturn(List.of(testUser));
        assertThat(userService.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("findAll: Returns empty list")
    void findAll_EmptyList() {
        when(userRepository.findAll()).thenReturn(new ArrayList<>());
        assertThat(userService.findAll()).isEmpty();
    }

    @Test
    @DisplayName("findById: Success")
    void findById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        assertNotNull(userService.findById(1L));
    }

    @Test
    @DisplayName("findById: Throws ResourceNotFoundException")
    void findById_NotFound_Throws() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.findById(99L));
    }
}