package hr.algebra.rapid.logisticsandfleetmanagementsystem.service.impl;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.Shipment;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.ShipmentRequest;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.ShipmentResponse;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.exceptions.ResourceNotFoundException;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.exceptions.DuplicateResourceException; // Potrebno za provjeru jedinstvenosti
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.ShipmentRepository;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.ShipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShipmentServiceImpl implements ShipmentService {

    private final ShipmentRepository shipmentRepository;

    // --- Metoda mapiranja (Entity -> Response DTO) ---

    @Override
    public ShipmentResponse mapToResponse(Shipment shipment) {
        ShipmentResponse dto = new ShipmentResponse();
        dto.setId(shipment.getId());
        dto.setTrackingNumber(shipment.getTrackingNumber());
        dto.setDescription(shipment.getDescription());
        dto.setWeightKg(shipment.getWeightKg());
        dto.setVolumeM3(shipment.getVolumeM3());
        dto.setOriginAddress(shipment.getOriginAddress());
        dto.setDestinationAddress(shipment.getDestinationAddress());
        dto.setStatus(shipment.getStatus());
        dto.setExpectedDeliveryDate(shipment.getExpectedDeliveryDate());
        dto.setActualDeliveryDate(shipment.getActualDeliveryDate());
        return dto;
    }

    // --- CRUD Implementacija ---

    @Override
    public List<ShipmentResponse> findAll() {
        return shipmentRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ShipmentResponse> findById(Long id) {
        return shipmentRepository.findById(id)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional
    public ShipmentResponse createShipment(ShipmentRequest request) {
        // 1. Provjera jedinstvenosti (ključna poslovna logika)
        if (shipmentRepository.findByTrackingNumber(request.getTrackingNumber()).isPresent()) {
            throw new DuplicateResourceException("Shipment with tracking number " + request.getTrackingNumber() + " already exists.");
        }

        // 2. Mapiranje Request DTO-a u entitet
        Shipment shipment = new Shipment();
        shipment.setTrackingNumber(request.getTrackingNumber());
        shipment.setDescription(request.getDescription());
        shipment.setWeightKg(request.getWeightKg());
        shipment.setVolumeM3(request.getVolumeM3());
        shipment.setOriginAddress(request.getOriginAddress());
        shipment.setDestinationAddress(request.getDestinationAddress());
        shipment.setExpectedDeliveryDate(request.getExpectedDeliveryDate());

        // 3. Postavljanje defaultnog statusa (poslovno pravilo)
        shipment.setStatus("PENDING");

        // 4. Spremanje i mapiranje natrag u Response DTO
        Shipment savedShipment = shipmentRepository.save(shipment);
        return mapToResponse(savedShipment);
    }

    @Override
    @Transactional
    public ShipmentResponse updateShipment(Long id, ShipmentRequest request) {
        // 1. Pronađi pošiljku
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment", "ID", id));

        // 2. Provjera jedinstvenosti za update (ako je trackingNumber promijenjen)
        Optional<Shipment> existingShipment = shipmentRepository.findByTrackingNumber(request.getTrackingNumber());
        if (existingShipment.isPresent() && !existingShipment.get().getId().equals(id)) {
            throw new DuplicateResourceException("Shipment with tracking number " + request.getTrackingNumber() + " already exists.");
        }

        // 3. Ažuriranje polja
        shipment.setTrackingNumber(request.getTrackingNumber());
        shipment.setDescription(request.getDescription());
        shipment.setWeightKg(request.getWeightKg());
        shipment.setVolumeM3(request.getVolumeM3());
        shipment.setOriginAddress(request.getOriginAddress());
        shipment.setDestinationAddress(request.getDestinationAddress());
        shipment.setExpectedDeliveryDate(request.getExpectedDeliveryDate());

        // Napomena: Status i actualDeliveryDate se obično ažuriraju kroz zasebne metode (npr. updateStatus)

        // 4. Spremanje i povrat
        return mapToResponse(shipmentRepository.save(shipment));
    }

    @Override
    @Transactional
    public void deleteShipment(Long id) {
        if (!shipmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Shipment", "ID", id);
        }
        shipmentRepository.deleteById(id);
    }
}