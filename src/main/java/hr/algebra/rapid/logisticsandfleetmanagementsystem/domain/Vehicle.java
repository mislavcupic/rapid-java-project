package hr.algebra.rapid.logisticsandfleetmanagementsystem.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Optional;

@Entity
@Table(name = "vehicle")
@Data
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "license_plate", unique = true, nullable = false)
    private String licensePlate;

    @Column(nullable = false)
    private String make;

    @Column(nullable = false)
    private String model;

    @Column(name = "model_year")
    private Integer year;

    @Column(name = "fuel_type")
    private String fuelType;

    @Column(name = "load_capacity_kg")
    private BigDecimal loadCapacityKg;

    // 🛑 KRITIČNA KOREKCIJA: Veza s Driver entitetom
    // Polje je sada tipa Driver, a JoinColumn se referencira na driver.id
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_driver_id", unique = true)
    private Driver currentDriver;

    // Pomoćna metoda za lakše rukovanje Optional-om
    public Optional<Driver> getCurrentDriverOptional() {
        return Optional.ofNullable(currentDriver);
    }
}