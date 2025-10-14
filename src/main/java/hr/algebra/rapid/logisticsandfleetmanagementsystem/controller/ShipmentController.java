package hr.algebra.rapid.logisticsandfleetmanagementsystem.controller;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.ShipmentRequest;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.ShipmentResponse;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.exceptions.ResourceNotFoundException;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.ShipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/shipments")
@RequiredArgsConstructor
public class ShipmentController {

    private final ShipmentService shipmentService;

    // --- READ (Dohvaćanje svih pošiljaka) ---
    // Dostupno Dispečeru i Administratoru
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_DISPATCHER')")
    public ResponseEntity<List<ShipmentResponse>> getAllShipments() {
        List<ShipmentResponse> shipments = shipmentService.findAll();
        return ResponseEntity.ok(shipments);
    }

    // --- READ (Dohvaćanje po ID-u) ---
    // Dostupno Dispečeru i Administratoru
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_DISPATCHER')")
    public ResponseEntity<ShipmentResponse> getShipmentById(@PathVariable Long id) {
        // Koristimo .orElseThrow() da bacimo 404 ako pošiljka ne postoji
        ShipmentResponse shipment = shipmentService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment", "ID", id));
        return ResponseEntity.ok(shipment);
    }

    // --- CREATE (Kreiranje nove pošiljke) ---
    // Dostupno samo Dispečeru
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_DISPATCHER')")
    // @Valid pokreće validaciju iz ShipmentRequest.java
    public ResponseEntity<ShipmentResponse> createShipment(@Valid @RequestBody ShipmentRequest request) {
        ShipmentResponse newShipment = shipmentService.createShipment(request);
        // Vraća status 201 Created
        return new ResponseEntity<>(newShipment, HttpStatus.CREATED);
    }

    // --- UPDATE (Ažuriranje postojeće pošiljke) ---
    // Dostupno samo Dispečeru
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_DISPATCHER')")
    public ResponseEntity<ShipmentResponse> updateShipment(@PathVariable Long id,
                                                           @Valid @RequestBody ShipmentRequest request) {
        ShipmentResponse updatedShipment = shipmentService.updateShipment(id, request);
        return ResponseEntity.ok(updatedShipment);
    }

    // --- DELETE (Brisanje pošiljke) ---
    // Dostupno samo Administratoru (Dispečer ne bi trebao moći brisati)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteShipment(@PathVariable Long id) {
        shipmentService.deleteShipment(id);
        // Vraća status 204 No Content
        return ResponseEntity.noContent().build();
    }
}
