package hr.algebra.rapid.logisticsandfleetmanagementsystem.service;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.Driver;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.UserInfo;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.DriverRequestDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.DriverResponseDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.exceptions.ResourceNotFoundException;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.DriverRepository;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.UserRepository;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.UserRoleRepository;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.impl.DriverServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DriverServiceTest {

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private DriverService driverService;
    private Driver driver;
    private UserInfo userInfo;
    private DriverRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        driverService = new DriverServiceImpl(driverRepository, userRepository, userRoleRepository, passwordEncoder);

        userInfo = new UserInfo();
        userInfo.setId(1L);
        userInfo.setUsername("testdriver");
        userInfo.setFirstName("John");
        userInfo.setLastName("Doe");

        driver = new Driver();
        driver.setId(1L);
        driver.setUserInfo(userInfo);
        driver.setLicenseNumber("LIC12345");

        requestDTO = new DriverRequestDTO();
        requestDTO.setLicenseNumber("LIC12345");
    }

    @Test
    void findAllDrivers_ShouldReturnList() {
        when(driverRepository.findAll()).thenReturn(Arrays.asList(driver));
        List<DriverResponseDTO> result = driverService.findAllDrivers();
        assertThat(result).isNotEmpty();
        verify(driverRepository, times(1)).findAll();
    }

    @Test
    void findDriverById_WhenExists_ShouldReturnDriver() {
        when(driverRepository.findById(1L)).thenReturn(Optional.of(driver));
        Optional<DriverResponseDTO> result = driverService.findDriverById(1L);
        assertThat(result).isPresent();
    }

    @Test
    void createDriver_WhenValid_ShouldCreateDriver() {
        when(driverRepository.save(any(Driver.class))).thenReturn(driver);
        DriverResponseDTO result = driverService.createDriver(requestDTO);
        assertThat(result).isNotNull();
    }

    @Test
    void deleteDriver_WhenExists_ShouldDelete() {
        when(driverRepository.existsById(1L)).thenReturn(true);
        driverService.deleteDriver(1L);
        verify(driverRepository, times(1)).deleteById(1L);
    }
}
