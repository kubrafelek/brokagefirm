package com.kubrafelek.brokagefirm.config;

import com.kubrafelek.brokagefirm.entity.Asset;
import com.kubrafelek.brokagefirm.entity.User;
import com.kubrafelek.brokagefirm.enums.Role;
import com.kubrafelek.brokagefirm.repository.AssetRepository;
import com.kubrafelek.brokagefirm.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AssetRepository assetRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, AssetRepository assetRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.assetRepository = assetRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        User admin = new User("admin", passwordEncoder.encode("admin123"), Role.ADMIN);
        userRepository.save(admin);

        User customer1 = new User("customer1", passwordEncoder.encode("pass123"), Role.CUSTOMER);
        User customer2 = new User("customer2", passwordEncoder.encode("pass123"), Role.CUSTOMER);
        userRepository.save(customer1);
        userRepository.save(customer2);

        Asset tryAsset1 = new Asset(customer1.getId(), "TRY", new BigDecimal("10000.00"), new BigDecimal("10000.00"));
        Asset tryAsset2 = new Asset(customer2.getId(), "TRY", new BigDecimal("15000.00"), new BigDecimal("15000.00"));
        assetRepository.save(tryAsset1);
        assetRepository.save(tryAsset2);

        Asset appleStock1 = new Asset(customer1.getId(), "AAPL", new BigDecimal("10.00"), new BigDecimal("10.00"));
        Asset googleStock2 = new Asset(customer2.getId(), "GOOGL", new BigDecimal("5.00"), new BigDecimal("5.00"));
        assetRepository.save(appleStock1);
        assetRepository.save(googleStock2);

        System.out.println("Initial data loaded:");
        System.out.println("Admin user: admin/admin123 (Role: " + admin.getRole() + ")");
        System.out.println("Customer 1: customer1/pass123 (ID: " + customer1.getId() + ", Role: " + customer1.getRole() + ")");
        System.out.println("Customer 2: customer2/pass123 (ID: " + customer2.getId() + ", Role: " + customer2.getRole() + ")");
        System.out.println("Assets initialized for each user");
    }
}
