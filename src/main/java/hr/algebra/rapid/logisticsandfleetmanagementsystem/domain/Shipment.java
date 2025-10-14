package hr.algebra.rapid.logisticsandfleetmanagementsystem.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "shipment")
@Data
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 1. Osnovni podaci o pošiljci
    @Column(name = "tracking_number", unique = true, nullable = false)
    private String trackingNumber; // Jedinstveni kod za praćenje

    @Column(name = "description")
    private String description;

    // 2. Podaci o teretu
    @Column(name = "weight_kg", nullable = false)
    private BigDecimal weightKg;

    @Column(name = "volume_m3")
    private BigDecimal volumeM3; // Količina tereta (opcionalno)

    // 3. Podaci o ruti
    @Column(name = "origin_address", nullable = false)
    private String originAddress;

    @Column(name = "destination_address", nullable = false)
    private String destinationAddress;

    // 4. Status i datumi
    // Koristimo String za status, ali u praksi je bolje koristiti Enum.
    @Column(name = "status", nullable = false)
    private String status; // Npr. PENDING, IN_TRANSIT, DELIVERED, CANCELED

    @Column(name = "expected_delivery_date")
    private LocalDateTime expectedDeliveryDate;

    @Column(name = "actual_delivery_date")
    private LocalDateTime actualDeliveryDate;

    // Opcija: Veza na Dispečera koji je kreirao pošiljku (ako je potrebno)
    // @ManyToOne
    // private UserInfo createdBy;
}