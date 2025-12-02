package hr.algebra.rapid.logisticsandfleetmanagementsystem.schedule;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
@Slf4j
public class ShipmentMaintenanceScheduler {

    private final AnalyticsService analyticsService;

    @PostConstruct
    public void init() {
        log.info("ShipmentMaintenanceScheduler je inicijaliziran i spreman za rad.");
    }

    /**
     * Automatski pokreće masovno ažuriranje pošiljaka u status 'OVERDUE'.
     * Pokreće se svaku minutu (u 0. sekundi).
     */
    @Scheduled(cron = "0 * * * * *")
    public void markOverdueShipments() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        log.info("SCHEDULER POKRENUT u {}", timestamp);

        try {
            int updatedRows = analyticsService.bulkMarkOverdue();

            if (updatedRows > 0) {
                log.info("Ažurirano {} pošiljaka u status 'OVERDUE'.", updatedRows);
            } else {
                log.info("Nema pošiljaka za označavanje kao 'OVERDUE'.");
            }
        } catch (Exception e) {
            log.error("GREŠKA prilikom izvršavanja scheduler zadatka: {}", e.getMessage(), e);
        }
    }
}
