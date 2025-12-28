package hr.algebra.rapid.logisticsandfleetmanagementsystem.repository;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    // 1. Rješava problem: 'cannot find symbol findByLicensePlate'
    // Koristi se u createVehicle i updateVehicle za provjeru duplikata registracije
    Optional<Vehicle> findByLicensePlate(String licensePlate);

    // 2. Potrebna je i ova metoda za provjeru konflikta (jedan vozač može voziti samo jedno vozilo)
    // Koristi se u createVehicle i updateVehicle
    Optional<Vehicle> findByCurrentDriverId(Long driverId);




}
