package hr.algebra.rapid.logisticsandfleetmanagementsystem.repository;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    // Dohvaća sve dodjele za određenog vozača
    List<Assignment> findByDriverId(Long driverId);

    // Dohvaća dodjele za vozača koje su u određenim statusima (npr. SCHEDULED, IN_PROGRESS)
    List<Assignment> findByDriverIdAndStatusIn(Long driverId, List<String> statuses);

    // Provjera postoji li već dodjela za određenu pošiljku (Shipment)
    Optional<Assignment> findByShipmentId(Long shipmentId);
}