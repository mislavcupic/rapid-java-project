package hr.algebra.rapid.logisticsandfleetmanagementsystem.repository;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    // Upiti specifični za vozača (za Dashboard)
    List<Assignment> findByDriverId(Long driverId);

    // KRITIČNA KOREKCIJA: Preimenovano iz 'AssignmentStatusIn' u 'StatusIn'
    List<Assignment> findByDriverIdAndStatusIn(Long driverId, List<String> statuses);

    // Dohvati dodjelu po ID-u pošiljke
    Optional<Assignment> findByShipmentId(Long shipmentId);
}