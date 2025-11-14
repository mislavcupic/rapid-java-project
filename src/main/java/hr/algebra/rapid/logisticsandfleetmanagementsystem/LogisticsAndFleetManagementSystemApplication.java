package hr.algebra.rapid.logisticsandfleetmanagementsystem;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LogisticsAndFleetManagementSystemApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.configure()
				.directory("./")
				.ignoreIfMissing()
				.load();

		// ✅ Lambda s vitičastim zagradama
		dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));

		SpringApplication.run(LogisticsAndFleetManagementSystemApplication.class, args);
	}
}