package hr.algebra.rapid.logisticsandfleetmanagementsystem.service;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.Assignment;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.ShipmentStatus;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.AssignmentRequestDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.AssignmentResponseDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface AssignmentService {

    AssignmentResponseDTO mapToResponse(Assignment assignment);

    List<AssignmentResponseDTO> findAll();

    Optional<AssignmentResponseDTO> findById(Long id);

    // Prima DTO koji sada sadrži List<Long> shipmentIds
    AssignmentResponseDTO createAssignment(AssignmentRequestDTO request);

    // Ažurira dodjelu (omogućuje dodavanje/izbacivanje pošiljaka iz kamiona)
    Optional<AssignmentResponseDTO> updateAssignment(Long id, AssignmentRequestDTO request);

    void deleteAssignment(Long id);

    List<AssignmentResponseDTO> findAssignmentsByDriver(Long driverId);

    /**
     * Driver započinje Assignment (SCHEDULED → IN_PROGRESS)
     * Svi Shipmenti u listi postaju IN_TRANSIT
     */
    Optional<AssignmentResponseDTO> startAssignment(Long assignmentId, Long driverId);

    /**
     * Driver završava Assignment (IN_PROGRESS → COMPLETED)
     * Provjerava jesu li SVI Shipmenti u kolekciji DELIVERED
     */
    Optional<AssignmentResponseDTO> completeAssignment(Long assignmentId, Long driverId);

    @Transactional
    AssignmentResponseDTO optimizeAssignmentOrder(Long assignmentId);
    Optional<AssignmentResponseDTO> updateStatus(Long assignmentId, String newStatus);

    @Transactional
    void updateShipmentStatus(Long shipmentId, ShipmentStatus newStatus);
}