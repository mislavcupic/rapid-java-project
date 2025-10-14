package hr.algebra.rapid.logisticsandfleetmanagementsystem.domain;

import jakarta.persistence.*;
import lombok.Data; // Omogućuje gettere, settere, toString, equals i hashCode za SVA POLJA
import java.time.LocalDate;
import java.util.Optional;

@Entity
@Table(name = "driver")
@Data // Kritično: Omogućuje setLicenseNumber, setPhoneNumber, getLicenseNumber itd.
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_info_id", unique = true, nullable = false)
    private UserInfo userInfo;

    // --- POLJA KOJA SU NEDOSTAJALA I UZROKOVALA "Cannot find symbol" ---

    @Column(name = "license_number", unique = true)
    private String licenseNumber;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "license_expiration_date")
    private LocalDate licenseExpirationDate;

    // --- POLJE ZA POVEZANO VOZILO (koristi se u DriverServiceImpl) ---

    // Veza s vozilom: Driver može biti current_driver samo JEDNOM vozilu
    @OneToOne(mappedBy = "currentDriver", fetch = FetchType.LAZY)
    private Vehicle currentVehicle; // BITNO: Ovo omogućuje getCurrentVehicle() metodu

    // --- TRANSIENT I POMOĆNE METODE ---

    @Transient
    public String getFullName() {
        if (userInfo != null) {
            return userInfo.getFirstName() + " " + userInfo.getLastName();
        }
        return "N/A";
    }

    /**
     * Pomoćna metoda koja vraća Optional<Vehicle> (koristi se u deleteDriver)
     */
    public Optional<Vehicle> getCurrentVehicle() {
        return Optional.ofNullable(currentVehicle);
    }
}