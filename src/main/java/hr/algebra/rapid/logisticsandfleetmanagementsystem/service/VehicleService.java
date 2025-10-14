package hr.algebra.rapid.logisticsandfleetmanagementsystem.service;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.Vehicle;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.VehicleRequest;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.VehicleResponse;

import java.util.List;
import java.util.Optional;

public interface VehicleService {

    // Pomoćna metoda za mapiranje (korisna u impl klasi)
    VehicleResponse mapToResponse(Vehicle vehicle);

    /** Dohvaća sva vozila. */
    // Promijenio sam naziv u findAllVehicles radi bolje jasnoće
    List<VehicleResponse> findAllVehicles();

    /** Pronalazi vozilo po ID-u. */
    // Promijenio sam naziv u findVehicleById radi bolje jasnoće
    Optional<VehicleResponse> findVehicleById(Long id);

    /** Kreira novo vozilo na temelju DTO-a.
     * Vraća VehicleResponse DTO.
     */
    // ✅ KRITIČNA PROMJENA: Vraća VehicleResponse DTO
    VehicleResponse createVehicle(VehicleRequest request);

    /** Ažurira vozilo na temelju DTO-a.
     * Vraća VehicleResponse DTO.
     */
    // ✅ KRITIČNA PROMJENA: Vraća VehicleResponse DTO
    VehicleResponse updateVehicle(Long id, VehicleRequest vehicleDetails);

    /** Briše vozilo. */
    void deleteVehicle(Long id);
}