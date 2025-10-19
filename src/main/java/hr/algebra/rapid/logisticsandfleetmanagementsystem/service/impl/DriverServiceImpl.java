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


    // --- Pomoćna metoda za mapiranje (Driver -> Response DTO) ---
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


    // --- CREATE Implementacija (ISPRAVAK: DODAN EMAIL) ---
    @Override
    @Transactional
    public DriverResponseDTO createDriver(DriverRequestDTO request) {

        // 1. KRITIČNA VALIDACIJA: Dodajte email u provjeru valjanosti
        if (request.getUsername() == null || request.getPassword() == null ||
                request.getFirstName() == null || request.getLastName() == null ||
                request.getEmail() == null) { // ⭐ DODANA PROVJERA EMAILA

            throw new IllegalArgumentException("Username, password, first name, last name, and email are required for new driver creation.");
        }

        // 2. Provjera konflikta za korisničko ime
        if (userRepository.findByUsername(request.getUsername()) != null) {
            throw new ConflictException("User with username " + request.getUsername() + " already exists.");
        }

        // 3. KREIRANJE USERINFO (Korisnički račun)
        UserInfo userInfo = new UserInfo();

        userInfo.setFirstName(request.getFirstName());
        userInfo.setLastName(request.getLastName());

        // ⭐ KRITIČNO: OVO JE LINIJA KOJA JE NEDOSTAJALA I UZROKOVALA SVE GREŠKE
        userInfo.setEmail(request.getEmail());

        userInfo.setUsername(request.getUsername());
        userInfo.setPassword(passwordEncoder.encode(request.getPassword())); // Hashiranje lozinke


        // 4. Dodjela uloge
        UserRole driverRole = userRoleRepository.findByName("ROLE_DRIVER")
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", "ROLE_DRIVER"));
        userInfo.setRoles(List.of(driverRole));

        UserInfo savedUser = userRepository.save(userInfo);


        // 5. Provjera konflikta: duplikat broja dozvole
        if (driverRepository.findByLicenseNumber(request.getLicenseNumber()).isPresent()) {
            userRepository.delete(savedUser); // Cleanup
            throw new ConflictException("Driver profile with license number " + request.getLicenseNumber() + " already exists.");
        }

        // 6. Mapiranje DTO-a u Driver entitet
        Driver driver = new Driver();
        driver.setUserInfo(savedUser);
        driver.setLicenseNumber(request.getLicenseNumber());
        driver.setLicenseExpirationDate(request.getLicenseExpirationDate());
        driver.setPhoneNumber(request.getPhoneNumber());

        // 7. Spremanje Drivera
        Driver savedDriver = driverRepository.save(driver);
        return mapToResponse(savedDriver);
    }

    // --- UPDATE Implementacija (OSTAVLJENA ISTA - NE MIJENJA USER INFO) ---
    @Override
    @Transactional
    public DriverResponseDTO updateDriver(Long id, DriverRequestDTO request) {

        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver", "ID", id));

        // Provjera konflikta za broj dozvole
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

    // --- DELETE Implementacija ---
    @Override
    @Transactional
    public void deleteDriver(Long id) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver", "ID", id));

        driver.getCurrentVehicle().ifPresent(vehicle -> vehicle.setCurrentDriver(null));

        driverRepository.delete(driver);
        // Opcionalno: Možete ovdje obrisati i UserInfo ako smatrate da se driver briše trajno.
    }
}