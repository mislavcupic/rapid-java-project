package hr.algebra.rapid.logisticsandfleetmanagementsystem.service.impl;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.Driver;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.UserInfo;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.DriverRequestDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.DriverResponseDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.exceptions.ConflictException;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.exceptions.ResourceNotFoundException;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.DriverRepository;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.UserRepository;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.DriverService;

import lombok.RequiredArgsConstructor;
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

    // --- Pomoćna metoda za mapiranje (Driver -> Response DTO) ---
    private DriverResponseDTO mapToResponse(Driver driver) {
        return DriverResponseDTO.fromDriver(driver);
    }

    // --- READ Implementacija ---

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

    // Metoda za JWT autorizaciju (implementacija prilagođena Vašem UserRepository-ju)
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

    // --- CREATE Implementacija ---

    @Override
    @Transactional
    public DriverResponseDTO createDriver(DriverRequestDTO request) {

        // 1. Dohvaćanje i provjera postojanja UserInfo (korisničkog računa)
        UserInfo userInfo = userRepository.findById(request.getUserInfoId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "ID", request.getUserInfoId()));

        // 2. Provjera konflikta: je li UserInfo već vezan uz Driver profil
        if (driverRepository.findByUserInfoId(userInfo.getId()).isPresent()) {
            throw new ConflictException("User with ID " + userInfo.getId() + " is already registered as a driver.");
        }

        // 3. Provjera konflikta: duplikat broja dozvole
        if (driverRepository.findByLicenseNumber(request.getLicenseNumber()).isPresent()) {
            throw new ConflictException("Driver profile with license number " + request.getLicenseNumber() + " already exists.");
        }

        // 4. Mapiranje DTO-a u entitet
        Driver driver = new Driver();
        driver.setUserInfo(userInfo);
        driver.setLicenseNumber(request.getLicenseNumber());
        driver.setLicenseExpirationDate(request.getLicenseExpirationDate());
        driver.setPhoneNumber(request.getPhoneNumber());

        // 5. Spremanje
        Driver savedDriver = driverRepository.save(driver);
        return mapToResponse(savedDriver);
    }

    // --- UPDATE Implementacija ---

    @Override
    @Transactional
    public DriverResponseDTO updateDriver(Long id, DriverRequestDTO request) {

        // 1. Pronađi postojeći Driver profil
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver", "ID", id));

        // 2. Provjera konflikta: duplikat broja dozvole (ako je promijenjen)
        if (!driver.getLicenseNumber().equals(request.getLicenseNumber())) {
            if (driverRepository.findByLicenseNumber(request.getLicenseNumber()).isPresent()) {
                throw new ConflictException("Driver profile with license number " + request.getLicenseNumber() + " already exists.");
            }
        }

        // 3. Ažuriranje podataka
        // Napomena: userInfoId se ne smije mijenjati kroz UPDATE operaciju
        driver.setLicenseNumber(request.getLicenseNumber());
        driver.setLicenseExpirationDate(request.getLicenseExpirationDate());
        driver.setPhoneNumber(request.getPhoneNumber());

        // 4. Spremanje
        Driver updatedDriver = driverRepository.save(driver);
        return mapToResponse(updatedDriver);
    }

    // --- DELETE Implementacija ---

    @Override
    @Transactional
    public void deleteDriver(Long id) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver", "ID", id));

        // Opcionalna provjera: Ne možete obrisati vozača ako ima aktivne dodjele ili je dodijeljen vozilu.
        // Ovdje je ključna Vaša poslovna logika (npr. provjera assignmentRepository.findByDriverId(id) )

        // 1. Uklanjanje reference iz vozila (ako postoji)
        driver.getCurrentVehicle().ifPresent(vehicle -> vehicle.setCurrentDriver(null));

        // 2. Brisanje Driver profila
        driverRepository.delete(driver);
        // UserInfo račun ostaje netaknut, ali uloga 'DRIVER' ostaje dok je Admin ne ukloni.
    }
}