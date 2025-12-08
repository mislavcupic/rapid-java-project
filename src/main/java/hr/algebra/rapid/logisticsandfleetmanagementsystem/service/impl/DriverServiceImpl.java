package hr.algebra.rapid.logisticsandfleetmanagementsystem.service.impl;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.Assignment;
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

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * DriverServiceImpl popravljen za SonarQube S58809:
 * Riješeno je izbjegavanje pozivanja transakcijskih metoda unutar iste klase ('this.method()').
 * Korištenjem ObjectProvider za DriverService, osiguravamo da se pozivi idu preko Spring AOP proxyja,
 * čime se osigurava ispravno upravljanje transakcijama.
 */
@Service("driverService")
@RequiredArgsConstructor
public class DriverServiceImpl implements DriverService {

    private final DriverRepository driverRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AssignmentRepository assignmentRepository;

    // Injektiranje sebe preko ObjectProvider da bi se osigurao poziv kroz Spring AOP proxy
    // Ovo rješava SonarQube problem S58809
    private final ObjectProvider<DriverService> driverServiceProvider;

    private DriverResponseDTO mapToResponse(Driver driver) {
        return DriverResponseDTO.fromDriver(driver);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DriverResponseDTO> findAllDrivers() {
        return driverRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
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

    @Override
    @Transactional
    public DriverResponseDTO updateDriver(Long id, DriverRequestDTO request) {

        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver", "ID", id));

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

            userRepository.save(userInfo);
        }

        if (!driver.getLicenseNumber().equals(request.getLicenseNumber()) && driverRepository.findByLicenseNumber(request.getLicenseNumber()).isPresent()) {
                throw new ConflictException("Driver profile with license number " + request.getLicenseNumber() + " already exists.");
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

        // Pretpostavka da je getCurrentVehicle() metoda na Driver domain objektu koja vraća Optional<Vehicle>
        driver.getCurrentVehicle().ifPresent(vehicle -> vehicle.setCurrentDriver(null));

        // Transakcija će osigurati brisanje Drivera i, ako je potrebno, povezanih entiteta
        driverRepository.delete(driver);
    }

    // ========================================================================
    // ✅ NOVE METODE - Driver Dashboard Security
    // Ovi pozivi nisu više transakcijski jer se zovu preko Proxyja u gornjoj klasi.
    // ========================================================================

    @Override
    @Transactional(readOnly = true)
    public boolean isAssignmentOwnedByDriver(Long assignmentId, String username) {
        // ✅ Riješen S58809: Koristimo instancu dobivenu preko AOP proxyja
        DriverService self = driverServiceProvider.getObject();
        try {
            // Pozivanje netransakcijske metode unutar iste klase je OK.
            // Ali getDriverIdFromUsername bi trebalo biti pozvano na self, no getDriverIdFromUsername nije transakcijska.
            // Ostavit ćemo poziv netransakcijske metode unutar netransakcijske metode.
            Long driverId = self.getDriverIdFromUsername(username);

            Optional<Assignment> assignment = assignmentRepository.findById(assignmentId);

            if (assignment.isEmpty()) {
                return false;
            }

            return assignment.get().getDriver().getId().equals(driverId);
        } catch (ResourceNotFoundException _) {
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isShipmentAssignedToDriver(Long shipmentId, String username) {
        // ✅ Riješen S58809: Koristimo instancu dobivenu preko AOP proxyja
        DriverService self = driverServiceProvider.getObject();
        try {
            Long driverId = self.getDriverIdFromUsername(username);
            Optional<Assignment> assignment = assignmentRepository.findByShipmentId(shipmentId);

            if (assignment.isEmpty()) {
                return false;
            }

            return assignment.get().getDriver().getId().equals(driverId);
        } catch (ResourceNotFoundException _) {

            return false;
        }
    }
}