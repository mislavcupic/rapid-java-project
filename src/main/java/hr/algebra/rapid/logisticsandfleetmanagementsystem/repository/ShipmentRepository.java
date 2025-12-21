package hr.algebra.rapid.logisticsandfleetmanagementsystem.repository;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.Shipment;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.ShipmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    // Custom metoda: Pronađi pošiljku po broju za praćenje
    Optional<Shipment> findByTrackingNumber(String trackingNumber);

    List<Shipment> findByStatus(ShipmentStatus shipmentStatus);

    List<Shipment> findByStatusAndActualDeliveryDateBefore(ShipmentStatus shipmentStatus, LocalDateTime cutoffDate);

    List<Shipment> findByRouteIsNull();
}