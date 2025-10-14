package hr.algebra.rapid.logisticsandfleetmanagementsystem.service;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.DriverRequestDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.DriverResponseDTO;
import java.util.List;
import java.util.Optional;

public interface DriverService {


    List<DriverResponseDTO> findAllDrivers();
    Optional<DriverResponseDTO> findDriverById(Long id);
    Long getDriverIdFromUsername(String username);
    DriverResponseDTO createDriver(DriverRequestDTO request);
    DriverResponseDTO updateDriver(Long id, DriverRequestDTO request);
    void deleteDriver(Long id);
}