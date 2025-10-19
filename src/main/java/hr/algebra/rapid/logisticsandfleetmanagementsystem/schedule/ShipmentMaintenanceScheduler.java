package hr.algebra.rapid.logisticsandfleetmanagementsystem.schedule;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

// ✅ DODAJTE LOGGER: Koristimo Lombok @Slf4j za automatsko generiranje loggera
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j // Anotacija koja automatski stvara: private static final Logger log = LoggerFactory.getLogger(ShipmentMaintenanceScheduler.class);
public class ShipmentMaintenanceScheduler {

    private final AnalyticsService analyticsService;

    /**
     * Automatski pokreće masovno ažuriranje pošiljaka u status 'OVERDUE'.
     * Pokreće se svaki sat (u 0. minuti, 0. sekundi).
     */
    @Scheduled(cron = "0 0 * * * *")
    public void markOverdueShipments() {

        // Logiranje početka zadatka (INFO razina)
        log.info("SCHEDULER: Pokrenut zadatak za provjeru i označavanje istečenih pošiljaka.");

        try {
            int updatedRows = analyticsService.bulkMarkOverdue();

            if (updatedRows > 0) {
                // Logiranje uspjeha (INFO razina)
                log.info("SCHEDULER: Uspješno ažurirano {} pošiljaka u status 'OVERDUE'.", updatedRows);
            } else {
                // Logiranje statusa (INFO razina)
                log.info("SCHEDULER: Nema pošiljaka za označavanje kao 'OVERDUE'.");
            }
        } catch (Exception e) {
            // Logiranje greške (ERROR razina)
            log.error("SCHEDULER ERROR: Greška prilikom izvršavanja zadatka za istekle pošiljke: {}", e.getMessage(), e);
        }
    }
}