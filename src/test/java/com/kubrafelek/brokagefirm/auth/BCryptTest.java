package com.kubrafelek.brokagefirm.auth;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BCryptTest {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // Test if the stored hash matches "pass123"
        String storedHash = "$2a$10$gSAhZrxMllrbgeg/Qkra6uQqHqmVVWa4qTBg1J8QeWMXPV.dDOGK6";
        String password = "pass123";

        boolean matches = encoder.matches(password, storedHash);
        System.out.println("Password 'pass123' matches stored hash: " + matches);

        // Generate a new hash for comparison
        String newHash = encoder.encode(password);
        System.out.println("New hash for 'pass123': " + newHash);

        // Test if the new hash matches
        boolean newMatches = encoder.matches(password, newHash);
        System.out.println("Password 'pass123' matches new hash: " + newMatches);

        // Test admin password
        String adminHash = "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.";
        String adminPassword = "admin123";
        boolean adminMatches = encoder.matches(adminPassword, adminHash);
        System.out.println("Password 'admin123' matches admin hash: " + adminMatches);
    }
}
