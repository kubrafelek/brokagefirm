package com.kubrafelek.brokagefirm.auth;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GenerateHashes {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        System.out.println("Generating correct password hashes:");
        System.out.println("Admin password (admin123): " + encoder.encode("admin123"));
        System.out.println("Customer password (pass123): " + encoder.encode("pass123"));
    }
}
