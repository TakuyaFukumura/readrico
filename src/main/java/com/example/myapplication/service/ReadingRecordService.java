package com.example.myapplication.service;

import com.example.myapplication.entity.ReadingRecord;
import com.example.myapplication.entity.ReadingRecord.ReadingStatus;
import com.example.myapplication.repository.ReadingRecordRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ReadingRecordService {

    private final ReadingRecordRepository readingRecordRepository;

    public ReadingRecordService(ReadingRecordRepository readingRecordRepository) {
        this.readingRecordRepository = readingRecordRepository;
    }

    /**
     * 読書状態ごとの読書記録一覧を取得
     */
    public List<ReadingRecord> getReadingRecordsByStatus(ReadingStatus status) {
        log.info("getReadingRecordsByStatus was called with status: {}", status);
        return readingRecordRepository.findByReadingStatusOrderByUpdatedAtDesc(status);
    }

    /**
     * 読書状態ごとの件数を取得
     */
    public long getCountByStatus(ReadingStatus status) {
        return readingRecordRepository.countByReadingStatus(status);
    }

    /**
     * IDで読書記録を取得
     */
    public Optional<ReadingRecord> getReadingRecordById(Long id) {
        log.info("getReadingRecordById was called with id: {}", id);
        return readingRecordRepository.findById(id);
    }

    /**
     * 読書記録を保存
     */
    public ReadingRecord saveReadingRecord(ReadingRecord readingRecord) {
        log.info("saveReadingRecord was called");
        LocalDateTime now = LocalDateTime.now();

        if (readingRecord.getId() == null) {
            readingRecord.setCreatedAt(now);
        }
        readingRecord.setUpdatedAt(now);

        return readingRecordRepository.save(readingRecord);
    }

    /**
     * 読書記録を削除
     */
    public void deleteReadingRecord(Long id) {
        log.info("deleteReadingRecord was called with id: {}", id);
        readingRecordRepository.deleteById(id);
    }

    /**
     * 総数と現在値から進捗率（％）を計算して返します。
     * <p>
     * 引数が不正な場合は0を返します。
     *
     * @param total 総数
     * @param current 現在値
     * @return 進捗率（％）。計算できない場合は0
     */
    public int getProgressPercent(Integer total, Integer current) {
        if (total == null || current == null || total <= 0 || current < 0) {
            return 0;
        }

        // 現在値 × 100 ÷ 総数 = 進捗率（単位：%）
        return BigDecimal.valueOf(current)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), RoundingMode.HALF_UP)
                .intValue();
    }
}
