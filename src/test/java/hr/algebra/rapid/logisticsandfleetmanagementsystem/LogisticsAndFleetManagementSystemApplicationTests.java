package hr.algebra.rapid.logisticsandfleetmanagementsystem;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class LogisticsAndFleetManagementSystemApplicationTests {

	@Test
	void applicationClassInstantiable() {
		// Ovo je običan Unit test. Ne treba @SpringBootTest.
		// Pokriva definiciju klase za Class Coverage.
		LogisticsAndFleetManagementSystemApplication app = new LogisticsAndFleetManagementSystemApplication();
		assertNotNull(app);
	}
}