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
    private final RouteService routeService;

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
        logger.info("Započinjem kreiranje pošiljke: {}", request.getTrackingNumber());

        // 1. Provjera duplikata
        if (shipmentRepository.existsByTrackingNumber(request.getTrackingNumber())) {
            throw new DuplicateResourceException(SHIPMENT);
        }

        // 2. Inicijalizacija entiteta
        Shipment shipment = new Shipment();

        // 3. Prvo popunjavamo osnovna polja (težina, adrese itd.)
        updateShipmentFields(shipment, request);

        // 4. Kreiranje rute (RouteService će izračunati koordinate ako su null u requestu)
        Route route = routeService.calculateAndCreateRoute(
                request.getOriginAddress(),
                request.getOriginLatitude(),
                request.getOriginLongitude(),
                request.getDestinationAddress(),
                request.getDestinationLatitude(),
                request.getDestinationLongitude()
        );

        // 5. KLJUČNI DIO: Povezivanje i sinkronizacija
        // Čak i ako su u 'request' koordinate bile NULL, u 'route' objektu su sada popunjene.
        // Moramo ih prepisati u shipment entitet da ne ostanu prazne u tablici 'shipment'.
        shipment.setRoute(route);

        if (route != null) {
            shipment.setOriginLatitude(route.getOriginLatitude());
            shipment.setOriginLongitude(route.getOriginLongitude());
            shipment.setDestinationLatitude(route.getDestinationLatitude());
            shipment.setDestinationLongitude(route.getDestinationLongitude());
            logger.info("Koordinate sinkronizirane iz rute: {}, {}", route.getOriginLatitude(), route.getOriginLongitude());
        }

        // 6. Spremanje pošiljke sa svim popunjenim poljima
        Shipment savedShipment = shipmentRepository.save(shipment);

        return mapToResponse(savedShipment);
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

    private void updateShipmentFields(Shipment shipment, ShipmentRequest request) {
        shipment.setTrackingNumber(request.getTrackingNumber());

        // Polazište - usklađeno s tvojim bitnim promjenama
        shipment.setOriginAddress(request.getOriginAddress());
        shipment.setOriginLatitude(request.getOriginLatitude());
        shipment.setOriginLongitude(request.getOriginLongitude());

        // Odredište - usklađeno
        shipment.setDestinationAddress(request.getDestinationAddress());
        shipment.setDestinationLatitude(request.getDestinationLatitude());
        shipment.setDestinationLongitude(request.getDestinationLongitude());

        // Ostali podaci
        shipment.setWeightKg(request.getWeightKg());
        shipment.setVolumeM3(request.getVolumeM3());
        shipment.setShipmentValue(request.getShipmentValue());
        shipment.setDescription(request.getDescription());
        shipment.setExpectedDeliveryDate(request.getExpectedDeliveryDate());

        // KLJUČ: Ako request ima status, postavi ga. Ako nema, a shipment je nov, stavi PENDING.
        if (request.getStatus() != null) {
            shipment.setStatus(ShipmentStatus.valueOf(request.getStatus()));
        } else if (shipment.getStatus() == null) {
            shipment.setStatus(ShipmentStatus.PENDING);
        }
    }
}