package hr.algebra.rapid.logisticsandfleetmanagementsystem.service.impl;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.Driver;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.Vehicle;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.VehicleRequest;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.VehicleResponse;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.DriverResponseDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.exceptions.ConflictException;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.exceptions.ResourceNotFoundException;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.VehicleRepository;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.DriverRepository;
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
    private final DriverRepository driverRepository;

    // Pomoćna metoda za postavljanje Maintenance polja
    private void setMaintenanceFields(Vehicle vehicle, VehicleRequest request) {
        vehicle.setCurrentMileageKm(request.getCurrentMileageKm());
        vehicle.setLastServiceDate(request.getLastServiceDate());
        vehicle.setNextServiceMileageKm(request.getNextServiceMileageKm());
        vehicle.setFuelConsumptionLitersPer100Km(request.getFuelConsumptionLitersPer100Km());
    }

    // --- Pomoćna metoda za mapiranje (Vehicle -> VehicleResponse DTO) ---
    @Override
    public VehicleResponse mapToResponse(Vehicle vehicle) {
        VehicleResponse response = new VehicleResponse();
        response.setId(vehicle.getId());
        response.setLicensePlate(vehicle.getLicensePlate());
        response.setMake(vehicle.getMake());
        response.setModel(vehicle.getModel());
        response.setModelYear(vehicle.getYear());
        response.setFuelType(vehicle.getFuelType());
        response.setLoadCapacityKg(vehicle.getLoadCapacityKg());

        // Maintenance fields
        response.setCurrentMileageKm(vehicle.getCurrentMileageKm());
        response.setLastServiceDate(vehicle.getLastServiceDate());
        response.setNextServiceMileageKm(vehicle.getNextServiceMileageKm());
        response.setFuelConsumptionLitersPer100Km(vehicle.getFuelConsumptionLitersPer100Km());

        // Izračun preostalih kilometara
        if (vehicle.getCurrentMileageKm() != null && vehicle.getNextServiceMileageKm() != null) {
            Long remainingKm = vehicle.getNextServiceMileageKm() - vehicle.getCurrentMileageKm();
            response.setRemainingKmToService(remainingKm);
        } else {
            response.setRemainingKmToService(0L);
        }

        // KONAČNI ISPRAVAK: Koristimo metodu getFullName() iz Vašeg Driver entiteta
        DriverResponseDTO driverDto = vehicle.getCurrentDriverOptional()
                .map(driver -> {
                    DriverResponseDTO dto = new DriverResponseDTO();
                    dto.setId(driver.getId());
                    // KRITIČNA LINIJA: Pozivamo getFullName()
                    dto.setFullName(driver.getFullName());
                    return dto;
                })
                .orElse(null);

        response.setCurrentDriver(driverDto);

        return response;
    }

    // --- 1. READ operacije (Cijela flota) ---

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

    // --- 2. CREATE operacija ---
    @Override
    @Transactional
    public VehicleResponse createVehicle(VehicleRequest request) {
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

        // Postavljanje Maintenance polja
        setMaintenanceFields(vehicle, request);

        // Logika za dodjelu vozača
        if (request.getCurrentDriverId() != null) {
            Driver driver = driverRepository.findById(request.getCurrentDriverId())
                    .orElseThrow(() -> new ResourceNotFoundException("Vozač profil", "ID", request.getCurrentDriverId()));

            // Provjera konflikta
            if (vehicleRepository.findByCurrentDriverId(driver.getId()).isPresent()) {
                throw new ConflictException("Vozač je već dodijeljen drugom vozilu.");
            }
            vehicle.setCurrentDriver(driver);
        }

        Vehicle newVehicle = vehicleRepository.save(vehicle);
        return mapToResponse(newVehicle);
    }

    // --- 3. UPDATE operacija ---
    @Override
    @Transactional
    public VehicleResponse updateVehicle(Long id, VehicleRequest request) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vozilo", "ID", id));

        // Provjera konflikta registracije (NUŽNO za kompletan CRUD)
        vehicleRepository.findByLicensePlate(request.getLicensePlate())
                .ifPresent(v -> {
                    if (!v.getId().equals(id)) {
                        throw new ConflictException("Vozilo s registracijom " + request.getLicensePlate() + " već postoji.");
                    }
                });


        vehicle.setLicensePlate(request.getLicensePlate());
        vehicle.setMake(request.getMake());
        vehicle.setModel(request.getModel());
        vehicle.setYear(request.getModelYear());
        vehicle.setFuelType(request.getFuelType());
        vehicle.setLoadCapacityKg(request.getLoadCapacityKg());

        // Ažuriranje Maintenance polja
        setMaintenanceFields(vehicle, request);

        // Logika za ažuriranje vozača
        Driver driver = null;
        if (request.getCurrentDriverId() != null) {
            driver = driverRepository.findById(request.getCurrentDriverId())
                    .orElseThrow(() -> new ResourceNotFoundException("Vozač profil", "ID", request.getCurrentDriverId()));

            // Provjera konflikta
            vehicleRepository.findByCurrentDriverId(driver.getId()).ifPresent(v -> {
                if (!v.getId().equals(id)) {
                    throw new ConflictException("Vozač je već dodijeljen drugom vozilu: " + v.getLicensePlate());
                }
            });
        }

        vehicle.setCurrentDriver(driver);

        Vehicle updatedVehicle = vehicleRepository.save(vehicle);
        return mapToResponse(updatedVehicle);
    }

    // --- 4. DELETE operacija ---
    @Override
    @Transactional
    public void deleteVehicle(Long id) {
        if (!vehicleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Vozilo", "ID", id);
        }
        vehicleRepository.deleteById(id);
    }

    // --- 5. ANALITIČKE METODE (BROJANJE) ---

    @Override
    @Transactional(readOnly = true)
    public Long countTotalVehicles() {
        return vehicleRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public Long countFreeVehicles() {
        // Vozilo bez vozača
        return vehicleRepository.findAll().stream()
                .filter(vehicle -> vehicle.getCurrentDriver() == null)
                .count();
    }

    @Override
    @Transactional(readOnly = true)
    public Long countVehiclesOverdueForService() {
        // Logika: remainingKmToService < 0 (Prekoračen rok)
        return vehicleRepository.findAll().stream()
                .map(this::mapToResponse)
                .filter(response -> response.getRemainingKmToService() != null && response.getRemainingKmToService() < 0L)
                .count();
    }

    @Override
    @Transactional(readOnly = true)
    public Long countVehiclesInServiceWarning(Long warningThresholdKm) {
        // Logika: 0 < remainingKmToService <= warningThresholdKm (Upozorenje)
        return vehicleRepository.findAll().stream()
                .map(this::mapToResponse)
                .filter(response -> response.getRemainingKmToService() != null
                        && response.getRemainingKmToService() > 0L // KRITIČNA PROMJENA: Mora biti > 0 da isključi Overdue
                        && response.getRemainingKmToService() <= warningThresholdKm)
                .count();
    }

    // --- 6. ANALITIČKE METODE (DOHVAĆANJE LISTA) ---

    @Override
    @Transactional(readOnly = true)
    public List<VehicleResponse> findOverdueMaintenanceVehicles() {
        return vehicleRepository.findAll().stream()
                .map(this::mapToResponse)
                // Logika: remainingKmToService < 0
                .filter(response -> response.getRemainingKmToService() != null && response.getRemainingKmToService() < 0L)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleResponse> findWarningMaintenanceVehicles(Long warningThresholdKm) {
        return vehicleRepository.findAll().stream()
                .map(this::mapToResponse)
                // Logika: 0 < remainingKmToService <= warningThresholdKm
                .filter(response -> response.getRemainingKmToService() != null
                        && response.getRemainingKmToService() > 0L // KRITIČNA PROMJENA: Mora biti > 0
                        && response.getRemainingKmToService() <= warningThresholdKm)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleResponse> findFreeVehiclesDetails() {
        return vehicleRepository.findAll().stream()
                .filter(vehicle -> vehicle.getCurrentDriver() == null)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
}