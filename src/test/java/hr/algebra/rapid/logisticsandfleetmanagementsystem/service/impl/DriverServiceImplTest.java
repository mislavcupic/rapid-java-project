package hr.algebra.rapid.logisticsandfleetmanagementsystem.service.impl;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.Driver;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.UserInfo;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.UserRole;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.DriverRequestDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.DriverResponseDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.DriverUpdateDTO;
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
import org.mockito.ArgumentCaptor;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
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
        @DisplayName("Should update driver data and return response")
        void updateDriver_WithValidData_ShouldUpdateDriverFields() {
            // Given
            when(driverRepository.findById(1L)).thenReturn(Optional.of(testDriver));
            // Pretpostavljamo da tvoj testDriver već ima povezan UserInfo (testUser)
            when(driverRepository.save(any(Driver.class))).thenReturn(testDriver);

            // KORISTIMO NOVI DTO
            DriverUpdateDTO updateRequest = new DriverUpdateDTO();
            updateRequest.setLicenseNumber("DL999999");
            updateRequest.setPhoneNumber("+385912345678");
            updateRequest.setLicenseExpirationDate(LocalDate.now().plusYears(10));

            // When
            DriverResponseDTO result = driverService.updateDriver(1L, updateRequest);

            // Then
            assertThat(result).isNotNull();
            // Više ne verificiramo userRepository.save jer update uloga/user podataka
            // sada ide kroz UserService, a ovdje samo ažuriramo Driver polja.
            verify(driverRepository).save(any(Driver.class));

            // Provjera da su podaci o vozaču ispravni
            assertThat(result.getLicenseNumber()).isEqualTo("DL999999");
        }

        @Test
        @DisplayName("Should throw exception when driver not found")
        void updateDriver_WhenNotFound_ShouldThrowException() {
            // Given
            when(driverRepository.findById(999L)).thenReturn(Optional.empty());

            // Kreiramo prazan update request za test
            DriverUpdateDTO emptyUpdate = new DriverUpdateDTO();

            // When & Then
            assertThatThrownBy(() -> driverService.updateDriver(999L, emptyUpdate))
                    .isInstanceOf(ResourceNotFoundException.class);
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

        // 2. KORISTIMO DriverUpdateDTO umjesto RequestDTO
        DriverUpdateDTO updateDTO = new DriverUpdateDTO();
        updateDTO.setLicenseNumber("NEW-LICENSE-999");
        updateDTO.setPhoneNumber("+385912345678");
        updateDTO.setLicenseExpirationDate(LocalDate.now().plusYears(10));

        // 3. Simuliraj da ta nova licenca već postoji u bazi
        when(driverRepository.findByLicenseNumber("NEW-LICENSE-999")).thenReturn(Optional.of(new Driver()));

        // Ovdje se više neće crveniti jer šaljemo updateDTO
        assertThatThrownBy(() -> driverService.updateDriver(1L, updateDTO))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("Branch: Delete - Driver not found (Throws ResourceNotFoundException)")
    void deleteDriver_WhenNotFound_ShouldThrowResourceNotFound() {
        // Given
        when(driverRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> driverService.deleteDriver(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Driver");
    }
    // --- DODATAK ZA GET ID FROM USERNAME ---
    @Test
    @DisplayName("Branch: GetID - UserInfo postoji ali Driver profil ne")
    void getDriverIdFromUsername_ProfileMissing_ShouldThrowException() {
        when(userRepository.findByUsername("driver1")).thenReturn(testUser);

        when(driverRepository.findByUserInfoId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> driverService.getDriverIdFromUsername("driver1"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Driver profile not found");
    }
    // --- DODATNI TESTOVI ZA MAKSIMALNU POKRIVENOST ---

    @Nested
    @DisplayName("Edge Cases & Branch Coverage")
    class EdgeCases {

        @Test
        @DisplayName("Branch: Create - Missing Fields (IllegalArgumentException)")
        void createDriver_MissingRequiredFields_ShouldThrowException() {
            // Testiramo granu gdje nedostaje npr. prezime (lastName)
            driverRequest.setLastName(null);

            assertThatThrownBy(() -> driverService.createDriver(driverRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Username, password, first name, last name, and email are required");
        }

        @Test
        @DisplayName("Branch: Create - License Number Conflict (Cleanup branch)")
        void createDriver_LicenseConflict_ShouldDeleteSavedUser() {
            // Scenarij: User je spremljen, ali licenca već postoji pa se user mora obrisati (rollback manualno)
            when(userRepository.findByUsername(anyString())).thenReturn(null);
            when(userRoleRepository.findByName(anyString())).thenReturn(Optional.of(driverRole));
            when(passwordEncoder.encode(anyString())).thenReturn("secret");
            when(userRepository.save(any(UserInfo.class))).thenReturn(testUser);

            // Grana: Licenca već postoji
            when(driverRepository.findByLicenseNumber(driverRequest.getLicenseNumber()))
                    .thenReturn(Optional.of(new Driver()));

            assertThatThrownBy(() -> driverService.createDriver(driverRequest))
                    .isInstanceOf(ConflictException.class);

            // Provjera cleanup grane (linija 100 u DriverServiceImpl)
            verify(userRepository).delete(testUser);
        }

        @Test
        @DisplayName("Branch: Update - License Conflict with another driver")
        void updateDriver_NewLicenseAlreadyExists_ShouldThrowConflict() {
            // Given
            when(driverRepository.findById(1L)).thenReturn(Optional.of(testDriver));

            // KORISTIMO DriverUpdateDTO umjesto RequestDTO
            DriverUpdateDTO updateDTO = new DriverUpdateDTO();
            updateDTO.setLicenseNumber("EXISTING-LICENSE-99");
            updateDTO.setPhoneNumber("+385912345678");
            updateDTO.setLicenseExpirationDate(LocalDate.now().plusYears(10));

            // Simuliramo da ta licenca već pripada nekom drugom vozaču
            when(driverRepository.findByLicenseNumber("EXISTING-LICENSE-99"))
                    .thenReturn(Optional.of(new Driver()));

            // When & Then
            assertThatThrownBy(() -> driverService.updateDriver(1L, updateDTO))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("already exists");
        }

        @Test
        @DisplayName("Branch: Update - Partial updates (UserInfo null checks)")
        void updateDriver_PartialData_ShouldNotOverwriteDataWithNull() {
            // Given
            when(driverRepository.findById(1L)).thenReturn(Optional.of(testDriver));
            when(driverRepository.save(any(Driver.class))).thenReturn(testDriver);
            when(userRepository.save(any(UserInfo.class))).thenReturn(testUser);

            // KORISTIMO DriverUpdateDTO i šaljemo samo licencu
            DriverUpdateDTO partialUpdate = new DriverUpdateDTO();
            partialUpdate.setLicenseNumber("NEW-LIC-111");
            partialUpdate.setPhoneNumber("+38591111222"); // Moramo poslati jer je @NotBlank u DTO
            partialUpdate.setLicenseExpirationDate(LocalDate.now().plusYears(5));
            // firstName, lastName, email su null u ovom objektu

            // When
            driverService.updateDriver(1L, partialUpdate);

            // Then
            // Provjeravamo da se UserInfo NIJE promijenio (ostao je "John" iz setUp-a)
            verify(userRepository).save(argThat(user ->
                    user.getFirstName().equals("John") &&
                            user.getEmail().equals("john.doe@example.com")
            ));
            verify(driverRepository).save(any(Driver.class));
        }

        @Test
        @DisplayName("Branch: Security - Assignment Ownership Check")
        void isAssignmentOwnedByDriver_ShouldUseProxyAndReturnBoolean() {
            // Setup za self-invocation preko ObjectProvidera
            when(driverServiceProvider.getObject()).thenReturn(driverService);

            // Mocking interna poziva
            when(userRepository.findByUsername("driver1")).thenReturn(testUser);
            when(driverRepository.findByUserInfoId(1L)).thenReturn(Optional.of(testDriver));

            hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.Assignment assignment =
                    new hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.Assignment();
            assignment.setDriver(testDriver);

            when(assignmentRepository.findById(10L)).thenReturn(Optional.of(assignment));

            boolean result = driverService.isAssignmentOwnedByDriver(10L, "driver1");

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Branch: Security - Shipment Assigned Check Exception path")
        void isShipmentAssignedToDriver_WhenExceptionOccurs_ShouldReturnFalse() {
            when(driverServiceProvider.getObject()).thenReturn(driverService);
            // Simuliramo da getDriverIdFromUsername baci exception
            when(userRepository.findByUsername("error-user")).thenReturn(null);

            boolean result = driverService.isShipmentAssignedToDriver(50L, "error-user");

            assertThat(result).isFalse();
            // Provjera logera (opcionalno, ali pokriva granu catch bloka)
        }
    }
    @Test
    @DisplayName("Branch: Update - Preskakanje null/empty polja u UserInfo")
    void updateDriver_PartialUserInfo_ShouldSkipNullFields() {
        // Given
        testDriver.getUserInfo().setFirstName("John");
        testDriver.getUserInfo().setLastName("Doe");
        testDriver.getUserInfo().setEmail("john.doe@example.com");

        // Mockiramo pronalaženje drivera
        when(driverRepository.findById(1L)).thenReturn(Optional.of(testDriver));

        // ✅ KLJUČNI FIX: Mockiramo i save metodu da ne vrati null
        when(driverRepository.save(any(Driver.class))).thenReturn(testDriver);

        // Pripremamo partial update
        DriverUpdateDTO partialRequest = new DriverUpdateDTO();
        partialRequest.setFirstName("");
        partialRequest.setLastName(null);
        partialRequest.setLicenseNumber("NEW-DL-999");

        // When
        driverService.updateDriver(1L, partialRequest);

        // Then
        // Provjera licence na samom objektu koji je bio u memoriji
        assertEquals("NEW-DL-999", testDriver.getLicenseNumber());

        // Provjera da je save pozvan s originalnim podacima (jer su novi bili null/empty)
        ArgumentCaptor<UserInfo> userCaptor = ArgumentCaptor.forClass(UserInfo.class);
        verify(userRepository).save(userCaptor.capture());

        UserInfo savedUser = userCaptor.getValue();
        assertEquals("John", savedUser.getFirstName());
        assertEquals("Doe", savedUser.getLastName());
    }
    // ========================================================================
    // 🚀 TESTOVI ZA MAX COVERAGE (BRANCH COVERAGE +9%)
    // ========================================================================

    @Test
    @DisplayName("Branch: Update - Preskoči UserInfo polja ako su null ili prazna")
    void updateDriver_WhenUserInfoFieldsAreNullOrEmpty_ShouldNotUpdateThem() {
        // Given
        when(driverRepository.findById(1L)).thenReturn(Optional.of(testDriver));
        when(driverRepository.save(any(Driver.class))).thenReturn(testDriver);
        when(userRepository.save(any(UserInfo.class))).thenReturn(testUser);

        // Šaljemo DTO gdje su firstName i email NULL, a lastName je prazan String ("")
        // Ovo gađa request.getFirstName() != null i !request.getFirstName().isEmpty()
        DriverUpdateDTO nullRequest = new DriverUpdateDTO();
        nullRequest.setLicenseNumber("DL123456");
        nullRequest.setPhoneNumber("+385999999");
        nullRequest.setLicenseExpirationDate(LocalDate.now());
        nullRequest.setFirstName(null);
        nullRequest.setLastName("");
        nullRequest.setEmail(null);

        // When
        driverService.updateDriver(1L, nullRequest);

        // Then
        // Provjeravamo da su u bazi ostala stara imena ("John" i "Doe") iz setUp-a
        verify(userRepository).save(argThat(user ->
                user.getFirstName().equals("John") && user.getLastName().equals("Doe")
        ));
    }

    @Test
    @DisplayName("Branch: Create - Baci IllegalArgumentException ako nedostaju osnovna polja")
    void createDriver_WhenFieldsAreMissing_ShouldThrowIllegalArgumentException() {
        // Given - Request bez emaila (gađa prvu veliku if granu u createDriver)
        DriverRequestDTO invalidRequest = new DriverRequestDTO();
        invalidRequest.setUsername("test");
        invalidRequest.setPassword("pass");
        invalidRequest.setFirstName("First");
        invalidRequest.setLastName("Last");
        invalidRequest.setEmail(null);

        // When & Then
        assertThatThrownBy(() -> driverService.createDriver(invalidRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("are required for new driver creation");
    }

    @Test
    @DisplayName("Branch: Create - Cleanup (Delete User) ako licenca već postoji")
    void createDriver_LicenseExists_ShouldCleanupUserAndThrowConflict() {
        // Given
        when(userRepository.findByUsername(anyString())).thenReturn(null);
        when(userRoleRepository.findByName(anyString())).thenReturn(Optional.of(driverRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(UserInfo.class))).thenReturn(testUser);

        // Gađamo granu gdje licenca već postoji nakon što je user već spremljen
        when(driverRepository.findByLicenseNumber(anyString())).thenReturn(Optional.of(testDriver));

        // When & Then
        assertThatThrownBy(() -> driverService.createDriver(driverRequest))
                .isInstanceOf(ConflictException.class);

        // KLJUČNO ZA COVERAGE: Provjera da se pozvao userRepository.delete(savedUser)
        verify(userRepository).delete(testUser);
    }

    @Test
    @DisplayName("Branch: Security - Vrati false ako assignment ne postoji (Catch blok)")
    void isAssignmentOwnedByDriver_WhenAssignmentMissing_ShouldReturnFalse() {
        // Given
        when(driverServiceProvider.getObject()).thenReturn(driverService);
        when(userRepository.findByUsername("driver1")).thenReturn(testUser);
        when(driverRepository.findByUserInfoId(1L)).thenReturn(Optional.of(testDriver));

        // Simuliramo da assignment ne postoji (Optional.empty)
        when(assignmentRepository.findById(10L)).thenReturn(Optional.empty());

        // When
        boolean result = driverService.isAssignmentOwnedByDriver(10L, "driver1");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Branch: Security - Shipment Assigned Catch blok (ResourceNotFound)")
    void isShipmentAssignedToDriver_WhenException_ShouldReturnFalse() {
        // Given
        when(driverServiceProvider.getObject()).thenReturn(driverService);

        // Simuliramo da getDriverIdFromUsername baci exception (npr. nema usera)
        when(userRepository.findByUsername("unknown")).thenReturn(null);

        // When
        boolean result = driverService.isShipmentAssignedToDriver(50L, "unknown");

        // Then
        assertThat(result).isFalse();
    }

// ========================================================================
    // 🔥 DODATNIH 9 BRANCH TESTOVA ZA 100% COVERAGE
    // ========================================================================

    @Test
    @DisplayName("Branch: Create - User role not found")
    void createDriver_WhenRoleNotFound_ShouldThrowException() {
        // Gađa granu: .orElseThrow(() -> new ResourceNotFoundException("Role"...))
        when(userRepository.findByUsername(anyString())).thenReturn(null);
        when(userRoleRepository.findByName("ROLE_DRIVER")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> driverService.createDriver(driverRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Role");
    }

    @Test
    @DisplayName("Branch: Update - License is same as current (Should skip conflict check)")
    void updateDriver_WhenLicenseIsSame_ShouldNotCheckConflict() {

        when(driverRepository.findById(1L)).thenReturn(Optional.of(testDriver));
        when(driverRepository.save(any(Driver.class))).thenReturn(testDriver);
        when(userRepository.save(any(UserInfo.class))).thenReturn(testUser);

        DriverUpdateDTO sameLicenseRequest = new DriverUpdateDTO();
        sameLicenseRequest.setLicenseNumber(testDriver.getLicenseNumber()); // DL123456
        sameLicenseRequest.setPhoneNumber("+385000000");
        sameLicenseRequest.setLicenseExpirationDate(LocalDate.now());

        driverService.updateDriver(1L, sameLicenseRequest);

        verify(driverRepository, never()).findByLicenseNumber(anyString());
    }

    @Test
    @DisplayName("Branch: Update - UserInfo is null (Should skip user update)")
    void updateDriver_WhenUserInfoIsNull_ShouldSkipUserLogic() {
        // Given
        Driver driverWithoutUser = new Driver();
        driverWithoutUser.setId(1L);
        driverWithoutUser.setUserInfo(null); // Namjerno null da testiramo granu
        driverWithoutUser.setLicenseNumber("OLD");

        when(driverRepository.findById(1L)).thenReturn(Optional.of(driverWithoutUser));
        // Vraćamo isti objekt da mapToResponseDTO ne pukne (kao u prošlim primjerima)
        when(driverRepository.save(any(Driver.class))).thenReturn(driverWithoutUser);

        DriverUpdateDTO request = new DriverUpdateDTO();
        request.setLicenseNumber("NEW");
        request.setPhoneNumber("123");
        // Postavljamo budući datum da izbjegnemo validaciju o kojoj smo pričali
        request.setLicenseExpirationDate(LocalDate.now().plusYears(1));

        // When
        driverService.updateDriver(1L, request);

        // Then
        // 1. Ključna provjera: Budući da je UserInfo bio null, userRepository.save se NIKADA ne smije pozvati
        verify(userRepository, never()).save(any(UserInfo.class));

        // 2. Provjera da se ostatak Driver logike ipak izvršio
        assertEquals("NEW", driverWithoutUser.getLicenseNumber());
        verify(driverRepository).save(driverWithoutUser);
    }

    @Test
    @DisplayName("Branch: Delete - Driver has no vehicle (Skip ifPresent)")
    void deleteDriver_WhenNoVehicle_ShouldSkipClearRelationship() {
        // Gađa granu: driver.getCurrentVehicle().ifPresent(...) - kada je Optional empty
        testDriver.setCurrentVehicle(null); // Pretpostavka da entitet ima metodu ili je null u bazi
        when(driverRepository.findById(1L)).thenReturn(Optional.of(testDriver));

        driverService.deleteDriver(1L);

        verify(driverRepository).delete(testDriver);
    }

    @Test
    @DisplayName("Branch: Security - Shipment not found in any assignment")
    void isShipmentAssignedToDriver_WhenShipmentNotFound_ShouldReturnFalse() {

        when(driverServiceProvider.getObject()).thenReturn(driverService);
        when(userRepository.findByUsername("driver1")).thenReturn(testUser);
        when(driverRepository.findByUserInfoId(1L)).thenReturn(Optional.of(testDriver));
        when(assignmentRepository.findByShipments_Id(99L)).thenReturn(Optional.empty());

        boolean result = driverService.isShipmentAssignedToDriver(99L, "driver1");

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Branch: Security - Assignment belongs to ANOTHER driver")
    void isAssignmentOwnedByDriver_WhenDifferentDriver_ShouldReturnFalse() {
        // Gađa granu: .equals(driverId) kada je rezultat false
        when(driverServiceProvider.getObject()).thenReturn(driverService);
        when(userRepository.findByUsername("driver1")).thenReturn(testUser);
        when(driverRepository.findByUserInfoId(1L)).thenReturn(Optional.of(testDriver));

        Driver otherDriver = new Driver();
        otherDriver.setId(55L); // Drugi ID

        hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.Assignment otherAssignment = new hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.Assignment();
        otherAssignment.setDriver(otherDriver);

        when(assignmentRepository.findById(10L)).thenReturn(Optional.of(otherAssignment));

        boolean result = driverService.isAssignmentOwnedByDriver(10L, "driver1");

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Branch: GetID - User not found (ResourceNotFoundException)")
    void getDriverIdFromUsername_UserNotFound_ShouldThrowException() {

        when(userRepository.findByUsername("missing_user")).thenReturn(null);

        assertThatThrownBy(() -> driverService.getDriverIdFromUsername("missing_user"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Branch: Update - License check (License not present in DB)")
    void updateDriver_WhenLicenseChangedButNotTaken_ShouldProceed() {
        // Gađa granu: ifPresent dio koji se NE izvršava (licenca je nova ali slobodna)
        when(driverRepository.findById(1L)).thenReturn(Optional.of(testDriver));
        when(driverRepository.findByLicenseNumber("NEW-LIC")).thenReturn(Optional.empty());
        when(driverRepository.save(any(Driver.class))).thenReturn(testDriver);
        when(userRepository.save(any(UserInfo.class))).thenReturn(testUser);

        DriverUpdateDTO request = new DriverUpdateDTO();
        request.setLicenseNumber("NEW-LIC");
        request.setPhoneNumber("123");
        request.setLicenseExpirationDate(LocalDate.now());

        driverService.updateDriver(1L, request);

        verify(driverRepository).save(any(Driver.class));
    }

    @Test
    @DisplayName("Branch: Security - ResourceNotFound catch in isAssignmentOwnedByDriver")
    void isAssignmentOwnedByDriver_CatchBlock_ShouldReturnFalse() {

        when(driverServiceProvider.getObject()).thenReturn(driverService);

        when(userRepository.findByUsername("user")).thenThrow(new ResourceNotFoundException("Error", "msg", "val"));

        boolean result = driverService.isAssignmentOwnedByDriver(1L, "user");

        assertThat(result).isFalse();
    }
    @Test
    @DisplayName("Branch: Update - License changed but No Conflict")
    void updateDriver_LicenseChanged_NoConflict_ShouldUpdateSuccessfully() {
        // Gađa granu gdje je licenca promijenjena, ali findByLicenseNumber vrati Optional.empty()
        when(driverRepository.findById(1L)).thenReturn(Optional.of(testDriver));
        when(driverRepository.findByLicenseNumber("UNIQUE-123")).thenReturn(Optional.empty());
        when(driverRepository.save(any(Driver.class))).thenReturn(testDriver);

        DriverUpdateDTO request = new DriverUpdateDTO();
        request.setLicenseNumber("UNIQUE-123");
        request.setPhoneNumber("000");
        request.setLicenseExpirationDate(LocalDate.now());

        driverService.updateDriver(1L, request);
        verify(driverRepository).save(any());
    }

    @Test
    @DisplayName("Branch: Security - Assignment Exists but Driver ID Mismatch")
    void isAssignmentOwnedByDriver_IdMismatch_ShouldReturnFalse() {
        // Setup proxy
        when(driverServiceProvider.getObject()).thenReturn(driverService);
        when(userRepository.findByUsername("driver1")).thenReturn(testUser);
        when(driverRepository.findByUserInfoId(1L)).thenReturn(Optional.of(testDriver));

        // Assignment koji pripada vozaču s ID-om 99 ( mismatch )
        Driver otherDriver = new Driver();
        otherDriver.setId(99L);
        hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.Assignment assignment = new hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.Assignment();
        assignment.setDriver(otherDriver);

        when(assignmentRepository.findById(10L)).thenReturn(Optional.of(assignment));

        boolean result = driverService.isAssignmentOwnedByDriver(10L, "driver1");
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Branch: Create - User role missing (ResourceNotFound)")
    void createDriver_RoleNotFound_ShouldThrowException() {
        // Gađa: .orElseThrow(() -> new ResourceNotFoundException("Role"...))
        when(userRepository.findByUsername(anyString())).thenReturn(null);
        when(userRoleRepository.findByName("ROLE_DRIVER")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> driverService.createDriver(driverRequest))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Branch: Update - FirstName is Empty String")
    void updateDriver_FirstNameEmpty_ShouldNotUpdate() {
        // Given
        testDriver.getUserInfo().setFirstName("John"); // Postavimo početno ime
        when(driverRepository.findById(1L)).thenReturn(Optional.of(testDriver));
        // Dodajemo mock za save da izbjegnemo NullPointerException ako metoda vraća DTO
        when(driverRepository.save(any(Driver.class))).thenReturn(testDriver);

        DriverUpdateDTO request = new DriverUpdateDTO();
        request.setFirstName(""); // Prazan string koji se treba ignorirati
        request.setLicenseNumber("DL123456");

        // When
        driverService.updateDriver(1L, request);

        // Then
        // 1. Hvatom što je zapravo poslano u bazu
        ArgumentCaptor<UserInfo> userCaptor = ArgumentCaptor.forClass(UserInfo.class);
        verify(userRepository).save(userCaptor.capture());

        // 2. Ključna provjera: Ime mora ostati "John", ne smije postati ""
        assertEquals("John", userCaptor.getValue().getFirstName(),
                "Ime je trebalo ostati John jer je u requestu poslan prazan string.");

        // 3. Dodatna sigurnost: Provjeravamo da nikada nije pozvan save s praznim imenom
        verify(userRepository, never()).save(argThat(u -> u.getFirstName().equals("")));
    }



    @Test
    @DisplayName("Branch: Security - Shipment is Empty (Assignment Not Found)")
    void isShipmentAssignedToDriver_AssignmentEmpty_ShouldReturnFalse() {
        when(driverServiceProvider.getObject()).thenReturn(driverService);
        when(userRepository.findByUsername("driver1")).thenReturn(testUser);
        when(driverRepository.findByUserInfoId(1L)).thenReturn(Optional.of(testDriver));


        when(assignmentRepository.findByShipments_Id(anyLong())).thenReturn(Optional.empty());

        boolean result = driverService.isShipmentAssignedToDriver(1L, "driver1");
        assertThat(result).isFalse();
    }
}