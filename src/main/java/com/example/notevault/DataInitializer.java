package com.example.notevault; // Make sure this package matches your project

import com.example.notevault.model.ERole;
import com.example.notevault.model.Role;
import com.example.notevault.repository.RoleRepository;
import jakarta.annotation.PostConstruct; // Import PostConstruct
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer {

    @Autowired
    private RoleRepository roleRepository;

    @PostConstruct // This makes the method run automatically after the application starts
    public void initializeRoles() {
        // Check if ROLE_USER exists, if not, create it
        if (roleRepository.findByName(ERole.ROLE_USER).isEmpty()) {
            roleRepository.save(new Role(ERole.ROLE_USER));
            System.out.println("Initialized ROLE_USER");
        }

        // Check if ROLE_ADMIN exists, if not, create it
        if (roleRepository.findByName(ERole.ROLE_ADMIN).isEmpty()) {
            roleRepository.save(new Role(ERole.ROLE_ADMIN));
            System.out.println("Initialized ROLE_ADMIN");
        }
    }
}