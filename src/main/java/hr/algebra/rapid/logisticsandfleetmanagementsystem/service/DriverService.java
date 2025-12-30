package hr.algebra.rapid.logisticsandfleetmanagementsystem.service;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.DriverRequestDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.DriverResponseDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.DriverUpdateDTO;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Optional;

public interface DriverService {

    // ========================================================================
    // POSTOJEĆE METODE (iz tvog originalnog koda)
    // ========================================================================

    List<DriverResponseDTO> findAllDrivers();

    Optional<DriverResponseDTO> findDriverById(Long id);

    Long getDriverIdFromUsername(String username);

    DriverResponseDTO createDriver(DriverRequestDTO request);

    DriverResponseDTO updateDriver(Long id, @Valid DriverUpdateDTO request);

    void deleteDriver(Long id);

    // ========================================================================
    // NOVE METODE za Driver Dashboard Security
    // ========================================================================

    /**
     * Provjerava pripada li Assignment s danim ID-em vozaču s danim username-om
     * @param assignmentId ID Assignment-a
     * @param username Username trenutno ulogiranog korisnika
     * @return true ako Assignment pripada ovom vozaču
     */
    boolean isAssignmentOwnedByDriver(Long assignmentId, String username);

    /**
     * Provjerava pripada li Shipment s danim ID-em vozaču kroz Assignment
     * @param shipmentId ID Shipment-a
     * @param username Username trenutno ulogiranog korisnika
     * @return true ako je Shipment assigniran ovom vozaču
     */
    boolean isShipmentAssignedToDriver(Long shipmentId, String username);
}