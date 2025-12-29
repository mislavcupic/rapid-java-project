package hr.algebra.rapid.logisticsandfleetmanagementsystem.domain;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "assignment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "shipments") // DODAJ OVO
// Osigurava ispravno ponašanje u Setovima i Mapama
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include // Koristi ID za equals/hashCode
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @OneToMany(mappedBy = "assignment", fetch = FetchType.LAZY)
    private List<Shipment> shipments;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;


    @Column(name = "status", nullable = false)
    private String status;

    @OneToOne(cascade = CascadeType.ALL) // Kaskadiraj operacije (kada kreiraš Assignment, kreiraš i Route)
    @JoinColumn(name = "route_id", referencedColumnName = "id", nullable = false)
    Route route;
}