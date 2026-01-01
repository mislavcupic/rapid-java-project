package hr.algebra.rapid.logisticsandfleetmanagementsystem.dto;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class VehicleResponseTest {

    @Test
    void testGetName_BranchCoverage() {
        VehicleResponse vehicle = new VehicleResponse();

        // Branch 1: oba su prisutna (make != null && model != null)
        vehicle.setMake("Iveco");
        vehicle.setModel("Daily");
        assertEquals("Iveco Daily", vehicle.getName());

        // Branch 2: samo make (make != null, model == null)
        vehicle.setModel(null);
        assertEquals("Iveco", vehicle.getName());

        // Branch 3: samo model (make == null, model != null)
        vehicle.setMake(null);
        vehicle.setModel("Daily");
        assertEquals("Daily", vehicle.getName());

        // Branch 4: oba su null
        vehicle.setModel(null);
        assertEquals("N/A", vehicle.getName());
    }

    @Test
    void testGetDriver_BranchCoverage() {
        VehicleResponse vehicle = new VehicleResponse();

        // Branch 1: currentDriver je null -> vraća "N/A"
        vehicle.setCurrentDriver(null);
        assertEquals("N/A", vehicle.getDriver());

        // Branch 2: currentDriver nije null, ali fullName je null -> vraća "N/A"
        DriverResponseDTO driver = new DriverResponseDTO();
        // Čak i ako postaviš ime/prezime, getDriver() gleda fullName polje
        driver.setFirstName("Ivan");
        driver.setLastName("Ivić");
        driver.setFullName(null);

        vehicle.setCurrentDriver(driver);
        assertEquals("N/A", vehicle.getDriver());

        // Branch 3: currentDriver nije null I fullName nije null -> vraća fullName
        driver.setFullName("Ivan Ivić"); // EKSPLICITNO postavi fullName
        vehicle.setCurrentDriver(driver);

        assertEquals("Ivan Ivić", vehicle.getDriver());
    }

    @Test
    void testGetRemainingKm_BranchCoverage() {
        VehicleResponse vehicle = new VehicleResponse();

        // Branch 1: remainingKmToService je null
        vehicle.setRemainingKmToService(null);
        assertEquals(0L, vehicle.getRemainingKm());

        // Branch 2: remainingKmToService ima vrijednost
        vehicle.setRemainingKmToService(500L);
        assertEquals(500L, vehicle.getRemainingKm());
    }

    @Test
    void testLombokGettersSetters() {
        // Pokrivanje preostalih polja koja generira Lombok za 100% coverage na poljima
        VehicleResponse vehicle = new VehicleResponse();
        vehicle.setId(1L);
        vehicle.setLicensePlate("ZG-1234-AA");
        vehicle.setFuelType("Diesel");

        assertEquals(1L, vehicle.getId());
        assertEquals("ZG-1234-AA", vehicle.getLicensePlate());
        assertEquals("Diesel", vehicle.getFuelType());
    }
}