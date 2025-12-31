package hr.algebra.rapid.logisticsandfleetmanagementsystem.repository;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.Shipment;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.ShipmentStatus;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    Optional<Shipment> findByTrackingNumber(String trackingNumber);
    List<Shipment> findByStatus(ShipmentStatus shipmentStatus);
    List<Shipment> findByStatusAndActualDeliveryDateBefore(ShipmentStatus shipmentStatus, LocalDateTime cutoffDate);
    List<Shipment> findByRouteIsNull();



    boolean existsByTrackingNumber(@NotBlank(message = "Tracking number is required") String trackingNumber);

    List<Shipment> findByAssignmentId(Long id);
}