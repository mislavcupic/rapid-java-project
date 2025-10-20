// hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.VehicleAnalyticsResponse.java

package hr.algebra.rapid.logisticsandfleetmanagementsystem.dto;

import lombok.Builder;
import lombok.Data;

/**
 * DTO koji prenosi ključne metrike Alert sustava s Backenda na Frontend
 * za potrebe Dashboarda/Analitike.
 */
@Data
@Builder
public class VehicleAnalyticsResponse {

    // Maintenance Alert Metrike
    private Long overdue; // Vozila KASNIMO sa servisom (< 0 km)
    private Long warning; // Vozila u upozorenju (0 do 5000 km do servisa)

    // Driver Scheduler Alert Metrike
    private Long free;    // Vozila bez vozača (SLOBODNO)

    // Ukupna Flota
    private Long total;   // Ukupan broj vozila
}