package hr.algebra.rapid.logisticsandfleetmanagementsystem.service.impl;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.Assignment;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.Driver;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.Vehicle;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.Shipment;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.AssignmentRequestDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.AssignmentResponseDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.DriverResponseDTO;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.exceptions.ResourceNotFoundException;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.exceptions.ConflictException;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.AssignmentRepository;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.DriverRepository;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.VehicleRepository;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.ShipmentRepository;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.AssignmentService;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.ShipmentService;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.VehicleService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class AssignmentServiceImpl implements AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;
    private final ShipmentRepository shipmentRepository;
    private final VehicleService vehicleService;
    private final ShipmentService shipmentService;

    // --- Metoda mapiranja (Entity -> Response DTO) ---

    @Override
    public AssignmentResponseDTO mapToResponse(Assignment assignment) {
        AssignmentResponseDTO dto = new AssignmentResponseDTO();
        dto.setId(assignment.getId());
        dto.setStartTime(assignment.getStartTime());
        dto.setEndTime(assignment.getEndTime());
        // Korekcija: Koristite samo getStatus() koji Lombok generira za polje 'status'
        dto.setAssignmentStatus(assignment.getStatus());

        // Mapiranje ugniježđenih objekata
        if (assignment.getDriver() != null) {
            dto.setDriver(DriverResponseDTO.fromDriver(assignment.getDriver()));
        }
        if (assignment.getVehicle() != null) {
            dto.setVehicle(vehicleService.mapToResponse(assignment.getVehicle()));
        }
        if (assignment.getShipment() != null) {
            dto.setShipment(shipmentService.mapToResponse(assignment.getShipment()));
        }

        return dto;
    }

    // --- Pomoćne metode (Dry Principle) ---
    private Driver getDriver(Long driverId) {
        return driverRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver", "ID", driverId));
    }

    private Vehicle getVehicle(Long vehicleId) {
        return vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", "ID", vehicleId));
    }

    private Shipment getShipment(Long shipmentId) {
        return shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment", "ID", shipmentId));
    }

    // --- CRUD Implementacija ---

    @Override
    public List<AssignmentResponseDTO> findAll() {
        return assignmentRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<AssignmentResponseDTO> findById(Long id) {
        return assignmentRepository.findById(id)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional
    public AssignmentResponseDTO createAssignment(AssignmentRequestDTO request) {
        // 1. Dohvaćanje i provjera postojanja svih entiteta (FK provjera)
        Driver driver = getDriver(request.getDriverId());
        Vehicle vehicle = getVehicle(request.getVehicleId());
        Shipment shipment = getShipment(request.getShipmentId());

        // 2. Poslovna pravila - Provjera da pošiljka već nije dodijeljena
        if (assignmentRepository.findByShipmentId(shipment.getId()).isPresent()) {
            throw new ConflictException("Shipment with ID " + shipment.getId() + " is already assigned.");
        }

        // 3. Poslovna pravila - Provjera statusa pošiljke
        if (!shipment.getStatus().equals("PENDING")) {
            throw new ConflictException("Shipment status is " + shipment.getStatus() + ". Only PENDING shipments can be assigned.");
        }

        // 4. Mapiranje i postavljanje defaultnog statusa
        Assignment assignment = new Assignment();
        assignment.setDriver(driver);
        assignment.setVehicle(vehicle);
        assignment.setShipment(shipment);
        assignment.setStartTime(request.getStartTime());
        assignment.setEndTime(request.getEndTime());
        // KRITIČNA KOREKCIJA: Status se postavlja kao String literal, ne Enum
        assignment.setStatus("SCHEDULED");

        // 5. Ažuriranje statusa pošiljke
        shipment.setStatus("SCHEDULED");
        shipmentRepository.save(shipment);

        // 6. Spremanje
        Assignment savedAssignment = assignmentRepository.save(assignment);
        return mapToResponse(savedAssignment);
    }

    @Override
    @Transactional
    public AssignmentResponseDTO updateAssignment(Long id, AssignmentRequestDTO request) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment", "ID", id));

        // Dohvaćanje novih entiteta (ako se ID-evi mijenjaju)
        Driver newDriver = getDriver(request.getDriverId());
        Vehicle newVehicle = getVehicle(request.getVehicleId());
        Shipment newShipment = getShipment(request.getShipmentId());

        // 1. Provjera sukoba (ako je shipmentId promijenjen, provjeriti je li novi ID slobodan)
        if (!assignment.getShipment().getId().equals(newShipment.getId())) {
            if (assignmentRepository.findByShipmentId(newShipment.getId()).isPresent()) {
                throw new ConflictException("New Shipment ID " + newShipment.getId() + " is already assigned to another assignment.");
            }
            // Vraćanje starog statusa pošiljke
            assignment.getShipment().setStatus("PENDING");
            shipmentRepository.save(assignment.getShipment());

            newShipment.setStatus("SCHEDULED");
            shipmentRepository.save(newShipment);
        }

        // 2. Ažuriranje
        assignment.setDriver(newDriver);
        assignment.setVehicle(newVehicle);
        assignment.setShipment(newShipment);
        assignment.setStartTime(request.getStartTime());
        assignment.setEndTime(request.getEndTime());
        // KRITIČNA KOREKCIJA: Status se postavlja kao String literal, ne Enum
        assignment.setStatus("SCHEDULED");

        return mapToResponse(assignmentRepository.save(assignment));
    }

    @Override
    @Transactional
    public void deleteAssignment(Long id) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment", "ID", id));

        // Vraćanje statusa pošiljke na PENDING prije brisanja dodjele
        Shipment shipment = assignment.getShipment();
        shipment.setStatus("PENDING");
        shipmentRepository.save(shipment);

        assignmentRepository.delete(assignment);
    }

    // --- Dodatna metoda za Dashboard Vozača ---
    @Override
    public List<AssignmentResponseDTO> findAssignmentsByDriver(Long driverId) {
        Driver driver = getDriver(driverId);

        // Dohvaćanje dodjela s statusima koje vozač treba vidjeti
        List<String> statuses = Arrays.asList("SCHEDULED", "IN_PROGRESS");

        // KRITIČNA KOREKCIJA: Pozivanje ispravne metode repozitorija
        return assignmentRepository.findByDriverIdAndStatusIn(driver.getId(), statuses).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
}