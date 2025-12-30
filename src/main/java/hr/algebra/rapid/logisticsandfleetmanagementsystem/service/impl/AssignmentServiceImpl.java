package hr.algebra.rapid.logisticsandfleetmanagementsystem.service.impl;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.*;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.AssignmentRequestDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.AssignmentResponseDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.DriverResponseDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.ShipmentResponse;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.exceptions.ResourceNotFoundException;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.exceptions.ConflictException;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.*;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.AssignmentService;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.ShipmentService;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.VehicleService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class AssignmentServiceImpl implements AssignmentService {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AssignmentServiceImpl.class);
    // Tvoji Assignment statusi (String u bazi)
    public static final String PENDING = "PENDING";
    public static final String SCHEDULED = "SCHEDULED";
    public static final String IN_PROGRESS = "IN_PROGRESS";
    public static final String COMPLETED = "COMPLETED";
    public static final String ASSIGNMENT = "Assignment";

    private final AssignmentRepository assignmentRepository;
    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;
    private final ShipmentRepository shipmentRepository;
    private final RouteRepository routeRepository;
    private final VehicleService vehicleService;
    private final ShipmentService shipmentService;

    @Override
    public AssignmentResponseDTO mapToResponse(Assignment assignment) {
        AssignmentResponseDTO dto = new AssignmentResponseDTO();
        dto.setId(assignment.getId());
        dto.setStartTime(assignment.getStartTime());
        dto.setEndTime(assignment.getEndTime());
        dto.setAssignmentStatus(assignment.getStatus());

        if (assignment.getDriver() != null) {
            dto.setDriver(DriverResponseDTO.fromDriver(assignment.getDriver()));
        }
        if (assignment.getVehicle() != null) {
            dto.setVehicle(vehicleService.mapToResponse(assignment.getVehicle()));
        }

        if (assignment.getShipments() != null) {
            List<ShipmentResponse> shipmentResponses = assignment.getShipments().stream()
                    .map(shipmentService::mapToResponse)
                    .toList();
            dto.setShipments(shipmentResponses);
        }

        return dto;
    }

    // --- POMOĆNE METODE ZA DOHVAT ---
    private Driver getDriver(Long driverId) {
        return driverRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver", "ID", driverId));
    }

    private Vehicle getVehicle(Long vehicleId) {
        return vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", "ID", vehicleId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssignmentResponseDTO> findAll() {
        return assignmentRepository.findAll().stream().map(this::mapToResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AssignmentResponseDTO> findById(Long id) {
        return assignmentRepository.findById(id).map(this::mapToResponse);
    }

    @Override
    @Transactional
    public AssignmentResponseDTO createAssignment(AssignmentRequestDTO request) {
        Driver driver = getDriver(request.getDriverId());
        Vehicle vehicle = getVehicle(request.getVehicleId());
        List<Shipment> shipments = shipmentRepository.findAllById(request.getShipmentIds());

        if (shipments.isEmpty()) throw new ConflictException("Nema odabranih pošiljaka.");

        Route route = new Route();
        route.setEstimatedDistanceKm(0.0);
        route.setEstimatedDurationMinutes(0L);
        route.setStatus(RouteStatus.DRAFT);
        Route savedRoute = routeRepository.save(route);

        Assignment assignment = new Assignment();
        assignment.setDriver(driver);
        assignment.setVehicle(vehicle);
        assignment.setStartTime(request.getStartTime());
        assignment.setStatus(SCHEDULED);
        assignment.setRoute(savedRoute);

        Assignment savedAssignment = assignmentRepository.save(assignment);

        // Odmah korigiraj statuse pošiljaka
        shipments.forEach(s -> {
            s.setAssignment(savedAssignment);
            s.setStatus(ShipmentStatus.SCHEDULED);
        });
        shipmentRepository.saveAll(shipments);

        return mapToResponse(savedAssignment);
    }

    @Override
    @Transactional
    public Optional<AssignmentResponseDTO> updateAssignment(Long id, AssignmentRequestDTO request) {
        return assignmentRepository.findById(id).map(assignment -> {
            Driver newDriver = getDriver(request.getDriverId());
            Vehicle newVehicle = getVehicle(request.getVehicleId());

            // 1. Makni stare pošiljke (vrati ih na PENDING)
            List<Shipment> currentShipments = shipmentRepository.findByAssignmentId(id);
            currentShipments.forEach(s -> {
                s.setAssignment(null);
                s.setStatus(ShipmentStatus.PENDING);
            });
            shipmentRepository.saveAll(currentShipments);

            // 2. Dodaj nove pošiljke i sinkroniziraj s trenutnim statusom naloga
            List<Shipment> newShipments = shipmentRepository.findAllById(request.getShipmentIds());
            ShipmentStatus targetStatus = mapToShipmentStatus(assignment.getStatus());

            newShipments.forEach(s -> {
                s.setAssignment(assignment);
                s.setStatus(targetStatus);
            });
            shipmentRepository.saveAll(newShipments);

            assignment.setDriver(newDriver);
            assignment.setVehicle(newVehicle);
            assignment.setShipments(newShipments);
            assignment.setStartTime(request.getStartTime());
            assignment.setEndTime(request.getEndTime());

            return mapToResponse(assignmentRepository.save(assignment));
        });
    }

    /**
     * ✅ KLJUČNA METODA ZA DISPEČERA:
     * Omogućuje promjenu statusa jedne pošiljke direktno s frontenda.
     */
    @Transactional
    public void updateShipmentStatus(Long shipmentId, ShipmentStatus newStatus) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment", "ID", shipmentId));
        shipment.setStatus(newStatus);
        shipmentRepository.save(shipment);
    }

    @Override
    @Transactional
    public Optional<AssignmentResponseDTO> updateStatus(Long assignmentId, String newStatus) {
        return assignmentRepository.findById(assignmentId).map(assignment -> {
            assignment.setStatus(newStatus);
            ShipmentStatus targetEnum = mapToShipmentStatus(newStatus);

            if (assignment.getShipments() != null) {
                assignment.getShipments().forEach(s -> s.setStatus(targetEnum));
                shipmentRepository.saveAll(assignment.getShipments());
            }

            return mapToResponse(assignmentRepository.save(assignment));
        });
    }

    @Override
    @Transactional
    public void deleteAssignment(Long id) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ASSIGNMENT, "ID", id));

        assignment.getShipments().forEach(s -> {
            s.setAssignment(null);
            s.setStatus(ShipmentStatus.PENDING);
        });
        shipmentRepository.saveAll(assignment.getShipments());

        assignmentRepository.delete(assignment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssignmentResponseDTO> findAssignmentsByDriver(Long driverId) {
        return assignmentRepository.findByDriverIdAndStatusIn(driverId, Arrays.asList(SCHEDULED, IN_PROGRESS))
                .stream().map(this::mapToResponse).toList();
    }

    @Override
    @Transactional
    public Optional<AssignmentResponseDTO> startAssignment(Long assignmentId, Long driverId) {
        return assignmentRepository.findById(assignmentId).map(assignment -> {
            // Provjera vlasništva
            if (!assignment.getDriver().getId().equals(driverId)) {
                throw new ConflictException("Nalog ne pripada ovom vozaču.");
            }

            // Postavljanje statusa naloga
            assignment.setStatus(IN_PROGRESS);
            assignment.setStartTime(LocalDateTime.now());

            // Ažuriranje svih pošiljaka u nalogu na IN_TRANSIT
            if (assignment.getShipments() != null) {
                assignment.getShipments().forEach(s ->
                    s.setStatus(ShipmentStatus.IN_TRANSIT)
                );
                // Važno: spremamo pošiljke
                shipmentRepository.saveAll(assignment.getShipments());
            }

            // KLJUČNO: saveAndFlush forsira bazu da odmah prihvati promjene
            Assignment saved = assignmentRepository.saveAndFlush(assignment);
            return mapToResponse(saved);
        });
    }

    @Override
    @Transactional
    public Optional<AssignmentResponseDTO> completeAssignment(Long assignmentId, Long driverId) {
        return assignmentRepository.findById(assignmentId).map(assignment -> {
            // 1. Provjera vozača
            if (!assignment.getDriver().getId().equals(driverId)) {
                throw new ConflictException("Nalog ne pripada ovom vozaču.");
            }

            // 2. Provjera je li ruta u tijeku (koristeći tvoje String varijable)
            // Ako koristiš konstante, stavi npr. Assignment.IN_PROGRESS
            if (!IN_PROGRESS.equals(assignment.getStatus())) {
                throw new ConflictException("Ruta se ne može završiti jer nije u tijeku. Trenutni status: " + assignment.getStatus());
            }

            // 3. KLJUČNA PROVJERA: Svi paketi moraju biti DELIVERED
            // Koristimo .toString() ili direktnu usporedbu ako je ShipmentStatus enum
            boolean allShipmentsDelivered = assignment.getShipments().stream()
                    .allMatch(s -> "DELIVERED".equals(s.getStatus().toString()));

            if (!allShipmentsDelivered) {
                throw new ConflictException("Ne možete završiti rutu jer svi paketi nisu isporučeni!");
            }

            // 4. Finalizacija rute
            assignment.setStatus(COMPLETED);
            assignment.setEndTime(LocalDateTime.now());

            // 5. Spremanje (shipmentRepository.saveAll ovdje više nije nužan jer su već Delivered)
            logger.info("Ruta {} uspješno završena. Svi paketi su isporučeni.", assignmentId);

            return mapToResponse(assignmentRepository.save(assignment));
        });
    }

    @Override
    @Transactional
    public AssignmentResponseDTO optimizeAssignmentOrder(Long assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException(ASSIGNMENT, "ID", assignmentId));

        List<Shipment> toOptimize = new ArrayList<>(assignment.getShipments());
        if (toOptimize.isEmpty()) return mapToResponse(assignment);

        double currentLat = toOptimize.get(0).getOriginLatitude();
        double currentLon = toOptimize.get(0).getOriginLongitude();

        int sequence = 1;
        while (!toOptimize.isEmpty()) {
            Shipment closest = null;
            double minDistance = Double.MAX_VALUE;
            for (Shipment s : toOptimize) {
                double dist = calculateHaversine(currentLat, currentLon, s.getDestinationLatitude(), s.getDestinationLongitude());
                if (dist < minDistance) {
                    minDistance = dist;
                    closest = s;
                }
            }
            if (closest != null) {
                closest.setDeliverySequence(sequence++);
                toOptimize.remove(closest);
                currentLat = closest.getDestinationLatitude();
                currentLon = closest.getDestinationLongitude();
                shipmentRepository.save(closest);
            } else break;
        }
        return mapToResponse(assignment);
    }

    private double calculateHaversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    private ShipmentStatus mapToShipmentStatus(String assignmentStatus) {
        if (assignmentStatus == null) return ShipmentStatus.PENDING;
        return switch (assignmentStatus) {
            case IN_PROGRESS -> ShipmentStatus.IN_TRANSIT;
            case COMPLETED -> ShipmentStatus.DELIVERED;
            case SCHEDULED -> ShipmentStatus.SCHEDULED;
            default -> ShipmentStatus.PENDING;
        };
    }

}