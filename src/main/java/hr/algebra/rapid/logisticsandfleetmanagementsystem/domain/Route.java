// hr.algebra.rapid.logisticsandfleetmanagementsystem.model.Route.java

package hr.algebra.rapid.logisticsandfleetmanagementsystem.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

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

    // Početne i Krajnje koordinate (KLJUČNO ZA MAPU)
    private String originAddress;
    private Double originLatitude;
    private Double originLongitude;

    private String destinationAddress;
    private Double destinationLatitude;
    private Double destinationLongitude;

    // PRORAČUNATI PODACI (Potrebno od vanjskog API-ja)
    @Column(nullable = false)
    private Double estimatedDistanceKm;

    @Column(nullable = false)
    private Long estimatedDurationMinutes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RouteStatus status = RouteStatus.DRAFT;

    // Povezivanje s Assignmentom
    @OneToOne(mappedBy = "route")
    private Assignment assignment;
}