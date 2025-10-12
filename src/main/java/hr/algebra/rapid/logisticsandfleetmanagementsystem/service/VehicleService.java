package hr.algebra.rapid.logisticsandfleetmanagementsystem.service;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.Vehicle;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.VehicleRequest;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.VehicleResponse;

import java.util.List;
import java.util.Optional;

public interface VehicleService {
    VehicleResponse mapToResponse(Vehicle vehicle);
    /**
     * Dohvaća sva vozila.
     *
     * @return Lista svih vozila.
     */
    List<VehicleResponse> findAll();

    /**
     * Pronalazi vozilo po ID-u.
     *
     * @param id ID vozila.
     * @return Optional<Vehicle> ako je pronađeno.
     */
    Optional<VehicleResponse> findById(Long id);

    /**
     * Kreira novo vozilo na temelju DTO-a.
     * @param request DTO sa podacima o vozilu.
     * @return Kreirano vozilo.
     */
    Vehicle createVehicle(VehicleRequest request);


    Vehicle updateVehicle(Long id, VehicleRequest vehicleDetails);


    void deleteVehicle(Long id);

}


