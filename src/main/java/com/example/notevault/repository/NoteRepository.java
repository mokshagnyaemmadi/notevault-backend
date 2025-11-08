package com.example.notevault.repository;

import com.example.notevault.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {

    // Finds notes for a user, sorted by pinned status first, then by last update time
    List<Note> findByUserIdOrderByPinnedDescUpdatedAtDesc(Long userId);

    long countByUserId(Long userId);

    // --- THIS IS THE UPDATED QUERY ---
    // We have removed the line: "LOWER(CAST(n.content AS string)) LIKE LOWER(CONCAT('%', :query, '%')) OR "
    // NoteRepository.java

    // NoteRepository.java

@Query(value = "SELECT * FROM notes n WHERE n.user_id = :userId AND " +
               "(:query IS NULL OR " +
               "LOWER(n.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
               "LOWER(n.content) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
               "LOWER(CAST(n.tags AS text)) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
               "(:tag IS NULL OR LOWER(CAST(n.tags AS text)) LIKE LOWER(CONCAT('%', :tag, '%')))" +
               "ORDER BY n.pinned DESC, n.updated_at DESC", 
       nativeQuery = true) 
List<Note> searchNotesByQueryAndTag(
    @Param("userId") Long userId,
    @Param("query") String query,
    @Param("tag") String tag
);

    @Transactional
    void deleteByUserId(Long userId);

    @Query("SELECT FUNCTION('DATE_TRUNC', 'day', n.createdAt) as date, COUNT(n) as count " +
           "FROM Note n WHERE n.createdAt >= :startDate " +
           "GROUP BY date ORDER BY date ASC")
    List<Object[]> countNotesByDate(Instant startDate);
}