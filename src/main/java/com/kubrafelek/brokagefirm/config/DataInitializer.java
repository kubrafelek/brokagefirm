package com.kubrafelek.brokagefirm.config;

import com.kubrafelek.brokagefirm.entity.Asset;
import com.kubrafelek.brokagefirm.entity.Customer;
import com.kubrafelek.brokagefirm.repository.AssetRepository;
import com.kubrafelek.brokagefirm.repository.CustomerRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {

    private final CustomerRepository customerRepository;

    private final AssetRepository assetRepository;

    private final PasswordEncoder passwordEncoder;

    public DataInitializer(CustomerRepository customerRepository, AssetRepository assetRepository, PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.assetRepository = assetRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        Customer admin = new Customer("admin", passwordEncoder.encode("admin123"), true);
        customerRepository.save(admin);

        Customer customer1 = new Customer("customer1", passwordEncoder.encode("pass123"), false);
        Customer customer2 = new Customer("customer2", passwordEncoder.encode("pass123"), false);
        customerRepository.save(customer1);
        customerRepository.save(customer2);

        Asset tryAsset1 = new Asset(customer1.getId(), "TRY", new BigDecimal("10000.00"), new BigDecimal("10000.00"));
        Asset tryAsset2 = new Asset(customer2.getId(), "TRY", new BigDecimal("15000.00"), new BigDecimal("15000.00"));
        assetRepository.save(tryAsset1);
        assetRepository.save(tryAsset2);

        Asset appleStock1 = new Asset(customer1.getId(), "AAPL", new BigDecimal("10.00"), new BigDecimal("10.00"));
        Asset appleStock2 = new Asset(customer2.getId(), "GOOGL", new BigDecimal("5.00"), new BigDecimal("5.00"));
        assetRepository.save(appleStock1);
        assetRepository.save(appleStock2);

        System.out.println("Initial data loaded:");
        System.out.println("Admin user: admin/admin123");
        System.out.println("Customer 1: customer1/pass123 (ID: " + customer1.getId() + ")");
        System.out.println("Customer 2: customer2/pass123 (ID: " + customer2.getId() + ")");
    }
}
