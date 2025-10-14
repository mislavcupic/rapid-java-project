package hr.algebra.rapid.logisticsandfleetmanagementsystem.controller;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.AssignmentRequestDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.AssignmentResponseDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.AssignmentService;
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
    // Ovisno o implementaciji, možda ćete trebati Service za dohvaćanje Driver ID-a iz UserDetails

    // --- 1. CRUD za DISPATCHER i ADMINISTRATORA ---

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_DISPATCHER')")
    public ResponseEntity<List<AssignmentResponseDTO>> getAllAssignments() {
        List<AssignmentResponseDTO> assignments = assignmentService.findAll();
        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_DISPATCHER')")
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
    @PreAuthorize("hasAuthority('ROLE_ADMIN')") // Samo Admin može brisati dodjele
    public ResponseEntity<Void> deleteAssignment(@PathVariable Long id) {
        assignmentService.deleteAssignment(id);
        return ResponseEntity.noContent().build();
    }

    // --- 2. ENDPOINT za DRIVER DASHBOARD ---
    // Vozač treba vidjeti SAMO svoje dodjele.

    // NAPOMENA: Ovdje je ključno dohvatiti ID vozača.
    // Pretpostavljam da imate uslugu (npr. UserService) koja može mapirati UserDetails.getUsername()
    // u Driver ID (ID iz nove tablice 'driver'). Ako to nemate, morate to dodati.

    @GetMapping("/my-schedule")
    @PreAuthorize("hasAuthority('ROLE_DRIVER')")
    public ResponseEntity<List<AssignmentResponseDTO>> getDriverSchedule(@AuthenticationPrincipal UserDetails userDetails) {

        // --- KRITIČNA LOGIKA: Pretvaranje UserDetails (username) u Driver ID (Long) ---
        // Budući da nemamo implementiranu tu logiku, pretpostavit ću da postoji metoda:
        // Long driverId = driverService.findDriverIdByUsername(userDetails.getUsername());

        // PRIVREMENO RJEŠENJE (Morate implementirati trajnu logiku):
        // Za testiranje možete koristiti fiksan ID, ali za produkciju morate koristiti token:
        // Long driverId = 1L; // Primjer fiksnog ID-a za Ivu Ivića

        // Primjer implementacije (potrebna je injekcija DriverService)
        // Ako je Vaš UserDetails objekt (AuthenticatedUser) sprema Driver ID:

        // PRILAGOĐAVANJE: Koristite UserService ili DriverService za dohvat ID-a iz username-a
        // Long driverId = driverService.getDriverIdFromUsername(userDetails.getUsername());

        // Za demonstraciju, koristimo fiksni ID dok ne implementirate helper service:
        // Dohvatite ID iz baze koristeći username, pa taj ID proslijedite servisu
        // OVA LINIJA JE KRITIČNA ZA VAŠU IMPLEMENTACIJU!

        // Privremeni mock (zamijenite ga pravom logikom):
        // Pretpostavimo da imamo metodu koja vraća ID Vozača iz tokena:
        Long currentDriverId = 1L; // Morate pronaći ID iz entiteta Driver na temelju username

        List<AssignmentResponseDTO> schedule = assignmentService.findAssignmentsByDriver(currentDriverId);
        return ResponseEntity.ok(schedule);
    }
}