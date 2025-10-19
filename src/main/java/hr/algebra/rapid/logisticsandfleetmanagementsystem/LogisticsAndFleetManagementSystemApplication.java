package hr.algebra.rapid.logisticsandfleetmanagementsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LogisticsAndFleetManagementSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(LogisticsAndFleetManagementSystemApplication.class, args);
	}

}
