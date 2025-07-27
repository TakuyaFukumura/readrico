package com.example.myapplication.service;

import com.example.myapplication.entity.ReadingRecord;
import com.example.myapplication.repository.ReadingRecordRepository;
import com.example.myapplication.status.ReadingStatus;
import com.opencsv.CSVWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
     * 上限は100%です。
     * <p>
     * 以下のいずれかの場合は0を返します。
     * <ul>
     *   <li>総数がnullか0以下</li>
     *   <li>現在値がnullか0未満</li>
     * </ul>
     *
     * @param total   総数
     * @param current 現在値
     * @return 進捗率（％）。計算できない場合は0
     */
    public int getProgressPercent(Integer total, Integer current) {
        if (total == null || current == null || total <= 0 || current < 0) {
            return 0;
        }
        if (current >= total) {
            return 100;
        }

        // 現在値 × 100 ÷ 総数 = 進捗率（単位：%）
        return BigDecimal.valueOf(current)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), RoundingMode.HALF_UP)
                .intValue();
    }

    /**
     * 全ての読書記録を取得
     */
    public List<ReadingRecord> getAllReadingRecords() {
        log.info("getAllReadingRecords was called");
        return readingRecordRepository.findAll();
    }

    /**
     * 読書記録をCSV形式でエクスポート
     *
     * @return CSVデータのバイト配列
     * @throws IOException CSV生成時にエラーが発生した場合
     */
    public byte[] exportToCsv() throws IOException {
        log.info("exportToCsv was called");

        List<ReadingRecord> records = getAllReadingRecords();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
             CSVWriter csvWriter = new CSVWriter(writer)) {

            // ヘッダー行を設定
            String[] headers = {"ID", "タイトル", "著者", "読書状態", "現在ページ", "総ページ数",
                    "概要", "感想", "作成日時", "更新日時"};
            csvWriter.writeNext(headers);

            // データ行を出力
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            for (ReadingRecord readingRecord : records) {
                String[] data = {
                        readingRecord.getId() != null ? readingRecord.getId().toString() : "",
                        readingRecord.getTitle() != null ? readingRecord.getTitle() : "",
                        readingRecord.getAuthor() != null ? readingRecord.getAuthor() : "",
                        readingRecord.getReadingStatus() != null ? readingRecord.getReadingStatus().getDisplayName() : "",
                        readingRecord.getCurrentPage() != null ? readingRecord.getCurrentPage().toString() : "",
                        readingRecord.getTotalPages() != null ? readingRecord.getTotalPages().toString() : "",
                        readingRecord.getSummary() != null ? readingRecord.getSummary() : "",
                        readingRecord.getThoughts() != null ? readingRecord.getThoughts() : "",
                        readingRecord.getCreatedAt() != null ? readingRecord.getCreatedAt().format(formatter) : "",
                        readingRecord.getUpdatedAt() != null ? readingRecord.getUpdatedAt().format(formatter) : ""
                };
                csvWriter.writeNext(data);
            }
        }

        return outputStream.toByteArray();
    }

    /**
     * CSV出力用のファイル名を生成
     *
     * @return ファイル名（例：reading-records_20241127_143022.csv）
     */
    public String generateCsvFileName() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String timestamp = LocalDateTime.now().format(formatter);
        return "reading-records_" + timestamp + ".csv";
    }
}
