package hr.algebra.rapid.logisticsandfleetmanagementsystem.service.impl;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.*;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.AssignmentRequestDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.AssignmentResponseDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.DriverResponseDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.exceptions.ResourceNotFoundException;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.exceptions.ConflictException;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.*;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.AssignmentService;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.ShipmentService;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.VehicleService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class AssignmentServiceImpl implements AssignmentService {

    public static final String SCHEDULED = "SCHEDULED";
    public static final String IN_PROGRESS = "IN_PROGRESS";
    public static final String COMPLETED = "COMPLETED";
    public static final String ASSIGNMENT = "Assignment";

    @PersistenceContext
    private EntityManager entityManager;

    private final AssignmentRepository assignmentRepository;
    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;
    private final ShipmentRepository shipmentRepository;
    private final RouteRepository routeRepository;
    private final VehicleService vehicleService;
    private final ShipmentService shipmentService;

    // RJEŠENJE ZA SONARQUBE: Self-injection (Lazy da izbjegnemo circular dependency)
    private AssignmentServiceImpl self;

    @Autowired
    public void setSelf(@Lazy AssignmentServiceImpl self) {
        this.self = self;
    }

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
            dto.setShipments(assignment.getShipments().stream()
                   .sorted(Comparator.comparing(Shipment::getDeliverySequence,
                           Comparator.nullsLast(Comparator.naturalOrder())))
                    .map(shipmentService::mapToResponse)
                   .toList());
        }
        return dto;
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
        // 1. Provjera vozača (ResourceNotFound)
        Driver driver = driverRepository.findById(request.getDriverId())
                .orElseThrow(() -> new ResourceNotFoundException("Driver", "ID", request.getDriverId()));

        // 2. Provjera vozila (ResourceNotFound)
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", "ID", request.getVehicleId()));

        // 3. Provjera pošiljaka (Conflict ako je prazno)
        List<Shipment> shipments = shipmentRepository.findAllById(request.getShipmentIds());
        if (shipments.isEmpty()) {
            throw new ConflictException("Nalog mora imati barem jednu pošiljku.");
        }

        // ✅ 4. KREIRAJ RUTU PRIJE ASSIGNMENTA
        Route route = new Route();
        route.setOriginAddress(shipments.get(0).getOriginAddress());
        route.setOriginLatitude(shipments.get(0).getOriginLatitude());
        route.setOriginLongitude(shipments.get(0).getOriginLongitude());
        route.setDestinationAddress(shipments.get(shipments.size() - 1).getDestinationAddress());
        route.setDestinationLatitude(shipments.get(shipments.size() - 1).getDestinationLatitude());
        route.setDestinationLongitude(shipments.get(shipments.size() - 1).getDestinationLongitude());
        route.setStatus(RouteStatus.DRAFT);
        route.setEstimatedDistanceKm(0.0);  // Će se izračunati u optimizeAssignmentOrder
        route.setEstimatedDurationMinutes(0L);

        Route savedRoute = routeRepository.save(route);

        // 5. Kreiraj Assignment sa Route-om
        Assignment assignment = new Assignment();
        assignment.setDriver(driver);
        assignment.setVehicle(vehicle);
        assignment.setStartTime(request.getStartTime());
        assignment.setStatus(SCHEDULED);
        assignment.setRoute(savedRoute);  // ✅ POSTAVI RUTU!

        Assignment saved = assignmentRepository.save(assignment);

        // 6. Poveži pošiljke sa assignmentom
        shipments.forEach(s -> {
            s.setAssignment(saved);
            s.setStatus(ShipmentStatus.SCHEDULED);
        });
        shipmentRepository.saveAll(shipments);

        // 7. Optimiziraj rutu
        return self.optimizeAssignmentOrder(saved.getId());
    }
    @Override
    @Transactional
    public Optional<AssignmentResponseDTO> updateAssignment(Long id, AssignmentRequestDTO request) {
        return assignmentRepository.findById(id).map(assignment -> {
            assignment.setDriver(driverRepository.findById(request.getDriverId()).orElseThrow());
            assignment.setVehicle(vehicleRepository.findById(request.getVehicleId()).orElseThrow());
            assignment.setStartTime(request.getStartTime());

            List<Shipment> oldShipments = shipmentRepository.findByAssignmentId(id);
            oldShipments.forEach(s -> { s.setAssignment(null); s.setDeliverySequence(null); });
            shipmentRepository.saveAll(oldShipments);

            List<Shipment> newShipments = shipmentRepository.findAllById(request.getShipmentIds());
            newShipments.forEach(s -> s.setAssignment(assignment));
            shipmentRepository.saveAll(newShipments);

            assignmentRepository.saveAndFlush(assignment);

            // SONAR FIX: Zovemo preko 'self' proxyja
            return self.optimizeAssignmentOrder(id);
        });
    }

    @Override
    @Transactional
    public void deleteAssignment(Long id) {
        // 1. Dohvati nalog (zajedno s pošiljkama)
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ASSIGNMENT, "ID", id));

        // 2. Oslobodi pošiljke
        if (assignment.getShipments() != null) {
            for (Shipment shipment : assignment.getShipments()) {
                shipment.setAssignment(null);
                shipment.setStatus(ShipmentStatus.PENDING);
                shipment.setDeliverySequence(null);
                // NEMOJ čistiti rutu na pošiljci ako je želiš zadržati
            }
            // Spremamo promjene na pošiljkama prije brisanja naloga
            shipmentRepository.saveAll(assignment.getShipments());
        }

        // 3. Očisti listu u nalogu da Hibernate ne misli da su još povezani
        assignment.getShipments().clear();

        // 4. BRISANJE
        // Koristimo direktno objekt koji smo našli, BEZ getReferenceById i BEZ entityManager.clear()
        assignmentRepository.delete(assignment);

        // Flush je opcionalan ovdje, @Transactional će ga odraditi na kraju metode
        assignmentRepository.flush();
    }

    @Override
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
    public Optional<AssignmentResponseDTO> startAssignment(Long assignmentId, Long driverId) {
        return assignmentRepository.findById(assignmentId).map(assignment -> {
            if (!assignment.getDriver().getId().equals(driverId)) throw new ConflictException("Pogrešan vozač");
            assignment.setStatus(IN_PROGRESS);
            assignment.setStartTime(LocalDateTime.now());
            if (assignment.getShipments() != null) {
                assignment.getShipments().forEach(s -> s.setStatus(ShipmentStatus.IN_TRANSIT));
                shipmentRepository.saveAll(assignment.getShipments());
            }
            return mapToResponse(assignmentRepository.saveAndFlush(assignment));
        });
    }

    @Override
    @Transactional
    public Optional<AssignmentResponseDTO> completeAssignment(Long assignmentId, Long driverId) {
        return assignmentRepository.findById(assignmentId).map(assignment -> {
            if (!assignment.getDriver().getId().equals(driverId)) throw new ConflictException("Pogrešan vozač");
            boolean allDelivered = assignment.getShipments().stream().allMatch(s -> ShipmentStatus.DELIVERED.equals(s.getStatus()));
            if (!allDelivered) throw new ConflictException("Nisu svi paketi isporučeni");
            assignment.setStatus(COMPLETED);
            assignment.setEndTime(LocalDateTime.now());
            return mapToResponse(assignmentRepository.save(assignment));
        });
    }

    @Override
    @Transactional
    public AssignmentResponseDTO optimizeAssignmentOrder(Long assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException(ASSIGNMENT, "ID", assignmentId));
        List<Shipment> shipments = shipmentRepository.findByAssignmentId(assignmentId);
        if (shipments.isEmpty()) return mapToResponse(assignment);

        double startLat = shipments.get(0).getOriginLatitude();
        double startLon = shipments.get(0).getOriginLongitude();

        List<List<Shipment>> allPerms = new ArrayList<>();
        generatePermutations(new ArrayList<>(), shipments, allPerms);

        List<Shipment> best = new ArrayList<>();
        double minD = Double.MAX_VALUE;

        for (List<Shipment> p : allPerms) {
            double d = 0;
            double curLat = startLat;
            double curLon = startLon;
            for (Shipment s : p) {
                d += calculateHaversine(curLat, curLon, s.getDestinationLatitude(), s.getDestinationLongitude());
                curLat = s.getDestinationLatitude();
                curLon = s.getDestinationLongitude();
            }
            if (d < minD) {
                minD = d;
                best = p;
            }
        }

        for (int i = 0; i < best.size(); i++) {
            best.get(i).setDeliverySequence(i + 1);
            shipmentRepository.save(best.get(i));
        }
        shipmentRepository.flush();
        entityManager.clear();
        return assignmentRepository.findById(assignmentId).map(this::mapToResponse).orElseThrow();
    }

    private void generatePermutations(List<Shipment> current, List<Shipment> remaining, List<List<Shipment>> result) {
        if (remaining.isEmpty()) {
            result.add(new ArrayList<>(current));
            return;
        }
        for (int i = 0; i < remaining.size(); i++) {
            final int idx = i;
            List<Shipment> nextPath = new ArrayList<>(current);
            nextPath.add(remaining.get(idx));
            generatePermutations(nextPath, IntStream.range(0, remaining.size())
                    .filter(j -> j != idx).mapToObj(remaining::get).toList(), result);
        }
    }

    private double calculateHaversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    private ShipmentStatus mapToShipmentStatus(String status) {
        if (status == null) return ShipmentStatus.PENDING;
        return switch (status) {
            case IN_PROGRESS -> ShipmentStatus.IN_TRANSIT;
            case COMPLETED -> ShipmentStatus.DELIVERED;
            case SCHEDULED -> ShipmentStatus.SCHEDULED;
            default -> ShipmentStatus.PENDING;
        };
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssignmentResponseDTO> findAssignmentsByDriver(Long driverId) {
        return assignmentRepository.findByDriverIdAndStatusIn(driverId, Arrays.asList(SCHEDULED, IN_PROGRESS))
                .stream().map(this::mapToResponse).toList();
    }
}