package hr.algebra.rapid.logisticsandfleetmanagementsystem.service;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.Shipment;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.IssueReportDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.ProofOfDeliveryDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.ShipmentRequest;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.ShipmentResponse;

import java.util.List;
import java.util.Optional;

public interface ShipmentService {

    ShipmentResponse mapToResponse(Shipment shipment);

    List<ShipmentResponse> findAll();

    Optional<ShipmentResponse> findById(Long id);

    ShipmentResponse createShipment(ShipmentRequest request);

    ShipmentResponse updateShipment(Long id, ShipmentRequest request);

    void deleteShipment(Long id);

    // Driver Workflow metode
    ShipmentResponse startDelivery(Long shipmentId, Long driverId);

    ShipmentResponse completeDelivery(Long shipmentId, Long driverId, ProofOfDeliveryDTO pod);

    ShipmentResponse reportIssue(Long shipmentId, Long driverId, IssueReportDTO issue);
}