package com.example.notevault.repository;

import com.example.notevault.model.ERole;
import com.example.notevault.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
  Optional<Role> findByName(ERole name);
}