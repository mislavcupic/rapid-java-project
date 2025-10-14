package hr.algebra.rapid.logisticsandfleetmanagementsystem.service;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.Shipment;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.ShipmentRequest;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.ShipmentResponse;
import java.util.List;
import java.util.Optional;

public interface ShipmentService {

    List<ShipmentResponse> findAll();

    Optional<ShipmentResponse> findById(Long id);

    ShipmentResponse createShipment(ShipmentRequest request);

    ShipmentResponse updateShipment(Long id, ShipmentRequest request);

    void deleteShipment(Long id);

    // Metoda mapiranja
    ShipmentResponse mapToResponse(Shipment shipment);
}