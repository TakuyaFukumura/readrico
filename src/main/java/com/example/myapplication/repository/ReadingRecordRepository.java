package com.example.myapplication.repository;

import com.example.myapplication.entity.ReadingRecord;
import com.example.myapplication.status.ReadingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReadingRecordRepository extends JpaRepository<ReadingRecord, Long> {

    List<ReadingRecord> findByReadingStatusOrderByUpdatedAtDesc(ReadingStatus readingStatus);

    long countByReadingStatus(ReadingStatus readingStatus);
}
