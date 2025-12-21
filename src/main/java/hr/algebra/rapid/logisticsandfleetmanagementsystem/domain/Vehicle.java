package hr.algebra.rapid.logisticsandfleetmanagementsystem.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate; // DODANO: Za praćenje datuma
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

    // NOVO: Polja za održavanje vozila (Maintenance)
    @Column(name = "current_mileage_km")
    private Long currentMileageKm;

    @Column(name = "last_service_date")
    private LocalDate lastServiceDate;

    @Column(name = "next_service_mileage_km")
    private Long nextServiceMileageKm;

    @Column(name = "fuel_consumption_liters_per_100km")
    private BigDecimal fuelConsumptionLitersPer100Km;

    // 🛑 KRITIČNA KOREKCIJA: Veza s Driver entitetom
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_driver_id", unique = true)
    private Driver currentDriver;

    public Optional<Driver> getCurrentDriverOptional() {
        return Optional.ofNullable(currentDriver);
    }
}