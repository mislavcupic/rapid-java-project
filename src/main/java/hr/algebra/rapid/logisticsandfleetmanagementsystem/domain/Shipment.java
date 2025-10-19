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
    private String trackingNumber;

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


    // razmislit ću o enumu
    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "expected_delivery_date")
    private LocalDateTime expectedDeliveryDate;

    @Column(name = "actual_delivery_date")
    private LocalDateTime actualDeliveryDate;


}