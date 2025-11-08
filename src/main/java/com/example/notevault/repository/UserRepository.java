package com.example.notevault.repository;

import com.example.notevault.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // NEW: Import Query
import org.springframework.stereotype.Repository;

import java.time.Instant; // NEW: Import Instant
import java.util.List; // NEW: Import List
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);

    // NEW: Query to count new users grouped by date (for the last 30 days)
    @Query("SELECT FUNCTION('DATE_TRUNC', 'day', u.createdAt) as date, COUNT(u) as count " +
           "FROM User u WHERE u.createdAt >= :startDate " +
           "GROUP BY date ORDER BY date ASC")
    List<Object[]> countUsersByDate(Instant startDate);
}