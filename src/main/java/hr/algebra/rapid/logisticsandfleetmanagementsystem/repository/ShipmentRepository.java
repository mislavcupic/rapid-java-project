package hr.algebra.rapid.logisticsandfleetmanagementsystem.repository;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    // Custom metoda: Pronađi pošiljku po broju za praćenje
    Optional<Shipment> findByTrackingNumber(String trackingNumber);
}