package hr.algebra.rapid.logisticsandfleetmanagementsystem;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles; // Importaj ovo

@SpringBootTest
@ActiveProfiles("test") // <--- OVO GOVORI SPRINGU DA KORISTI H2 (application-test.properties)
class LogisticsAndFleetManagementSystemApplicationTests {

	@Test
	void contextLoads() {
		// Prazna metoda znači: "Ako se context podigne, test je prošao!"
	}
}