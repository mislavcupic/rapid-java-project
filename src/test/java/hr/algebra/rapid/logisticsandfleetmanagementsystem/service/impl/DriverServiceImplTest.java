package hr.algebra.rapid.logisticsandfleetmanagementsystem.service.impl;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.Driver;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.UserInfo;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.UserRole;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.DriverRequestDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.DriverResponseDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.exceptions.ConflictException;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.exceptions.ResourceNotFoundException;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.AssignmentRepository;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.DriverRepository;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.UserRepository;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.UserRoleRepository;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.DriverService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DriverService Unit Tests - CORRECTED")
class DriverServiceImplTest {

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private ObjectProvider<DriverService> driverServiceProvider;

    @InjectMocks
    private DriverServiceImpl driverService;

    private Driver testDriver;
    private UserInfo testUser;
    private UserRole driverRole;
    private DriverRequestDTO driverRequest;

    @BeforeEach
    void setUp() {
        // Setup UserRole
        driverRole = new UserRole();
        driverRole.setId(1L);
        driverRole.setName("ROLE_DRIVER");

        // Setup UserInfo (osobni podaci)
        testUser = new UserInfo();
        testUser.setId(1L);
        testUser.setUsername("driver1");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john.doe@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setRoles(List.of(driverRole));

        // Setup Driver (logistički podaci)
        testDriver = new Driver();
        testDriver.setId(1L);
        testDriver.setUserInfo(testUser);  // ← KLJUČNA VEZA!
        testDriver.setLicenseNumber("DL123456");
        testDriver.setPhoneNumber("+385911234567");
        testDriver.setLicenseExpirationDate(LocalDate.now().plusYears(5));

        // Setup DriverRequestDTO (kombinira UserInfo + Driver podatke)
        driverRequest = new DriverRequestDTO();
        driverRequest.setUsername("driver1");
        driverRequest.setPassword("password123");
        driverRequest.setFirstName("John");
        driverRequest.setLastName("Doe");
        driverRequest.setEmail("john.doe@example.com");
        driverRequest.setLicenseNumber("DL123456");
        driverRequest.setPhoneNumber("+385911234567");
        driverRequest.setLicenseExpirationDate(LocalDate.now().plusYears(5));
    }

    @Nested
    @DisplayName("Find Operations")
    class FindOperations {

        @Test
        @DisplayName("Should return all drivers with UserInfo data")
        void findAllDrivers_ShouldReturnAllDrivers() {
            // Given
            when(driverRepository.findAll()).thenReturn(Arrays.asList(testDriver));

            // When
            List<DriverResponseDTO> result = driverService.findAllDrivers();

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(1L);
            assertThat(result.get(0).getUsername()).isEqualTo("driver1");
            assertThat(result.get(0).getFirstName()).isEqualTo("John");
            assertThat(result.get(0).getLastName()).isEqualTo("Doe");
            assertThat(result.get(0).getEmail()).isEqualTo("john.doe@example.com");
            assertThat(result.get(0).getLicenseNumber()).isEqualTo("DL123456");
            verify(driverRepository).findAll();
        }

        @Test
        @DisplayName("Should return driver by ID with UserInfo data")
        void findDriverById_WhenExists_ShouldReturnDriver() {
            // Given
            when(driverRepository.findById(1L)).thenReturn(Optional.of(testDriver));

            // When
            Optional<DriverResponseDTO> result = driverService.findDriverById(1L);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getFullName()).isEqualTo("John Doe");
            assertThat(result.get().getEmail()).isEqualTo("john.doe@example.com");
            verify(driverRepository).findById(1L);
        }

        @Test
        @DisplayName("Should return empty when driver not found")
        void findDriverById_WhenNotFound_ShouldReturnEmpty() {
            // Given
            when(driverRepository.findById(999L)).thenReturn(Optional.empty());

            // When
            Optional<DriverResponseDTO> result = driverService.findDriverById(999L);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Create Driver")
    class CreateDriver {

        @Test
        @DisplayName("Should create driver with UserInfo account")
        void createDriver_WithValidData_ShouldCreateBothUserInfoAndDriver() {
            // Given
            when(userRepository.findByUsername("driver1")).thenReturn(null);
            when(userRoleRepository.findByName("ROLE_DRIVER")).thenReturn(Optional.of(driverRole));
            when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
            when(userRepository.save(any(UserInfo.class))).thenReturn(testUser);
            when(driverRepository.findByLicenseNumber("DL123456")).thenReturn(Optional.empty());
            when(driverRepository.save(any(Driver.class))).thenReturn(testDriver);

            // When
            DriverResponseDTO result = driverService.createDriver(driverRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo("driver1");
            assertThat(result.getFullName()).isEqualTo("John Doe");
            assertThat(result.getLicenseNumber()).isEqualTo("DL123456");

            verify(userRepository).findByUsername("driver1");
            verify(passwordEncoder).encode("password123");
            verify(userRepository).save(any(UserInfo.class));
            verify(driverRepository).save(any(Driver.class));
        }

        @Test
        @DisplayName("Should throw exception when username already exists")
        void createDriver_WhenUsernameExists_ShouldThrowException() {
            // Given
            when(userRepository.findByUsername("driver1")).thenReturn(testUser);

            // When & Then
            assertThatThrownBy(() -> driverService.createDriver(driverRequest))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("username")
                    .hasMessageContaining("driver1");

            verify(driverRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when license number already exists")
        void createDriver_WhenLicenseExists_ShouldThrowException() {
            // Given
            when(userRepository.findByUsername("driver1")).thenReturn(null);
            when(userRoleRepository.findByName("ROLE_DRIVER")).thenReturn(Optional.of(driverRole));
            when(passwordEncoder.encode(any())).thenReturn("encoded");
            when(userRepository.save(any(UserInfo.class))).thenReturn(testUser);
            when(driverRepository.findByLicenseNumber("DL123456")).thenReturn(Optional.of(testDriver));

            // When & Then
            assertThatThrownBy(() -> driverService.createDriver(driverRequest))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("license number");

            verify(userRepository).delete(testUser); // Cleanup
        }
    }

    @Nested
    @DisplayName("Update Driver")
    class UpdateDriver {

        @Test
        @DisplayName("Should update driver and UserInfo data")
        void updateDriver_WithValidData_ShouldUpdateBoth() {
            // Given
            when(driverRepository.findById(1L)).thenReturn(Optional.of(testDriver));
            when(driverRepository.findByLicenseNumber("DL999999")).thenReturn(Optional.empty());
            when(userRepository.save(any(UserInfo.class))).thenReturn(testUser);
            when(driverRepository.save(any(Driver.class))).thenReturn(testDriver);

            DriverRequestDTO updateRequest = new DriverRequestDTO();
            updateRequest.setFirstName("Jane");
            updateRequest.setLastName("Smith");
            updateRequest.setEmail("jane.smith@example.com");
            updateRequest.setLicenseNumber("DL999999");
            updateRequest.setPhoneNumber("+385912345678");
            updateRequest.setLicenseExpirationDate(LocalDate.now().plusYears(10));

            // When
            DriverResponseDTO result = driverService.updateDriver(1L, updateRequest);

            // Then
            assertThat(result).isNotNull();
            verify(userRepository).save(any(UserInfo.class));
            verify(driverRepository).save(any(Driver.class));
        }

        @Test
        @DisplayName("Should throw exception when driver not found")
        void updateDriver_WhenNotFound_ShouldThrowException() {
            // Given
            when(driverRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> driverService.updateDriver(999L, driverRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Driver");
        }
    }

    @Nested
    @DisplayName("Delete Driver")
    class DeleteDriver {

        @Test
        @DisplayName("Should delete driver successfully")
        void deleteDriver_WhenExists_ShouldDelete() {
            // Given
            when(driverRepository.findById(1L)).thenReturn(Optional.of(testDriver));
            doNothing().when(driverRepository).delete(testDriver);

            // When
            driverService.deleteDriver(1L);

            // Then
            verify(driverRepository).delete(testDriver);
        }

        @Test
        @DisplayName("Should throw exception when driver not found")
        void deleteDriver_WhenNotFound_ShouldThrowException() {
            // Given
            when(driverRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> driverService.deleteDriver(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Get Driver ID from Username")
    class GetDriverIdFromUsername {

        @Test
        @DisplayName("Should return driver ID for valid username")
        void getDriverIdFromUsername_WhenValid_ShouldReturnId() {
            // Given
            when(userRepository.findByUsername("driver1")).thenReturn(testUser);
            when(driverRepository.findByUserInfoId(1L)).thenReturn(Optional.of(testDriver));

            // When
            Long result = driverService.getDriverIdFromUsername("driver1");

            // Then
            assertThat(result).isEqualTo(1L);
            verify(userRepository).findByUsername("driver1");
            verify(driverRepository).findByUserInfoId(1L);
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void getDriverIdFromUsername_WhenUserNotFound_ShouldThrowException() {
            // Given
            when(userRepository.findByUsername("unknown")).thenReturn(null);

            // When & Then
            assertThatThrownBy(() -> driverService.getDriverIdFromUsername("unknown"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("username");
        }

        @Test
        @DisplayName("Should throw exception when driver profile not found")
        void getDriverIdFromUsername_WhenDriverNotFound_ShouldThrowException() {
            // Given
            when(userRepository.findByUsername("driver1")).thenReturn(testUser);
            when(driverRepository.findByUserInfoId(1L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> driverService.getDriverIdFromUsername("driver1"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Driver profile not found");
        }
    }
    // --- DODATAK ZA CREATE DRIVER ---
    @Test
    @DisplayName("Branch: Create - Missing Email (IllegalArgumentException)")
    void createDriver_WhenEmailIsNull_ShouldThrowException() {
        // Postavljamo email na null da pogodimo prvu 'if' granu u servisu
        driverRequest.setEmail(null);

        assertThatThrownBy(() -> driverService.createDriver(driverRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Username, password, first name, last name, and email are required");
    }

    @Test
    @DisplayName("Branch: Create - License Number Conflict (Složena grana s brisanjem)")
    void createDriver_WhenLicenseExists_ShouldDeleteUserAndThrowConflict() {
        // 1. Prolazi provjeru usernamea
        when(userRepository.findByUsername(anyString())).thenReturn(null);
        // 2. Prolazi provjeru role
        when(userRoleRepository.findByName("ROLE_DRIVER")).thenReturn(Optional.of(driverRole));
        // 3. Sprema UserInfo
        when(userRepository.save(any(UserInfo.class))).thenReturn(testUser);

        // 4. OVDJE POGAĐAMO GRANU: Licenca već postoji u bazi
        when(driverRepository.findByLicenseNumber(driverRequest.getLicenseNumber()))
                .thenReturn(Optional.of(testDriver));

        assertThatThrownBy(() -> driverService.createDriver(driverRequest))
                .isInstanceOf(ConflictException.class);

        // Provjera 'cleanup' grane: mora obrisati usera jer driver nije uspio
        verify(userRepository).delete(any(UserInfo.class));
    }
    @Test
    @DisplayName("Branch: Update - Licenca već postoji kod drugog vozača")
    void updateDriver_WhenLicenseConflict_ShouldThrowException() {
        // 1. Pronađi vozača
        when(driverRepository.findById(1L)).thenReturn(Optional.of(testDriver));

        // 2. Postavi novu licencu koja je različita od trenutne ("DL123456")
        driverRequest.setLicenseNumber("NEW-LICENSE-999");

        // 3. Simuliraj da ta nova licenca već postoji u bazi
        when(driverRepository.findByLicenseNumber("NEW-LICENSE-999")).thenReturn(Optional.of(new Driver()));

        assertThatThrownBy(() -> driverService.updateDriver(1L, driverRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("Branch: Delete - Vozač ima aktivne naloge (Conflict)")
    void deleteDriver_WithActiveAssignments_ShouldThrowException() {
        // 1. Mora proći prva linija: findById (linija 118 u servisu)
        when(driverRepository.findById(1L)).thenReturn(Optional.of(testDriver));

        // Mockiram findByDriverId da vrati listu koja NIJE prazna
        // (U DriverServiceImpl.java linija 121 piše: if (!assignments.isEmpty()))
        hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.Assignment mockAssignment =
                new hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.Assignment();

        // Moraš vratiti listu s barem jednim elementom
        when(assignmentRepository.findByDriverId(1L)).thenReturn(List.of(mockAssignment));

        // Act & Assert
        assertThatThrownBy(() -> driverService.deleteDriver(1L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Cannot delete driver with active assignments");
    }
    // --- DODATAK ZA GET ID FROM USERNAME ---
    @Test
    @DisplayName("Branch: GetID - UserInfo postoji ali Driver profil ne")
    void getDriverIdFromUsername_ProfileMissing_ShouldThrowException() {
        when(userRepository.findByUsername("driver1")).thenReturn(testUser);
        // Simuliramo granu: if(driver.isEmpty())
        when(driverRepository.findByUserInfoId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> driverService.getDriverIdFromUsername("driver1"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Driver profile not found");
    }
}