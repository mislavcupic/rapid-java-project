package hr.algebra.rapid.logisticsandfleetmanagementsystem.service.impl;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.*;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.AssignmentRequestDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.AssignmentResponseDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.DriverResponseDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.ShipmentResponse;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class AssignmentServiceImpl implements AssignmentService {

    public static final String PENDING = "PENDING";
    public static final String SCHEDULED = "SCHEDULED";
    public static final String IN_PROGRESS = "IN_PROGRESS";
    public static final String COMPLETED = "COMPLETED";
    public static final String ASSIGNMENT = "Assignment";

    private final AssignmentRepository assignmentRepository;
    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;
    private final ShipmentRepository shipmentRepository;
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

        // PROMJENA: Mapiramo listu pošiljaka umjesto jedne
        if (assignment.getShipments() != null) {
            List<ShipmentResponse> shipmentResponses = assignment.getShipments().stream()
                    .map(shipmentService::mapToResponse).
                    toList();
            dto.setShipments(shipmentResponses);
        }

        return dto;
    }

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
        return assignmentRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
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

        // PROMJENA: Dohvaćamo listu pošiljaka
        List<Shipment> shipments = shipmentRepository.findAllById(request.getShipmentIds());

        if (shipments.isEmpty()) {
            throw new ConflictException("No shipments found for IDs: " + request.getShipmentIds());
        }

        // Provjera jesu li neke pošiljke već dodijeljene
        for (Shipment s : shipments) {
            if (assignmentRepository.findByShipments_Id(s.getId()).isPresent()) {
                throw new ConflictException("Shipment with ID " + s.getId() + " is already assigned.");
            }
        }

        Assignment assignment = new Assignment();
        assignment.setDriver(driver);
        assignment.setVehicle(vehicle);
        assignment.setStartTime(request.getStartTime());
        assignment.setStatus(SCHEDULED);

        // Povezivanje pošiljaka
        shipments.forEach(s -> {
            s.setAssignment(assignment);
            s.setStatus(ShipmentStatus.valueOf(SCHEDULED));
        });
        assignment.setShipments(shipments);

        Assignment savedAssignment = assignmentRepository.save(assignment);
        return mapToResponse(savedAssignment);
    }

    @Override
    @Transactional
    public Optional<AssignmentResponseDTO> updateAssignment(Long id, AssignmentRequestDTO request) {
        return assignmentRepository.findById(id).map(assignment -> {
            Driver newDriver = getDriver(request.getDriverId());
            Vehicle newVehicle = getVehicle(request.getVehicleId());

            // Oslobađanje starih pošiljaka
            assignment.getShipments().forEach(s -> {
                s.setAssignment(null);
                s.setStatus(ShipmentStatus.valueOf(PENDING));
            });

            // Postavljanje novih pošiljaka
            List<Shipment> newShipments = shipmentRepository.findAllById(request.getShipmentIds());
            newShipments.forEach(s -> {
                s.setAssignment(assignment);
                s.setStatus(ShipmentStatus.valueOf(SCHEDULED));
            });

            assignment.setDriver(newDriver);
            assignment.setVehicle(newVehicle);
            assignment.setShipments(newShipments);
            assignment.setStartTime(request.getStartTime());
            assignment.setEndTime(request.getEndTime());

            return mapToResponse(assignmentRepository.save(assignment));
        });
    }

    @Override
    @Transactional
    public void deleteAssignment(Long id) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ASSIGNMENT, "ID", id));

        // Oslobodi pošiljke prije brisanja assignmenta
        assignment.getShipments().forEach(s -> {
            s.setAssignment(null);
            s.setStatus(ShipmentStatus.valueOf(PENDING));
        });

        assignmentRepository.delete(assignment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssignmentResponseDTO> findAssignmentsByDriver(Long driverId) {
        List<String> statuses = Arrays.asList(SCHEDULED, IN_PROGRESS);
        return assignmentRepository.findByDriverIdAndStatusIn(driverId, statuses).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public Optional<AssignmentResponseDTO> startAssignment(Long assignmentId, Long driverId) {
        return assignmentRepository.findById(assignmentId)
                .map(assignment -> {
                    if (!assignment.getDriver().getId().equals(driverId)) {
                        throw new ConflictException("Assignment ID " + assignmentId + " does not belong to driver ID " + driverId);
                    }

                    assignment.setStatus(IN_PROGRESS);
                    assignment.setStartTime(LocalDateTime.now());

                    // PROMJENA: Ažuriraj status svim pošiljkama u listi
                    assignment.getShipments().forEach(s ->
                        s.setStatus(ShipmentStatus.IN_TRANSIT)
                    );

                    return mapToResponse(assignmentRepository.save(assignment));
                });
    }

    @Override
    @Transactional
    public Optional<AssignmentResponseDTO> completeAssignment(Long assignmentId, Long driverId) {
        return assignmentRepository.findById(assignmentId)
                .map(assignment -> {
                    if (!assignment.getDriver().getId().equals(driverId)) {
                        throw new ConflictException("Assignment ID " + assignmentId + " does not belong to driver ID " + driverId);
                    }

                    // Provjera jesu li svi paketi DELIVERED
                    boolean allDelivered = assignment.getShipments().stream()
                            .allMatch(s -> s.getStatus().equals(ShipmentStatus.DELIVERED));

                    if (!allDelivered) {
                        throw new ConflictException("Cannot complete assignment: Not all shipments are delivered.");
                    }

                    assignment.setStatus(COMPLETED);
                    assignment.setEndTime(LocalDateTime.now());

                    return mapToResponse(assignmentRepository.save(assignment));
                });
    }
    @Transactional
    @Override
    public AssignmentResponseDTO optimizeAssignmentOrder(Long assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException(ASSIGNMENT, "ID", assignmentId));

        List<Shipment> toOptimize = new ArrayList<>(assignment.getShipments());
        if (toOptimize.isEmpty()) return mapToResponse(assignment);

        // Početna točka (npr. origin prve pošiljke)
        double currentLat = toOptimize.get(0).getRoute().getOriginLatitude().doubleValue();
        double currentLon = toOptimize.get(0).getRoute().getOriginLongitude().doubleValue();

        int sequence = 1;
        while (!toOptimize.isEmpty()) {
            Shipment closest = null;
            double minDistance = Double.MAX_VALUE;

            for (Shipment s : toOptimize) {
                double dist = calculateHaversine(currentLat, currentLon,
                        s.getRoute().getDestinationLatitude().doubleValue(),
                        s.getRoute().getDestinationLongitude().doubleValue());
                if (dist < minDistance) {
                    minDistance = dist;
                    closest = s;
                }
            }

            if (closest != null) {
                closest.setDeliverySequence(sequence++);
                toOptimize.remove(closest);
                // Sljedeća točka pretrage je odredište zadnjeg dostavljenog paketa
                currentLat = closest.getRoute().getDestinationLatitude().doubleValue();
                currentLon = closest.getRoute().getDestinationLongitude().doubleValue();
                shipmentRepository.save(closest);
            }
        }
        return mapToResponse(assignment);
    }
    private double calculateHaversine(double lat1, double lon1, double lat2, double lon2) {
        // Polumjer Zemlje u kilometrima
        final int R = 6371;

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }
}