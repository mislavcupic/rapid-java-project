package hr.algebra.rapid.logisticsandfleetmanagementsystem.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "routes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String originAddress;
    private Double originLatitude;
    private Double originLongitude;

    private String destinationAddress;
    private Double destinationLatitude;
    private Double destinationLongitude;

    // Promijenjeno u nullable=true kako bi se ruta mogla spremiti samo s koordinatama
    @Column(nullable = true)
    private Double estimatedDistanceKm = 0.0;

    @Column(nullable = true)
    private Long estimatedDurationMinutes = 0L;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RouteStatus status = RouteStatus.DRAFT;

    @OneToOne(mappedBy = "route")
    private Assignment assignment;
}