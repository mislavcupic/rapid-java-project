//package hr.algebra.rapid.logisticsandfleetmanagementsystem.service;
//
//import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.Assignment;
//import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.AssignmentRequestDTO;
package hr.algebra.rapid.logisticsandfleetmanagementsystem.service;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.Assignment;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.AssignmentRequestDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.AssignmentResponseDTO;
import java.util.List;
import java.util.Optional;

public interface AssignmentService {

    // ========================================================================
    // POSTOJEĆE METODE (iz tvog originalnog koda)
    // ========================================================================

    AssignmentResponseDTO mapToResponse(Assignment assignment);

    List<AssignmentResponseDTO> findAll();

    Optional<AssignmentResponseDTO> findById(Long id);

    AssignmentResponseDTO createAssignment(AssignmentRequestDTO request);

    AssignmentResponseDTO updateAssignment(Long id, AssignmentRequestDTO request);

    void deleteAssignment(Long id);

    List<AssignmentResponseDTO> findAssignmentsByDriver(Long driverId);

    // ========================================================================
    // NOVE METODE za Driver Workflow
    // ========================================================================

    /**
     * Driver započinje Assignment (SCHEDULED → IN_PROGRESS)
     * @param assignmentId ID Assignment-a
     * @param driverId ID vozača
     * @return Ažurirani Assignment
     */
    AssignmentResponseDTO startAssignment(Long assignmentId, Long driverId);

    /**
     * Driver završava Assignment (IN_PROGRESS → COMPLETED)
     * Automatski provjerava jesu li svi Shipment-i DELIVERED
     * @param assignmentId ID Assignment-a
     * @param driverId ID vozača
     * @return Ažurirani Assignment
     */
    AssignmentResponseDTO completeAssignment(Long assignmentId, Long driverId);
}