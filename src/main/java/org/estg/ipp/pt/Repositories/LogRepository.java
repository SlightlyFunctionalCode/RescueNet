package org.estg.ipp.pt.Repositories;

import org.estg.ipp.pt.Classes.Enum.TagType;
import org.estg.ipp.pt.Classes.Log;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LogRepository extends JpaRepository<Log, Long> {

    // Find logs by a specific tag
    List<Log> findByTag(TagType tag);

    // Find logs within a date range
    @Query("SELECT l FROM Log l WHERE l.dateTime BETWEEN :startDate AND :endDate")
    List<Log> findByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    // Find logs by tag within a date range
    @Query("SELECT l FROM Log l WHERE l.tag = :tag AND l.dateTime BETWEEN :startDate AND :endDate")
    List<Log> findByTagAndDateRange(TagType tag, LocalDateTime startDate, LocalDateTime endDate);
}
