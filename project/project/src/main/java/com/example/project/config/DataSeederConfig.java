package com.example.project.config;

import com.example.project.entity.User;
import com.example.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
@Profile("auth")  // Only run data seeding for auth profile
public class DataSeederConfig {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner seedUsers() {
        return args -> {
            try {
                // Seed default users for testing
                seedUser("john.doe@example.com", "John", "Doe", "pass123", true);
                seedUser("jane.smith@example.com", "Jane", "Smith", "pass123", true);

                // Seed additional users for wallet testing
                seedUser("user.one@example.com", "User", "One", "pass123", true);
                seedUser("user.two@example.com", "User", "Two", "pass123", true);
                seedUser("user.three@example.com", "User", "Three", "pass123", true);
                seedUser("user.four@example.com", "User", "Four", "pass123", true);
                seedUser("user.five@example.com", "User", "Five", "pass123", true);
                seedUser("user.six@example.com", "User", "Six", "pass123", true);
                seedUser("user.seven@example.com", "User", "Seven", "pass123", true);
                seedUser("user.eight@example.com", "User", "Eight", "pass123", true);
                seedUser("user.nine@example.com", "User", "Nine", "pass123", true);
                seedUser("user.ten@example.com", "User", "Ten", "pass123", true);
                seedUser("user.eleven@example.com", "User", "Eleven", "pass123", true);
                seedUser("user.twelve@example.com", "User", "Twelve", "pass123", true);
                seedUser("user.thirteen@example.com", "User", "Thirteen", "pass123", true);
                seedUser("user.fourteen@example.com", "User", "Fourteen", "pass123", true);
                seedUser("user.fifteen@example.com", "User", "Fifteen", "pass123", true);

                System.out.println("✓ Successfully seeded 15 test users");
            } catch (Exception e) {
                System.err.println("Error seeding users: " + e.getMessage());
                // Don't fail startup if seeding fails
            }
        };
    }

    // Insert defaults only once so restarts do not create duplicates.
    private void seedUser(String email, String firstName, String lastName, String password, boolean active) {
        try {
            if (userRepository.existsByEmail(email)) {
                return;
            }

            User user = new User();
            user.setEmail(email);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setPassword(passwordEncoder.encode(password));
            user.setActive(active);
            userRepository.save(user);
        } catch (Exception e) {
            System.err.println("Error seeding user " + email + ": " + e.getMessage());
        }
    }
}



