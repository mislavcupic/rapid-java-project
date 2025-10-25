// hr.algebra.rapid.logisticsandfleetmanagementsystem.service.impl.DriverServiceImpl.java
package hr.algebra.rapid.logisticsandfleetmanagementsystem.service.impl;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.Driver;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.UserInfo;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.UserRole;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.DriverRequestDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.DriverResponseDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.exceptions.ConflictException;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.exceptions.ResourceNotFoundException;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.DriverRepository;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.UserRepository;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.UserRoleRepository;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.DriverService;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DriverServiceImpl implements DriverService {

    private final DriverRepository driverRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;


    private DriverResponseDTO mapToResponse(Driver driver) {
        return DriverResponseDTO.fromDriver(driver);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DriverResponseDTO> findAllDrivers() {
        return driverRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DriverResponseDTO> findDriverById(Long id) {
        return driverRepository.findById(id).map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getDriverIdFromUsername(String username) {
        UserInfo userInfo = userRepository.findByUsername(username);

        if (userInfo == null) {
            throw new ResourceNotFoundException("User", "username", username);
        }

        Driver driver = driverRepository.findByUserInfoId(userInfo.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Driver profile not found", "User ID", userInfo.getId()));
        return driver.getId();
    }

    @Override
    @Transactional
    public DriverResponseDTO createDriver(DriverRequestDTO request) {

        if (request.getUsername() == null || request.getPassword() == null ||
                request.getFirstName() == null || request.getLastName() == null ||
                request.getEmail() == null) {
            throw new IllegalArgumentException("Username, password, first name, last name, and email are required for new driver creation.");
        }

        if (userRepository.findByUsername(request.getUsername()) != null) {
            throw new ConflictException("User with username " + request.getUsername() + " already exists.");
        }

        UserInfo userInfo = new UserInfo();
        userInfo.setFirstName(request.getFirstName());
        userInfo.setLastName(request.getLastName());
        userInfo.setEmail(request.getEmail());
        userInfo.setUsername(request.getUsername());
        userInfo.setPassword(passwordEncoder.encode(request.getPassword()));

        UserRole driverRole = userRoleRepository.findByName("ROLE_DRIVER")
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", "ROLE_DRIVER"));
        userInfo.setRoles(List.of(driverRole));

        UserInfo savedUser = userRepository.save(userInfo);

        if (driverRepository.findByLicenseNumber(request.getLicenseNumber()).isPresent()) {
            userRepository.delete(savedUser);
            throw new ConflictException("Driver profile with license number " + request.getLicenseNumber() + " already exists.");
        }

        Driver driver = new Driver();
        driver.setUserInfo(savedUser);
        driver.setLicenseNumber(request.getLicenseNumber());
        driver.setLicenseExpirationDate(request.getLicenseExpirationDate());
        driver.setPhoneNumber(request.getPhoneNumber());

        Driver savedDriver = driverRepository.save(driver);
        return mapToResponse(savedDriver);
    }

    // ✅ ISPRAVLJENO - UPDATE SADA AŽURIRA I USERINFO POLJA
    @Override
    @Transactional
    public DriverResponseDTO updateDriver(Long id, DriverRequestDTO request) {

        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver", "ID", id));

        // ✅ AŽURIRAJ USERINFO POLJA (ime, prezime, email)
        UserInfo userInfo = driver.getUserInfo();
        if (userInfo != null) {
            if (request.getFirstName() != null && !request.getFirstName().isEmpty()) {
                userInfo.setFirstName(request.getFirstName());
            }
            if (request.getLastName() != null && !request.getLastName().isEmpty()) {
                userInfo.setLastName(request.getLastName());
            }
            if (request.getEmail() != null && !request.getEmail().isEmpty()) {
                userInfo.setEmail(request.getEmail());
            }
            // Username NE mijenjamo jer je unique i može izazvati konflikte

            userRepository.save(userInfo);
        }

        // ✅ AŽURIRAJ DRIVER POLJA (licenca, telefon)
        if (!driver.getLicenseNumber().equals(request.getLicenseNumber())) {
            if (driverRepository.findByLicenseNumber(request.getLicenseNumber()).isPresent()) {
                throw new ConflictException("Driver profile with license number " + request.getLicenseNumber() + " already exists.");
            }
        }

        driver.setLicenseNumber(request.getLicenseNumber());
        driver.setLicenseExpirationDate(request.getLicenseExpirationDate());
        driver.setPhoneNumber(request.getPhoneNumber());

        Driver updatedDriver = driverRepository.save(driver);
        return mapToResponse(updatedDriver);
    }

    @Override
    @Transactional
    public void deleteDriver(Long id) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver", "ID", id));

        driver.getCurrentVehicle().ifPresent(vehicle -> vehicle.setCurrentDriver(null));

        driverRepository.delete(driver);
    }
}