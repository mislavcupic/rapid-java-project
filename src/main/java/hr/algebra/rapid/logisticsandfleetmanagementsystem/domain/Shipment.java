package hr.algebra.rapid.logisticsandfleetmanagementsystem.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "shipment")
@Getter
@Setter
@ToString(exclude = "assignment")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
@NoArgsConstructor
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tracking_number", unique = true, nullable = false)
    private String trackingNumber;

    @Column(name = "description")
    private String description;

    @Column(name = "weight_kg", nullable = false)
    private BigDecimal weightKg;

    @Column(name = "volume_m3")
    private BigDecimal volumeM3;

    @Column(name = "shipment_value")
    private BigDecimal shipmentValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ShipmentStatus status;

    @Column(name = "expected_delivery_date")
    private LocalDateTime expectedDeliveryDate;

    @Column(name = "actual_delivery_date")
    private LocalDateTime actualDeliveryDate;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "route_id", referencedColumnName = "id", unique = true)
    private Route route;
    @Column(name = "origin_address", nullable = false)
    private String originAddress;
    @Column(name = "destination_address", nullable = false)
    private String destinationAddress;
    @Column(name = "origin_latitude")
    private Double originLatitude;
    @Column(name = "origin_longitude")
    private Double originLongitude;
    @Column(name = "destination_latitude")
    private Double destinationLatitude;
    @Column(name = "destination_longitude")
    private Double destinationLongitude;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assignment_id")
    private Assignment assignment;

    @Column(name = "delivery_sequence")
    private Integer deliverySequence;
}