package hr.algebra.rapid.logisticsandfleetmanagementsystem.service.impl;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.*;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.*;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.exceptions.ResourceNotFoundException;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.exceptions.DuplicateResourceException;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.AssignmentRepository;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.ShipmentRepository;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.RouteService;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.ShipmentService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShipmentServiceImpl implements ShipmentService {

    private static final Logger logger = LoggerFactory.getLogger(ShipmentServiceImpl.class);
    public static final String SHIPMENT = "Shipment";
    public static final String NOT_ASSIGNED_TO_ANY_TRIP = "Shipment is not assigned to any trip.";

    private final ShipmentRepository shipmentRepository;
    private final AssignmentRepository assignmentRepository;
    private final RouteService routeService; // Ubrizgan tvoj RouteService

    @Override
    @Transactional(readOnly = true)
    public List<ShipmentResponse> findAll() {
        return shipmentRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.Optional<ShipmentResponse> findById(Long id) {
        return shipmentRepository.findById(id).map(this::mapToResponse);
    }

    @Override
    @Transactional
    public ShipmentResponse createShipment(ShipmentRequest request) {
        if (shipmentRepository.existsByTrackingNumber(request.getTrackingNumber())) {
            throw new DuplicateResourceException(SHIPMENT, "Tracking Number", request.getTrackingNumber());
        }

        Shipment shipment = new Shipment();
        updateShipmentFields(shipment, request);

        // KREIRANJE RUTE: Pozivamo tvoj RouteServiceImpl da napravi izračun i objekt
        Route newRoute = routeService.calculateAndCreateRoute(
                request.getOriginAddress(), request.getOriginLatitude(), request.getOriginLongitude(),
                request.getDestinationAddress(), request.getDestinationLatitude(), request.getDestinationLongitude()
        );
        shipment.setRoute(newRoute);

        Shipment saved = shipmentRepository.save(shipment);
        logger.info("Created shipment {} with route ID {}", saved.getTrackingNumber(), saved.getRoute().getId());
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public ShipmentResponse updateShipment(Long id, ShipmentRequest request) {
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(SHIPMENT, "ID", id));

        updateShipmentFields(shipment, request);

        // AŽURIRANJE RUTE: Kreiramo novu ili osvježavamo postojeću
        Route updatedRoute = routeService.calculateAndCreateRoute(
                request.getOriginAddress(), request.getOriginLatitude(), request.getOriginLongitude(),
                request.getDestinationAddress(), request.getDestinationLatitude(), request.getDestinationLongitude()
        );

        // Ako je pošiljka već imala rutu, moramo zadržati isti ID rute u bazi da ne gomilamo redove
        if (shipment.getRoute() != null) {
            updatedRoute.setId(shipment.getRoute().getId());
        }
        shipment.setRoute(updatedRoute);

        Shipment saved = shipmentRepository.save(shipment);
        logger.info("Updated shipment {} with coordinates [{}, {}]",
                saved.getTrackingNumber(), updatedRoute.getOriginLatitude(), updatedRoute.getOriginLongitude());

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public void deleteShipment(Long id) {
        if (!shipmentRepository.existsById(id)) {
            throw new ResourceNotFoundException(SHIPMENT, "ID", id);
        }
        shipmentRepository.deleteById(id);
    }

    @Override
    @Transactional
    public ShipmentResponse startDelivery(Long shipmentId, Long driverId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ResourceNotFoundException(SHIPMENT, "ID", shipmentId));

        shipment.setStatus(ShipmentStatus.IN_TRANSIT);
        return mapToResponse(shipmentRepository.save(shipment));
    }

    @Override
    @Transactional
    public ShipmentResponse completeDelivery(Long shipmentId, Long driverId, ProofOfDeliveryDTO pod) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ResourceNotFoundException(SHIPMENT, "ID", shipmentId));

        shipment.setStatus(ShipmentStatus.DELIVERED);
        shipment.setActualDeliveryDate(LocalDateTime.now());
        return mapToResponse(shipmentRepository.save(shipment));
    }

    @Override
    @Transactional
    public ShipmentResponse reportIssue(Long shipmentId, Long driverId, IssueReportDTO issue) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ResourceNotFoundException(SHIPMENT, "ID", shipmentId));

        shipment.setStatus(ShipmentStatus.DELAYED);
        return mapToResponse(shipmentRepository.save(shipment));
    }

    // MAPIRANJE IZ ENTITETA U RESPONSE (Za Frontend mapu)
    @Override
    public ShipmentResponse mapToResponse(Shipment shipment) {
        ShipmentResponse response = new ShipmentResponse();
        response.setId(shipment.getId());
        response.setTrackingNumber(shipment.getTrackingNumber());
        response.setDescription(shipment.getDescription());
        response.setOriginAddress(shipment.getOriginAddress());
        response.setDestinationAddress(shipment.getDestinationAddress());
        response.setWeightKg(shipment.getWeightKg());
        response.setVolumeM3(shipment.getVolumeM3());
        response.setShipmentValue(shipment.getShipmentValue());
        response.setStatus(shipment.getStatus());
        response.setExpectedDeliveryDate(shipment.getExpectedDeliveryDate());
        response.setActualDeliveryDate(shipment.getActualDeliveryDate());

        // Ovdje izvlačimo koordinate iz Route objekta natrag u ShipmentResponse
        if (shipment.getRoute() != null) {
            response.setRouteId(shipment.getRoute().getId());
            response.setOriginLatitude(shipment.getRoute().getOriginLatitude());
            response.setOriginLongitude(shipment.getRoute().getOriginLongitude());
            response.setDestinationLatitude(shipment.getRoute().getDestinationLatitude());
            response.setDestinationLongitude(shipment.getRoute().getDestinationLongitude());
            response.setEstimatedDistanceKm(shipment.getRoute().getEstimatedDistanceKm());
            response.setEstimatedDurationMinutes(shipment.getRoute().getEstimatedDurationMinutes());
            response.setRouteStatus(shipment.getRoute().getStatus().name());
        }

        return response;
    }

    // Pomoćna metoda za ažuriranje polja iz requesta
    private void updateShipmentFields(Shipment shipment, ShipmentRequest request) {
        shipment.setTrackingNumber(request.getTrackingNumber());
        shipment.setOriginAddress(request.getOriginAddress());
        shipment.setDestinationAddress(request.getDestinationAddress());
        shipment.setWeightKg(request.getWeightKg());
        shipment.setVolumeM3(request.getVolumeM3());
        shipment.setShipmentValue(request.getShipmentValue());
        shipment.setDescription(request.getDescription());
        shipment.setExpectedDeliveryDate(request.getExpectedDeliveryDate());

        if (shipment.getStatus() == null) {
            shipment.setStatus(ShipmentStatus.PENDING);
        }
    }
}