package com.dev.tagashira.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataInitializationService implements CommandLineRunner {
    
    private final RoleService roleService;

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting data initialization...");
        initializeDefaultRoles();
        log.info("Data initialization completed.");
    }

    private void initializeDefaultRoles() {
        try {
            roleService.initializeDefaultRoles();
            log.info("Default roles initialized successfully");
        } catch (Exception e) {
            log.error("Error initializing default roles: {}", e.getMessage(), e);
        }
    }
} 