package hr.algebra.rapid.logisticsandfleetmanagementsystem.controller;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.VehicleAnalyticsResponse;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Kontroler za izlaganje funkcionalnosti analitike i masovnih operacija
 * pomoću JdbcTemplate-a.
 * Ove operacije su obično rezervirane za Administraciju i Dispečere.
 */
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    // -----------------------------------------------------------------
    // ANALITIKA (READ)
    // -----------------------------------------------------------------

    /**
     * Dohvaća prosječnu težinu svih aktivnih pošiljaka.
     * Samo Admin i Dispečer imaju pristup.
     * @return Prosječna težina u kilogramima.
     */
    @GetMapping("/shipments/average-active-weight")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_DISPATCHER')")
    public ResponseEntity<Double> getAverageActiveShipmentWeight() {
        Double averageWeight = analyticsService.getAverageActiveShipmentWeight();
        return ResponseEntity.ok(averageWeight);
    }

    // -----------------------------------------------------------------
    // BULK OPERACIJE (WRITE)
    // -----------------------------------------------------------------

    /**
     * Masovno ažurira status svih pošiljaka kojima je istekao očekivani rok isporuke
     * i postavlja im status na 'OVERDUE'.
     * Samo Admin ima pravo izvoditi masovne WRITE operacije.
     * @return Broj pogođenih (ažuriranih) redaka.
     */
    @PostMapping("/shipments/mark-overdue")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> bulkMarkOverdue() {
        int updatedRows = analyticsService.bulkMarkOverdue();

        String responseMessage = String.format("Uspješno ažurirano %d pošiljaka u status 'OVERDUE'.", updatedRows);

        return ResponseEntity.ok(responseMessage);
    }
    @GetMapping("/vehicles/status") // <--- OVO JE URL KOJI VAM JE FALIO
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_DISPATCHER')")
    public ResponseEntity<VehicleAnalyticsResponse> getVehicleAlertStatus() {
        // Poziva implementaciju iz JdbcAnalyticsServiceImpl (koja zove VehicleService)
        VehicleAnalyticsResponse response = analyticsService.getVehicleAlertStatus();
        return ResponseEntity.ok(response);
    }

}
