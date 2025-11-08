package com.example.notevault;

// Make sure these import paths match your project structure
import com.example.notevault.model.ERole;
import com.example.notevault.model.Role;
import com.example.notevault.model.User;
import com.example.notevault.repository.RoleRepository;
import com.example.notevault.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Override
    public void run(String... args) throws Exception {
        // 1. Ensure all roles exist in the database
        if (roleRepository.findByName(ERole.ROLE_USER).isEmpty()) {
            roleRepository.save(new Role(ERole.ROLE_USER));
            System.out.println("Created ROLE_USER");
        }
        
        Role adminRole;
        if (roleRepository.findByName(ERole.ROLE_ADMIN).isEmpty()) {
            adminRole = roleRepository.save(new Role(ERole.ROLE_ADMIN));
            System.out.println("Created ROLE_ADMIN");
        } else {
            adminRole = roleRepository.findByName(ERole.ROLE_ADMIN).get();
        }

        // 2. Check if an admin user already exists
        if (userRepository.findByUsername("admin").isEmpty()) {
            // No user named "admin" found, let's create one
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setEmail("admin@notevault.com"); // Use a placeholder email
            adminUser.setPassword(encoder.encode("admin12345")); // <-- Your admin password

            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);
            adminUser.setRoles(roles);

            userRepository.save(adminUser);
            
            System.out.println("************************************************************");
            System.out.println("!!! CREATED DEFAULT ADMIN USER !!!");
            System.out.println("!!! Username: admin");
            System.out.println("!!! Password: admin12345");
            System.out.println("!!! PLEASE LOG IN AND CHANGE THIS PASSWORD IMMEDIATELY !!!");
            System.out.println("************************************************************");
        }
    }
}