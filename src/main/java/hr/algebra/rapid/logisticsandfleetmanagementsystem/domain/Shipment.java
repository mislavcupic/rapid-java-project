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
    private BigDecimal volumeM3;

    @Column(name = "shipment_value")
    private BigDecimal shipmentValue;

    // 3. Status - KORIŠTENJE ENUM-a
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ShipmentStatus status;

    @Column(name = "expected_delivery_date")
    private LocalDateTime expectedDeliveryDate;

    @Column(name = "actual_delivery_date")
    private LocalDateTime actualDeliveryDate;


    // =================================================================
    // VEZA NA ROUTE - OVO JE KLJUČNO!
    // =================================================================

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "route_id", referencedColumnName = "id", unique = true)
    private Route route;


    // Adrese ostaju radi SQL kompatibilnosti (iako su primarne na Ruti)
    @Column(name = "origin_address", nullable = false)
    private String originAddress;

    @Column(name = "destination_address", nullable = false)
    private String destinationAddress;


}