package hr.algebra.rapid.logisticsandfleetmanagementsystem.service;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.Assignment;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.AssignmentRequestDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.AssignmentResponseDTO;
import java.util.List;
import java.util.Optional;

public interface AssignmentService {

    AssignmentResponseDTO mapToResponse(Assignment assignment);

    List<AssignmentResponseDTO> findAll();

    Optional<AssignmentResponseDTO> findById(Long id);

    AssignmentResponseDTO createAssignment(AssignmentRequestDTO request);

    AssignmentResponseDTO updateAssignment(Long id, AssignmentRequestDTO request);

    void deleteAssignment(Long id);

    // Dodatna metoda za vozača
    List<AssignmentResponseDTO> findAssignmentsByDriver(Long driverId);
}
