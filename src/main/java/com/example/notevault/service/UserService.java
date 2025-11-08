package com.example.notevault.service;

import com.example.notevault.model.ERole;
import com.example.notevault.model.Role;
import com.example.notevault.model.User;
import com.example.notevault.payload.request.ChangePasswordRequest;
import com.example.notevault.repository.NoteRepository;
import com.example.notevault.repository.RoleRepository;
import com.example.notevault.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Existing changePassword method
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Error: Incorrect old password.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    // Existing updateUserRoles method
    @Transactional
    public User updateUserRoles(Long userId, Set<String> strRoles) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Error: User not found with id: " + userId));

        Set<Role> roles = new HashSet<>();

        if (strRoles == null || strRoles.isEmpty()) {
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Default role ROLE_USER not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(roleName -> {
                ERole roleEnum;
                switch (roleName.toUpperCase()) {
                    case "ADMIN":
                    case "ROLE_ADMIN":
                        roleEnum = ERole.ROLE_ADMIN;
                        break;
                    case "USER":
                    case "ROLE_USER":
                    default:
                        roleEnum = ERole.ROLE_USER;
                        break;
                }
                Role role = roleRepository.findByName(roleEnum)
                        .orElseThrow(() -> new RuntimeException("Error: Role '" + roleEnum + "' not found."));
                roles.add(role);
            });
        }

        user.setRoles(roles);
        return userRepository.save(user);
    }

    // Existing deleteUser method
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Error: User not found with id: " + userId));

        noteRepository.deleteByUserId(userId);
        userRepository.delete(user);
    }

    // NEW: Method to disable a user account
    @Transactional
    public User disableUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Error: User not found with id: " + userId));
        user.setEnabled(false);
        return userRepository.save(user);
    }

    // NEW: Method to enable a user account
    @Transactional
    public User enableUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Error: User not found with id: " + userId));
        user.setEnabled(true);
        return userRepository.save(user);
    }
}