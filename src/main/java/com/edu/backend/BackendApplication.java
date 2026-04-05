package com.edu.backend;

import com.edu.backend.model.User;
import com.edu.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

	@Bean
	public CommandLineRunner initAdmin(UserRepository userRepository) {
		return args -> {
			userRepository.findByEmail("admin@gmail.com").ifPresentOrElse(
				user -> {
					if (!"active".equals(user.getStatus())) {
						user.setStatus("active");
						user.setRole("admin");
						userRepository.save(user);
						System.out.println("Approved existing admin user: admin@gmail.com");
					}
				},
				() -> {
					User admin = new User();
					admin.setName("Super Admin");
					admin.setEmail("admin@gmail.com");
					admin.setPassword("admin123"); 
					admin.setRole("admin");
					admin.setStatus("active");
					userRepository.save(admin);
					System.out.println("Created new super admin account: admin@gmail.com");
				}
			);
		};
	}
}
