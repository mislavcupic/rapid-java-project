package hr.algebra.rapid.logisticsandfleetmanagementsystem.service.impl;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.Vehicle;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.UserInfo; // Dodan import za UserInfo
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.VehicleRequest;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.VehicleResponse; // DODAN NOVI DTO ZA IZLAZ
import hr.algebra.rapid.logisticsandfleetmanagementsystem.exceptions.ResourceNotFoundException;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.VehicleRepository;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.UserRepository;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors; // Potreban za mapiranje liste

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;

    @Override // Dodajemo @Override jer je metoda sada u interfaceu
    public VehicleResponse mapToResponse(Vehicle vehicle) {
        VehicleResponse dto = new VehicleResponse();
        dto.setId(vehicle.getId());
        dto.setLicensePlate(vehicle.getLicensePlate());
        dto.setMake(vehicle.getMake());
        dto.setModel(vehicle.getModel());
        // Provjerite da li se polje zove modelYear ili year
        dto.setModelYear(vehicle.getYear());
        dto.setFuelType(vehicle.getFuelType());
        dto.setLoadCapacityKg(vehicle.getLoadCapacityKg());

        // Logika za ID vozača
        UserInfo currentDriver = vehicle.getCurrentDriver();
        if (currentDriver != null) {
            dto.setCurrentDriverId(currentDriver.getId());
            // Ako želite puno ime vozača, osigurajte da je entitet inicijaliziran
            // (ovdje možemo pretpostaviti da je u kreiranju/ažuriranju inicijaliziran)
            // dto.setCurrentDriverFullName(currentDriver.getFirstName() + " " + currentDriver.getLastName());
        }

        return dto;
    }
    // --- Glavne metode (Ažurirane za povrat DTO-a) ---

    @Override
    public List<VehicleResponse> findAll() {
        List<Vehicle> vehicles = vehicleRepository.findAll();

        return vehicles.stream()
                .map(this::mapToResponse) // Koristimo novu javnu mapToResponse metodu
                .collect(Collectors.toList());
    }

    // Ažuriramo metodu findById da koristi javnu metodu za mapiranje
    @Override
    public Optional<VehicleResponse> findById(Long id) {
        return vehicleRepository.findById(id)
                .map(this::mapToResponse); // Koristimo novu javnu mapToResponse metodu
    }


    // --- Metode za unos/izmjenu (koriste VehicleRequest DTO) ---

    @Override
    // Ostaje Vehicle za povrat, ali se može promijeniti u VehicleResponse
    public Vehicle createVehicle(VehicleRequest request) {

        Vehicle vehicle = new Vehicle();
        vehicle.setLicensePlate(request.getLicensePlate());
        vehicle.setMake(request.getMake());
        vehicle.setModel(request.getModel());
        // Korištenje modelYear kako je definirano u DTO-u
        vehicle.setYear(request.getModelYear());
        vehicle.setFuelType(request.getFuelType());
        vehicle.setLoadCapacityKg(request.getLoadCapacityKg());

        // Logika za postavljanje vozača
        if (request.getCurrentDriverId() != null) {
            // Dohvaćamo vozača, bacamo iznimku ako ne postoji
            UserInfo driver = userRepository.findById(request.getCurrentDriverId())
                    .orElseThrow(() -> new ResourceNotFoundException("Vozač", "ID", request.getCurrentDriverId()));
            vehicle.setCurrentDriver(driver);
        } else {
            // Ako je ID null, postavljamo vozača na null
            vehicle.setCurrentDriver(null);
        }

        return vehicleRepository.save(vehicle);
    }



    @Override
    @Transactional
    // Metoda updateVehicle sada prima VehicleRequest
    public Vehicle updateVehicle(Long id, VehicleRequest request) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vozilo", "ID", id));

        // Ažuriraj polja iz Request DTO-a
        vehicle.setLicensePlate(request.getLicensePlate());
        vehicle.setMake(request.getMake());
        vehicle.setModel(request.getModel());
        vehicle.setYear(request.getModelYear()); // Korištenje modelYear
        vehicle.setFuelType(request.getFuelType());
        vehicle.setLoadCapacityKg(request.getLoadCapacityKg());

        // Logika za ažuriranje vozača
        if (request.getCurrentDriverId() != null) {
            UserInfo driver = userRepository.findById(request.getCurrentDriverId())
                    .orElseThrow(() -> new ResourceNotFoundException("Vozač", "ID", request.getCurrentDriverId()));
            vehicle.setCurrentDriver(driver);
        } else {
            // Ako je ID null/prazan, uklanjamo vozača (odspajamo)
            vehicle.setCurrentDriver(null);
        }

        // Nema potrebe za eksplicitnim .save() zbog @Transactional, ali ga ostavljamo radi jasnoće
        return vehicleRepository.save(vehicle);
    }

    // Metoda deleteVehicle ostaje ista
    @Override
    @Transactional
    public void deleteVehicle(Long id) {
        if (!vehicleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Vozilo", "ID", id);
        }
        vehicleRepository.deleteById(id);
    }
}