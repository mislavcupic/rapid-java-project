package hr.algebra.rapid.logisticsandfleetmanagementsystem.controller;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.AssignmentRequestDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.AssignmentResponseDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.AssignmentService;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.DriverService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;
    private final DriverService driverService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_DISPATCHER')")
    public ResponseEntity<List<AssignmentResponseDTO>> getAllAssignments() {
        List<AssignmentResponseDTO> assignments = assignmentService.findAll();
        return ResponseEntity.status(HttpStatus.OK).body(assignments);
    }

    @GetMapping("/my-schedule")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_DRIVER')")
    public ResponseEntity<List<AssignmentResponseDTO>> getDriverSchedule(@AuthenticationPrincipal UserDetails userDetails) {
        Long currentDriverId = driverService.getDriverIdFromUsername(userDetails.getUsername());
        List<AssignmentResponseDTO> schedule = assignmentService.findAssignmentsByDriver(currentDriverId);
        return ResponseEntity.status(HttpStatus.OK).body(schedule);
    }
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_DISPATCHER') or @driverService.isAssignmentOwnedByDriver(#id, authentication.name)")
    public ResponseEntity<AssignmentResponseDTO> getAssignmentById(@PathVariable Long id) {
        Optional<AssignmentResponseDTO> response = assignmentService.findById(id);
        if (response.isPresent()) {
            return ResponseEntity.status(HttpStatus.OK).body(response.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_DISPATCHER')")
    public ResponseEntity<AssignmentResponseDTO> createAssignment(@Valid @RequestBody AssignmentRequestDTO request) {
        AssignmentResponseDTO created = assignmentService.createAssignment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_DISPATCHER')")
    public ResponseEntity<AssignmentResponseDTO> updateAssignment(@PathVariable Long id, @Valid @RequestBody AssignmentRequestDTO request) {
        Optional<AssignmentResponseDTO> updated = assignmentService.updateAssignment(id, request);
        if (updated.isPresent()) {
            return ResponseEntity.status(HttpStatus.OK).body(updated.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteAssignment(@PathVariable Long id) {
        assignmentService.deleteAssignment(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }



    @PutMapping("/{id}/start")
    @PreAuthorize("hasAuthority('ROLE_DRIVER')")
    public ResponseEntity<AssignmentResponseDTO> startAssignment(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        // Izvlačenje driverId iz username-a
        Long driverId = driverService.getDriverIdFromUsername(userDetails.getUsername());

        Optional<AssignmentResponseDTO> started = assignmentService.startAssignment(id, driverId);

        if (started.isPresent()) {
            return ResponseEntity.ok(started.get());
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}/complete")
    @PreAuthorize("hasAuthority('ROLE_DRIVER') and @driverService.isAssignmentOwnedByDriver(#id, authentication.name)")
    public ResponseEntity<AssignmentResponseDTO> completeAssignment(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        Long driverId = driverService.getDriverIdFromUsername(userDetails.getUsername());
        Optional<AssignmentResponseDTO> completed = assignmentService.completeAssignment(id, driverId);

        if (completed.isPresent()) {
            return ResponseEntity.status(HttpStatus.OK).body(completed.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('ROLE_DISPATCHER')")
    public ResponseEntity<AssignmentResponseDTO> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return assignmentService.updateStatus(id, status)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}