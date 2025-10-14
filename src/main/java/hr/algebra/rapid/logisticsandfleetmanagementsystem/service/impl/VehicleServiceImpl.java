package hr.algebra.rapid.logisticsandfleetmanagementsystem.service.impl;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.Driver;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.Vehicle;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.VehicleRequest;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.VehicleResponse;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.DriverResponseDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.exceptions.ConflictException;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.exceptions.ResourceNotFoundException;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.VehicleRepository;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.DriverRepository; // ⭐ NOVI REPOZITORIJ
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.VehicleService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository; // ⭐ Koristimo DriverRepository

    // --- Pomoćna metoda za mapiranje (Vehicle -> VehicleResponse DTO) ---
    @Override
    public VehicleResponse mapToResponse(Vehicle vehicle) {
        VehicleResponse dto = new VehicleResponse();
        dto.setId(vehicle.getId());
        dto.setLicensePlate(vehicle.getLicensePlate());
        dto.setMake(vehicle.getMake());
        dto.setModel(vehicle.getModel());
        dto.setModelYear(vehicle.getYear()); // Preimenovano u getYear() ako je to ispravan naziv u entitetu
        dto.setFuelType(vehicle.getFuelType());
        dto.setLoadCapacityKg(vehicle.getLoadCapacityKg());

        // LOGIKA MAPIRANJA VOZAČA
        Driver currentDriver = vehicle.getCurrentDriver(); // Entitet je tipa Driver
        if (currentDriver != null) {
            // Koristimo statičku metodu koja mapira Driver entitet u DTO
            DriverResponseDTO driverDto = DriverResponseDTO.fromDriver(currentDriver);
            dto.setCurrentDriver(driverDto);
        }

        return dto;
    }

    // --- READ operacije ---

    @Override
    @Transactional(readOnly = true)
    public List<VehicleResponse> findAllVehicles() {
        return vehicleRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<VehicleResponse> findVehicleById(Long id) {
        return vehicleRepository.findById(id).map(this::mapToResponse);
    }

    // --- CREATE operacija ---

    @Override
    @Transactional
    public VehicleResponse createVehicle(VehicleRequest request) {

        // 1. Provjera konflikta za registraciju
        if (vehicleRepository.findByLicensePlate(request.getLicensePlate()).isPresent()) {
            throw new ConflictException("Vozilo s registracijom " + request.getLicensePlate() + " već postoji.");
        }

        Vehicle vehicle = new Vehicle();
        vehicle.setLicensePlate(request.getLicensePlate());
        vehicle.setMake(request.getMake());
        vehicle.setModel(request.getModel());
        vehicle.setYear(request.getModelYear());
        vehicle.setFuelType(request.getFuelType());
        vehicle.setLoadCapacityKg(request.getLoadCapacityKg());

        // 2. Logika za postavljanje Driver entiteta (koristimo DriverRepository)
        if (request.getCurrentDriverId() != null) {
            Driver driver = driverRepository.findById(request.getCurrentDriverId())
                    .orElseThrow(() -> new ResourceNotFoundException("Vozač profil", "ID", request.getCurrentDriverId()));

            // 3. Provjera konflikta: Je li vozač već dodijeljen drugom vozilu?
            vehicleRepository.findByCurrentDriverId(driver.getId()).ifPresent(v -> {
                throw new ConflictException("Vozač je već dodijeljen vozilu: " + v.getLicensePlate());
            });

            vehicle.setCurrentDriver(driver);
        } else {
            vehicle.setCurrentDriver(null);
        }

        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        return mapToResponse(savedVehicle);
    }

    // --- UPDATE operacija ---

    @Override
    @Transactional
    public VehicleResponse updateVehicle(Long id, VehicleRequest request) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vozilo", "ID", id));

        // 1. Provjera konflikta za registraciju (ako je promijenjena)
        if (!vehicle.getLicensePlate().equals(request.getLicensePlate()) &&
                vehicleRepository.findByLicensePlate(request.getLicensePlate()).isPresent()) {
            throw new ConflictException("Vozilo s registracijom " + request.getLicensePlate() + " već postoji.");
        }

        vehicle.setLicensePlate(request.getLicensePlate());
        vehicle.setMake(request.getMake());
        vehicle.setModel(request.getModel());
        vehicle.setYear(request.getModelYear());
        vehicle.setFuelType(request.getFuelType());
        vehicle.setLoadCapacityKg(request.getLoadCapacityKg());

        // 2. Logika za ažuriranje vozača
        Driver driver = null;
        if (request.getCurrentDriverId() != null) {
            driver = driverRepository.findById(request.getCurrentDriverId())
                    .orElseThrow(() -> new ResourceNotFoundException("Vozač profil", "ID", request.getCurrentDriverId()));

            // 3. Provjera konflikta: Je li vozač dodijeljen drugom vozilu?
            vehicleRepository.findByCurrentDriverId(driver.getId()).ifPresent(v -> {
                // Dozvolite dodjelu samo ako je riječ o trenutnom vozilu
                if (!v.getId().equals(id)) {
                    throw new ConflictException("Vozač je već dodijeljen drugom vozilu: " + v.getLicensePlate());
                }
            });
        }

        vehicle.setCurrentDriver(driver);

        Vehicle updatedVehicle = vehicleRepository.save(vehicle);
        return mapToResponse(updatedVehicle);
    }

    // --- DELETE operacija ---

    @Override
    @Transactional
    public void deleteVehicle(Long id) {
        if (!vehicleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Vozilo", "ID", id);
        }
        // Opcionalno: Provjera ima li vozilo aktivne pošiljke (assignment) prije brisanja

        vehicleRepository.deleteById(id);
    }
}