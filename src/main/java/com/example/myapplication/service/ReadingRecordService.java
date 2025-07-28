package com.example.myapplication.service;

import com.example.myapplication.entity.ReadingRecord;
import com.example.myapplication.repository.ReadingRecordRepository;
import com.example.myapplication.status.ReadingStatus;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
     * 読書記録をCSV形式で出力
     *
     * @return CSVデータのバイト配列
     * @throws IOException CSV生成時にエラーが発生した場合
     */
    public byte[] exportToCsv() throws IOException {

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
                String[] data = convertToCsvRow(readingRecord, formatter);
                csvWriter.writeNext(data);
            }
        }

        return outputStream.toByteArray();
    }

    /**
     * 読書記録エンティティをCSV1行分の文字列配列に変換します。
     * <p>
     * 値がnullの場合は空文字列を入れます。
     *
     * @param readingRecord 変換対象の読書記録
     * @param formatter     日付フォーマット
     * @return CSV出力用の文字列配列
     */
    private String[] convertToCsvRow(ReadingRecord readingRecord, DateTimeFormatter formatter) {
        return new String[]{
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

    /**
     * CSVファイルを解析して読書記録の一覧を返す
     *
     * @param csvFile アップロードされたCSVファイル
     * @return 解析された読書記録の一覧
     * @throws IOException CSV読み込み時にエラーが発生した場合
     */
    public List<ReadingRecord> parseCsvFile(MultipartFile csvFile) throws IOException {
        log.info("parseCsvFile was called with filename: {}", csvFile.getOriginalFilename());

        List<ReadingRecord> records = new ArrayList<>();

        try (InputStreamReader reader = new InputStreamReader(csvFile.getInputStream(), StandardCharsets.UTF_8);
             CSVReader csvReader = new CSVReader(reader)) {

            List<String[]> allData = csvReader.readAll();

            if (allData.isEmpty()) {
                return records;
            }

            // ヘッダー行をスキップ（最初の行が項目名の場合）
            int startIndex = 0;
            String[] firstRow = allData.get(0);
            if (isHeaderRow(firstRow)) {
                startIndex = 1;
            }

            // データ行を処理
            for (int i = startIndex; i < allData.size(); i++) {
                String[] data = allData.get(i);
                try {
                    ReadingRecord readingRecord = parseCsvRow(data);
                    if (readingRecord != null) {
                        records.add(readingRecord);
                    }
                } catch (Exception e) {
                    log.warn("Failed to parse CSV row {}: {}", i + 1, e.getMessage());
                    // 個別行のエラーは警告ログに留めて処理を続行
                }
            }
        } catch (CsvException e) {
            log.error("CSV parsing error: {}", e.getMessage(), e);
            throw new IOException("CSVファイルの解析中にエラーが発生しました: " + e.getMessage(), e);
        }

        log.info("Successfully parsed {} records from CSV", records.size());
        return records;
    }

    /**
     * CSV行がヘッダー行かどうかを判定
     */
    private boolean isHeaderRow(String[] row) {
        if (row.length == 0) return false;

        // 最初の列がIDの場合（数値でない場合）、ヘッダー行と判定
        String firstCol = row[0].trim();
        if (firstCol.equals("ID") || firstCol.equals("id") || firstCol.equals("Id")) {
            return true;
        }

        // 最初の列が数値でない場合もヘッダー行の可能性が高い
        try {
            Long.parseLong(firstCol);
            return false; // 数値の場合はデータ行
        } catch (NumberFormatException e) {
            return true; // 数値でない場合はヘッダー行
        }
    }

    /**
     * CSV1行分のデータを読書記録エンティティに変換
     */
    private ReadingRecord parseCsvRow(String[] data) {
        if (data.length < 1) { // 最低限タイトルが必要
            return null;
        }

        ReadingRecord readingRecord = new ReadingRecord();
        int index = 0;

        // 最初の列がIDかどうかを判定（数値の場合はID、そうでなければタイトル）
        boolean hasIdColumn = false;
        if (data.length > 0) {
            try {
                Long.parseLong(data[0].trim());
                hasIdColumn = true; // 最初の列が数値の場合はID列とみなす
            } catch (NumberFormatException e) {
                hasIdColumn = false; // 数値でない場合はタイトル列とみなす
            }
        }

        // IDがある場合はスキップ
        if (hasIdColumn && data.length > index) {
            index++; // ID列をスキップ
        }

        // タイトル（必須）
        if (data.length > index && !data[index].trim().isEmpty()) {
            readingRecord.setTitle(data[index].trim());
            index++;
        } else {
            return null; // タイトルが空の場合は無効なレコード
        }

        // 著者
        if (data.length > index) {
            readingRecord.setAuthor(data[index].trim().isEmpty() ? null : data[index].trim());
            index++;
        }

        // 読書状態
        if (data.length > index) {
            readingRecord.setReadingStatus(parseReadingStatus(data[index].trim()));
            index++;
        } else {
            readingRecord.setReadingStatus(ReadingStatus.UNREAD);
        }

        // 現在ページ
        if (data.length > index) {
            readingRecord.setCurrentPage(parseInteger(data[index].trim(), 0));
            index++;
        } else {
            readingRecord.setCurrentPage(0);
        }

        // 総ページ数
        if (data.length > index) {
            readingRecord.setTotalPages(parseInteger(data[index].trim(), null));
            index++;
        }

        // 概要
        if (data.length > index) {
            readingRecord.setSummary(data[index].trim().isEmpty() ? null : data[index].trim());
            index++;
        }

        // 感想
        if (data.length > index) {
            readingRecord.setThoughts(data[index].trim().isEmpty() ? null : data[index].trim());
        }

        return readingRecord;
    }

    /**
     * 読書状態の文字列を読書状態列挙型に変換
     */
    private ReadingStatus parseReadingStatus(String statusStr) {
        if (statusStr == null || statusStr.trim().isEmpty()) {
            return ReadingStatus.UNREAD;
        }

        // 表示名での変換を試行
        for (ReadingStatus status : ReadingStatus.values()) {
            if (status.getDisplayName().equals(statusStr.trim())) {
                return status;
            }
        }

        // 英語名での変換を試行
        try {
            return ReadingStatus.valueOf(statusStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Unknown reading status: {}, defaulting to UNREAD", statusStr);
            return ReadingStatus.UNREAD;
        }
    }

    /**
     * 文字列を整数に変換（失敗時はデフォルト値を返す）
     */
    private Integer parseInteger(String str, Integer defaultValue) {
        if (str == null || str.trim().isEmpty()) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(str.trim());
        } catch (NumberFormatException e) {
            log.warn("Failed to parse integer: {}, using default: {}", str, defaultValue);
            return defaultValue;
        }
    }

    /**
     * 読書記録のリストを一括保存
     */
    public List<ReadingRecord> saveReadingRecords(List<ReadingRecord> records) {
        log.info("saveReadingRecords was called with {} records", records.size());

        LocalDateTime now = LocalDateTime.now();

        // 各レコードに作成日時・更新日時を設定
        for (ReadingRecord readingRecord : records) {
            readingRecord.setCreatedAt(now);
            readingRecord.setUpdatedAt(now);
        }

        return readingRecordRepository.saveAll(records);
    }
}
