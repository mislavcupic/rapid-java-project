// hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.ShipmentResponse.java

package hr.algebra.rapid.logisticsandfleetmanagementsystem.dto;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.ShipmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShipmentResponse {

    private Long id;
    private String trackingNumber;
    private String description;

    // Adrese
    private String originAddress;
    private String destinationAddress;

    // NOVO: Koordinate za prikaz na mapi
    private Double originLatitude;
    private Double originLongitude;
    private Double destinationLatitude;
    private Double destinationLongitude;

    // Podaci o pošiljci
    private BigDecimal weightKg;
    private BigDecimal volumeM3;
    private BigDecimal shipmentValue; // Osigurano da je polje prisutno
    private ShipmentStatus status;

    private LocalDateTime expectedDeliveryDate;
    private LocalDateTime actualDeliveryDate;

    // Podaci o proračunatoj ruti
    private Double estimatedDistanceKm;
    private Long estimatedDurationMinutes;
    private String routeStatus;

    // ✅ PROMJENA: Dodano polje routeId koje backend vraća
    private Long routeId;

    private Integer deliverySequence;
}