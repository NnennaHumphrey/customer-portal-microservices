package com.nnenna.portal.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AuthConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Seeds an admin account so invoices can be issued in the demo. */
    @Bean
    CommandLineRunner seedAdmin(UserRepository users, PasswordEncoder encoder,
                                @Value("${app.admin.email:admin@portal.local}") String email,
                                @Value("${app.admin.password:admin12345}") String password) {
        return args -> {
            if (!users.existsByEmail(email)) {
                users.save(new UserAccount(email, encoder.encode(password), "ADMIN"));
            }
        };
    }
}
