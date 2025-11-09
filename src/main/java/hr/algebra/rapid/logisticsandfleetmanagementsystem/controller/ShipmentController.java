package hr.algebra.rapid.logisticsandfleetmanagementsystem.controller;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.IssueReportDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.ProofOfDeliveryDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.ShipmentRequest;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.ShipmentResponse;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.exceptions.ResourceNotFoundException;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.AssignmentRepository;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.DriverService;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.ShipmentService;
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
@RequestMapping("/api/shipments")
@RequiredArgsConstructor
public class ShipmentController {

    private final ShipmentService shipmentService;
    private final DriverService driverService; // ✅ NOVO - za Driver Dashboard

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_DISPATCHER')")
    public ResponseEntity<List<ShipmentResponse>> getAllShipments() {
        List<ShipmentResponse> shipments = shipmentService.findAll();
        return ResponseEntity.ok(shipments);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_DISPATCHER') or " +
            "@driverService.isShipmentAssignedToDriver(#id, authentication.name)") // ✅ NOVO - Driver može vidjeti svoje
    public ResponseEntity<ShipmentResponse> getShipmentById(@PathVariable Long id) {
        ShipmentResponse shipment = shipmentService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment", "ID", id));
        return ResponseEntity.ok(shipment);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_DISPATCHER')")
    public ResponseEntity<ShipmentResponse> createShipment(@Valid @RequestBody ShipmentRequest request) {
        ShipmentResponse newShipment = shipmentService.createShipment(request);
        return new ResponseEntity<>(newShipment, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_DISPATCHER')")
    public ResponseEntity<ShipmentResponse> updateShipment(@PathVariable Long id,
                                                           @Valid @RequestBody ShipmentRequest request) {
        ShipmentResponse updatedShipment = shipmentService.updateShipment(id, request);
        return ResponseEntity.ok(updatedShipment);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteShipment(@PathVariable Long id) {
        shipmentService.deleteShipment(id);
        return ResponseEntity.noContent().build();
    }

    // ========================================================================
    // ✅ NOVI DRIVER ENDPOINTI - Akcije za promjenu statusa pošiljke
    // ========================================================================

    /**
     * Driver započinje dostavu (SCHEDULED → IN_TRANSIT)
     * PUT /api/shipments/{id}/start
     */
    @PutMapping("/{id}/start")
    @PreAuthorize("hasAuthority('ROLE_DRIVER') and " +
            "@driverService.isShipmentAssignedToDriver(#id, authentication.name)")
    public ResponseEntity<ShipmentResponse> startDelivery(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long driverId = driverService.getDriverIdFromUsername(userDetails.getUsername());
        ShipmentResponse updatedShipment = shipmentService.startDelivery(id, driverId);

        return ResponseEntity.ok(updatedShipment);
    }

    /**
     * Driver završava dostavu s Proof of Delivery (IN_TRANSIT → DELIVERED)
     * POST /api/shipments/{id}/complete
     */
    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAuthority('ROLE_DRIVER') and " +
            "@driverService.isShipmentAssignedToDriver(#id, authentication.name)")
    public ResponseEntity<ShipmentResponse> completeDelivery(
            @PathVariable Long id,
            @Valid @RequestBody ProofOfDeliveryDTO pod,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long driverId = driverService.getDriverIdFromUsername(userDetails.getUsername());
        ShipmentResponse completedShipment = shipmentService.completeDelivery(id, driverId, pod);

        return ResponseEntity.ok(completedShipment);
    }

    /**
     * Driver prijavljuje problem s dostavom (IN_TRANSIT → DELAYED)
     * PUT /api/shipments/{id}/report-issue
     */
    @PutMapping("/{id}/report-issue")
    @PreAuthorize("hasAuthority('ROLE_DRIVER') and " +
            "@driverService.isShipmentAssignedToDriver(#id, authentication.name)")
    public ResponseEntity<ShipmentResponse> reportIssue(
            @PathVariable Long id,
            @Valid @RequestBody IssueReportDTO issue,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long driverId = driverService.getDriverIdFromUsername(userDetails.getUsername());
        ShipmentResponse updatedShipment = shipmentService.reportIssue(id, driverId, issue);

        return ResponseEntity.ok(updatedShipment);
    }
}////package hr.algebra.rapid.logisticsandfleetmanagementsystem.controller;
////
////import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.ShipmentRequest;
////import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.ShipmentResponse;
////import hr.algebra.rapid.logisticsandfleetmanagementsystem.exceptions.ResourceNotFoundException;
////import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.ShipmentService;
////import lombok.RequiredArgsConstructor;
////import org.springframework.http.HttpStatus;
////import org.springframework.http.ResponseEntity;
////import org.springframework.security.access.prepost.PreAuthorize;
////import org.springframework.web.bind.annotation.*;
//package hr.algebra.rapid.logisticsandfleetmanagementsystem.controller;
//
//import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.IssueReportDTO;
//import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.ProofOfDeliveryDTO;
//import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.ShipmentRequest;
//import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.ShipmentResponse;
//import hr.algebra.rapid.logisticsandfleetmanagementsystem.exceptions.ResourceNotFoundException;
//import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.DriverService;
//import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.ShipmentService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.web.bind.annotation.*;
//
//import jakarta.validation.Valid;
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/shipments")
//@RequiredArgsConstructor
//public class ShipmentController {
//
//    private final ShipmentService shipmentService;
//    private final DriverService driverService; // ✅ NOVO - za Driver Dashboard
//
//    @GetMapping
//    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_DISPATCHER')")
//    public ResponseEntity<List<ShipmentResponse>> getAllShipments() {
//        List<ShipmentResponse> shipments = shipmentService.findAll();
//        return ResponseEntity.ok(shipments);
//    }
//
//    @GetMapping("/{id}")
//    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_DISPATCHER') or " +
//            "@driverService.isShipmentAssignedToDriver(#id, authentication.name)") // ✅ NOVO - Driver može vidjeti svoje
//    public ResponseEntity<ShipmentResponse> getShipmentById(@PathVariable Long id) {
//        ShipmentResponse shipment = shipmentService.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Shipment", "ID", id));
//        return ResponseEntity.ok(shipment);
//    }
//
//    @PostMapping
//    @PreAuthorize("hasAuthority('ROLE_DISPATCHER')")
//    public ResponseEntity<ShipmentResponse> createShipment(@Valid @RequestBody ShipmentRequest request) {
//        ShipmentResponse newShipment = shipmentService.createShipment(request);
//        return new ResponseEntity<>(newShipment, HttpStatus.CREATED);
//    }
//
//    @PutMapping("/{id}")
//    @PreAuthorize("hasAuthority('ROLE_DISPATCHER')")
//    public ResponseEntity<ShipmentResponse> updateShipment(@PathVariable Long id,
//                                                           @Valid @RequestBody ShipmentRequest request) {
//        ShipmentResponse updatedShipment = shipmentService.updateShipment(id, request);
//        return ResponseEntity.ok(updatedShipment);
//    }
//
//    @DeleteMapping("/{id}")
//    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
//    public ResponseEntity<Void> deleteShipment(@PathVariable Long id) {
//        shipmentService.deleteShipment(id);
//        return ResponseEntity.noContent().build();
//    }
//
//    // ========================================================================
//    // ✅ NOVI DRIVER ENDPOINTI - Akcije za promjenu statusa pošiljke
//    // ========================================================================
//
//    /**
//     * Driver započinje dostavu (SCHEDULED → IN_TRANSIT)
//     * PUT /api/shipments/{id}/start
//     */
//    @PutMapping("/{id}/start")
//    @PreAuthorize("hasAuthority('ROLE_DRIVER') and " +
//            "@driverService.isShipmentAssignedToDriver(#id, authentication.name)")
//    public ResponseEntity<ShipmentResponse> startDelivery(
//            @PathVariable Long id,
//            @AuthenticationPrincipal UserDetails userDetails) {
//
//        Long driverId = driverService.getDriverIdFromUsername(userDetails.getUsername());
//        ShipmentResponse updatedShipment = shipmentService.startDelivery(id, driverId);
//
//        return ResponseEntity.ok(updatedShipment);
//    }
//
//    /**
//     * Driver završava dostavu s Proof of Delivery (IN_TRANSIT → DELIVERED)
//     * POST /api/shipments/{id}/complete
//     */
//    @PostMapping("/{id}/complete")
//    @PreAuthorize("hasAuthority('ROLE_DRIVER') and " +
//            "@driverService.isShipmentAssignedToDriver(#id, authentication.name)")
//    public ResponseEntity<ShipmentResponse> completeDelivery(
//            @PathVariable Long id,
//            @Valid @RequestBody ProofOfDeliveryDTO pod,
//            @AuthenticationPrincipal UserDetails userDetails) {
//
//        Long driverId = driverService.getDriverIdFromUsername(userDetails.getUsername());
//        ShipmentResponse completedShipment = shipmentService.completeDelivery(id, driverId, pod);
//
//        return ResponseEntity.ok(completedShipment);
//    }
//
//    /**
//     * Driver prijavljuje problem s dostavom (IN_TRANSIT → DELAYED)
//     * PUT /api/shipments/{id}/report-issue
//     */
//    @PutMapping("/{id}/report-issue")
//    @PreAuthorize("hasAuthority('ROLE_DRIVER') and " +
//            "@driverService.isShipmentAssignedToDriver(#id, authentication.name)")
//    public ResponseEntity<ShipmentResponse> reportIssue(
//            @PathVariable Long id,
//            @Valid @RequestBody IssueReportDTO issue,
//            @AuthenticationPrincipal UserDetails userDetails) {
//
//        Long driverId = driverService.getDriverIdFromUsername(userDetails.getUsername());
//        ShipmentResponse updatedShipment = shipmentService.reportIssue(id, driverId, issue);
//
//        return ResponseEntity.ok(updatedShipment);
//    }
//}//
////import jakarta.validation.Valid;
////import java.util.List;
////
////@RestController
////@RequestMapping("/api/shipments")
////@RequiredArgsConstructor
////public class ShipmentController {
////
////    private final ShipmentService shipmentService;
////
////    // --- READ (Dohvaćanje svih pošiljaka) ---
////    // Dostupno Dispečeru i Administratoru
////    @GetMapping
////    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_DISPATCHER')")
////    public ResponseEntity<List<ShipmentResponse>> getAllShipments() {
////        List<ShipmentResponse> shipments = shipmentService.findAll();
////        return ResponseEntity.ok(shipments);
////    }
////
////    // --- READ (Dohvaćanje po ID-u) ---
////    // Dostupno Dispečeru i Administratoru
////    @GetMapping("/{id}")
////    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_DISPATCHER')")
////    public ResponseEntity<ShipmentResponse> getShipmentById(@PathVariable Long id) {
////        // Koristimo .orElseThrow() da bacimo 404 ako pošiljka ne postoji
////        ShipmentResponse shipment = shipmentService.findById(id)
////                .orElseThrow(() -> new ResourceNotFoundException("Shipment", "ID", id));
////        return ResponseEntity.ok(shipment);
////    }
////
////    // --- CREATE (Kreiranje nove pošiljke) ---
////    // Dostupno samo Dispečeru
////    @PostMapping
////    @PreAuthorize("hasAuthority('ROLE_DISPATCHER')")
////    // @Valid pokreće validaciju iz ShipmentRequest.java
////    public ResponseEntity<ShipmentResponse> createShipment(@Valid @RequestBody ShipmentRequest request) {
////        ShipmentResponse newShipment = shipmentService.createShipment(request);
////        // Vraća status 201 Created
////        return new ResponseEntity<>(newShipment, HttpStatus.CREATED);
////    }
////
////    // --- UPDATE (Ažuriranje postojeće pošiljke) ---
////    // Dostupno samo Dispečeru
////    @PutMapping("/{id}")
////    @PreAuthorize("hasAuthority('ROLE_DISPATCHER')")
////    public ResponseEntity<ShipmentResponse> updateShipment(@PathVariable Long id,
////                                                           @Valid @RequestBody ShipmentRequest request) {
////        ShipmentResponse updatedShipment = shipmentService.updateShipment(id, request);
////        return ResponseEntity.ok(updatedShipment);
////    }
////
////    // --- DELETE (Brisanje pošiljke) ---
////    // Dostupno samo Administratoru (Dispečer ne bi trebao moći brisati)
////    @DeleteMapping("/{id}")
////    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
////    public ResponseEntity<Void> deleteShipment(@PathVariable Long id) {
////        shipmentService.deleteShipment(id);
////        // Vraća status 204 No Content
////        return ResponseEntity.noContent().build();
////    }
////}
