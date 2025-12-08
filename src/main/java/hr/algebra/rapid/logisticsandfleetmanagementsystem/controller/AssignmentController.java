package hr.algebra.rapid.logisticsandfleetmanagementsystem.controller;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.AssignmentRequestDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.AssignmentResponseDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.AssignmentService;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.DriverService;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;
    private final DriverService driverService; // ✅ NOVO - za Driver Dashboard

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_DISPATCHER')")
    public ResponseEntity<List<AssignmentResponseDTO>> getAllAssignments() {
        List<AssignmentResponseDTO> assignments = assignmentService.findAll();
        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_DISPATCHER') or " +
            "@driverService.isAssignmentOwnedByDriver(#id, authentication.name)") // ✅ NOVO - Driver može vidjeti svoje
    public ResponseEntity<AssignmentResponseDTO> getAssignmentById(@PathVariable Long id) {
        AssignmentResponseDTO assignment = assignmentService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment", "ID", id));
        return ResponseEntity.ok(assignment);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_DISPATCHER')")
    public ResponseEntity<AssignmentResponseDTO> createAssignment(@Valid @RequestBody AssignmentRequestDTO request) {
        AssignmentResponseDTO newAssignment = assignmentService.createAssignment(request);
        return new ResponseEntity<>(newAssignment, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_DISPATCHER')")
    public ResponseEntity<AssignmentResponseDTO> updateAssignment(@PathVariable Long id,
                                                                  @Valid @RequestBody AssignmentRequestDTO request) {
        AssignmentResponseDTO updatedAssignment = assignmentService.updateAssignment(id, request);
        return ResponseEntity.ok(updatedAssignment);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteAssignment(@PathVariable Long id) {
        assignmentService.deleteAssignment(id);
        return ResponseEntity.noContent().build();
    }

    // ========================================================================
    // ✅ NOVI DRIVER DASHBOARD ENDPOINTI
    // ========================================================================

    /**
     * Driver Dashboard - Dohvat svojih Assignment-a (SCHEDULED, IN_PROGRESS)
     * GET /api/assignments/my-schedule
     */
    @GetMapping("/my-schedule")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_DRIVER')")
    public ResponseEntity<List<AssignmentResponseDTO>> getDriverSchedule(
            @AuthenticationPrincipal UserDetails userDetails) {

        // ✅ ISPRAVKA: Dinamički dohvat Driver ID-a (NE hardcoded 1L!)
        Long currentDriverId = driverService.getDriverIdFromUsername(userDetails.getUsername());

        List<AssignmentResponseDTO> schedule = assignmentService.findAssignmentsByDriver(currentDriverId);
        return ResponseEntity.ok(schedule);
    }

    /**
     * Driver započinje Assignment (SCHEDULED → IN_PROGRESS)
     * PUT /api/assignments/{id}/start
     */
    @PutMapping("/{id}/start")
    @PreAuthorize("hasAuthority('ROLE_DRIVER') and " +
            "@driverService.isAssignmentOwnedByDriver(#id, authentication.name)")
    public ResponseEntity<AssignmentResponseDTO> startAssignment(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long driverId = driverService.getDriverIdFromUsername(userDetails.getUsername());
        AssignmentResponseDTO updatedAssignment = assignmentService.startAssignment(id, driverId);

        return ResponseEntity.ok(updatedAssignment);
    }

    /**
     * Driver završava Assignment (IN_PROGRESS → COMPLETED)
     * PUT /api/assignments/{id}/complete
     */
    @PutMapping("/{id}/complete")
    @PreAuthorize("hasAuthority('ROLE_DRIVER') and " +
            "@driverService.isAssignmentOwnedByDriver(#id, authentication.name)")
    public ResponseEntity<AssignmentResponseDTO> completeAssignment(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long driverId = driverService.getDriverIdFromUsername(userDetails.getUsername());
        AssignmentResponseDTO completedAssignment = assignmentService.completeAssignment(id, driverId);

        return ResponseEntity.ok(completedAssignment);
    }
}