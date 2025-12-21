package hr.algebra.rapid.logisticsandfleetmanagementsystem.service.impl;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.VehicleAnalyticsResponse;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.exceptions.BulkMarkOverdueException;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.AnalyticsService;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.VehicleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // Dodan import za Logger
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j // Dodaje 'log' objekt
public class JdbcAnalyticsServiceImpl implements AnalyticsService {

    // Spring automatski injektira konfigurirani JdbcTemplate bean
    private final JdbcTemplate jdbcTemplate;
    private final VehicleService vehicleService;
    // 1. ANALITIKA (READ)
    @Override
    public Double getAverageActiveShipmentWeight() {
        // SQL direktno izračunava prosjek na razini baze
        String sql = "SELECT AVG(weight_kg) FROM shipment WHERE status IN ('PENDING', 'IN_TRANSIT')";

        try {
            // queryForObject je za dohvaćanje JEDNOG rezultata (npr. SUM, COUNT, AVG)
            Double result = jdbcTemplate.queryForObject(sql, Double.class);

            log.info("Analitika: Izračunata prosječna težina aktivnih pošiljaka: {} kg", result);

            return result != null ? result : 0.0;
        } catch (Exception e) {
            log.error("Greška pri izračunu prosječne težine:", e);
            return 0.0; // Vraćanje nule u slučaju greške
        }
    }

    // 2. BULK OPERACIJA (WRITE)
    @Override
    @Transactional // Važno: Koristite transakciju i za bulk operacije!
    public int bulkMarkOverdue() {
        LocalDateTime now = LocalDateTime.now();

        // Jedan UPDATE upit koji cilja sve redove odjednom
        String sql = "UPDATE shipment SET status = 'OVERDUE' " +
                "WHERE expected_delivery_date < ? " +
                "AND status NOT IN ('DELIVERED', 'CANCELED', 'OVERDUE')";

        try {
            log.info("Pokretanje bulk Mark Overdue upita. Datum usporedbe: {}", now);

            // .update() izvršava INSERT, UPDATE, DELETE i vraća broj pogođenih redaka
            int updatedRows = jdbcTemplate.update(sql, now);

            log.info("Bulk Mark Overdue završen. Ažurirano redova: {}", updatedRows);

            return updatedRows;
        } catch (Exception e) {
            log.error("KRITIČNA GREŠKA pri bulkMarkOverdue:", e);
            throw new BulkMarkOverdueException("Greška pri masovnom ažuriranju statusa pošiljaka.", e);
        }
    }
    @Override
    @Transactional(readOnly = true)
    public VehicleAnalyticsResponse getVehicleAlertStatus() {
        // Pozivamo metode koje smo definirali u VehicleServiceImpl za brojanje
        Long overdue = vehicleService.countVehiclesOverdueForService();
        Long warning = vehicleService.countVehiclesInServiceWarning(5000L); // Prag upozorenja 5000 km
        Long free = vehicleService.countFreeVehicles();
        Long total = vehicleService.countTotalVehicles();

        log.info("Analitika Vozila: Prekoračeno={}, Upozorenje={}, Slobodno={}, Ukupno={}",
                overdue, warning, free, total);

        // Vraćamo DTO s agregiranim podacima
        return VehicleAnalyticsResponse.builder()
                .overdue(overdue)
                .warning(warning)
                .free(free)
                .total(total)
                .build();
    }
}