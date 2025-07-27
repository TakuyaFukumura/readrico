package com.example.myapplication.controller;

import com.example.myapplication.entity.ReadingRecord;
import com.example.myapplication.service.ReadingRecordService;
import com.example.myapplication.status.ReadingStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/reading-records")
public class ReadingRecordController {

    private static final Logger logger = LoggerFactory.getLogger(ReadingRecordController.class);

    private static final String STATUSES = "statuses";
    private static final String READING_RECORD = "readingRecord";
    private static final String REDIRECT = "redirect:/reading-records";

    private final ReadingRecordService readingRecordService;

    @Autowired
    public ReadingRecordController(ReadingRecordService readingRecordService) {
        this.readingRecordService = readingRecordService;
    }

    /**
     * 読書記録一覧画面
     */
    @GetMapping
    public String list(@RequestParam(defaultValue = "UNREAD") String status, Model model) {
        ReadingStatus readingStatus;
        try {
            readingStatus = ReadingStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            readingStatus = ReadingStatus.UNREAD;
        }

        model.addAttribute("readingRecords", readingRecordService.getReadingRecordsByStatus(readingStatus));
        model.addAttribute("currentStatusName", readingStatus.name());

        // 各ステータスの件数を追加
        model.addAttribute("unreadCount", readingRecordService.getCountByStatus(ReadingStatus.UNREAD));
        model.addAttribute("readingCount", readingRecordService.getCountByStatus(ReadingStatus.READING));
        model.addAttribute("completedCount", readingRecordService.getCountByStatus(ReadingStatus.COMPLETED));
        model.addAttribute("pausedCount", readingRecordService.getCountByStatus(ReadingStatus.PAUSED));

        return "reading-records/list";
    }

    /**
     * 読書記録詳細・編集画面
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Optional<ReadingRecord> readingRecord = readingRecordService.getReadingRecordById(id);

        if (readingRecord.isEmpty()) {
            return REDIRECT;
        }

        // 進捗率の計算
        int progressPercent = readingRecordService.getProgressPercent(
                readingRecord.get().getTotalPages(),
                readingRecord.get().getCurrentPage()
        );
        model.addAttribute("progressPercent", progressPercent);

        model.addAttribute(READING_RECORD, readingRecord.get());
        model.addAttribute(STATUSES, ReadingStatus.values());
        return "reading-records/detail";
    }

    /**
     * 読書記録新規登録画面
     */
    @GetMapping("/new")
    public String newRecord(Model model) {
        model.addAttribute(READING_RECORD, new ReadingRecord());
        model.addAttribute(STATUSES, ReadingStatus.values());
        return "reading-records/form";
    }

    /**
     * 読書記録編集画面
     */
    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        Optional<ReadingRecord> readingRecord = readingRecordService.getReadingRecordById(id);

        if (readingRecord.isEmpty()) {
            return REDIRECT;
        }

        model.addAttribute(READING_RECORD, readingRecord.get());
        model.addAttribute(STATUSES, ReadingStatus.values());
        return "reading-records/form";
    }

    /**
     * 読書記録保存処理
     */
    @PostMapping("/save")
    public String save(@ModelAttribute ReadingRecord readingRecord, RedirectAttributes redirectAttributes) {
        try {
            ReadingRecord saved = readingRecordService.saveReadingRecord(readingRecord);
            redirectAttributes.addFlashAttribute("message", "読書記録を保存しました。");
            return REDIRECT + "/" + saved.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "保存中にエラーが発生しました。");
            if (readingRecord.getId() != null) {
                return REDIRECT + "/" + readingRecord.getId() + "/edit";
            } else {
                return REDIRECT + "/new";
            }
        }
    }

    /**
     * 読書記録削除処理
     */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            readingRecordService.deleteReadingRecord(id);
            redirectAttributes.addFlashAttribute("message", "読書記録を削除しました。");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "削除中にエラーが発生しました。");
        }
        return REDIRECT;
    }

    /**
     * 読書記録CSV出力処理
     */
    @GetMapping("/export-csv")
    public ResponseEntity<byte[]> exportCsv() {
        try {
            byte[] csvData = readingRecordService.exportToCsv();
            String fileName = readingRecordService.generateCsvFileName();

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                    .body(csvData);
        } catch (IOException e) {
            // エラーメッセージをログに出力
            logger.error("CSVファイルの出力中にエラーが発生しました: {}", e.getMessage(), e);

            // ユーザー向けのエラーメッセージを返す
            String errorMessage = "CSVファイルの出力中にエラーが発生しました。";
            return ResponseEntity.internalServerError()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(errorMessage.getBytes());
        }
    }

    /**
     * CSVアップロード画面
     */
    @GetMapping("/upload")
    public String upload() {
        return "reading-records/upload";
    }

    /**
     * CSVファイル確認処理
     */
    @PostMapping("/upload/confirm")
    public String uploadConfirm(@RequestParam("csvFile") MultipartFile csvFile, 
                                Model model, 
                                RedirectAttributes redirectAttributes) {
        try {
            // ファイルの基本チェック
            if (csvFile.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "ファイルが選択されていません。");
                return "redirect:/reading-records/upload";
            }

            if (!csvFile.getOriginalFilename().toLowerCase().endsWith(".csv")) {
                redirectAttributes.addFlashAttribute("error", "CSVファイルを選択してください。");
                return "redirect:/reading-records/upload";
            }

            // CSVファイルを解析
            List<ReadingRecord> records = readingRecordService.parseCsvFile(csvFile);

            if (records.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "有効なデータが見つかりませんでした。");
                return "redirect:/reading-records/upload";
            }

            // CSVデータをBase64エンコードして保持
            String csvData = Base64.getEncoder().encodeToString(csvFile.getBytes());

            model.addAttribute("readingRecords", records);
            model.addAttribute("csvData", csvData);

            return "reading-records/upload-confirm";

        } catch (IOException e) {
            logger.error("CSVファイルの読み込み中にエラーが発生しました: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "CSVファイルの読み込み中にエラーが発生しました。");
            return "redirect:/reading-records/upload";
        } catch (Exception e) {
            logger.error("予期しないエラーが発生しました: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "予期しないエラーが発生しました。");
            return "redirect:/reading-records/upload";
        }
    }

    /**
     * CSV一括登録実行処理
     */
    @PostMapping("/upload/save")
    public String uploadSave(@RequestParam("csvData") String csvData, 
                             RedirectAttributes redirectAttributes) {
        try {
            // Base64デコードしてCSVデータを復元
            byte[] decodedData = Base64.getDecoder().decode(csvData);
            
            // 一時的なMultipartFileを作成して再解析
            MultipartFile tempFile = new TempMultipartFile(decodedData, "temp.csv", "text/csv");
            List<ReadingRecord> records = readingRecordService.parseCsvFile(tempFile);

            if (records.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "有効なデータが見つかりませんでした。");
                return "redirect:/reading-records/upload";
            }

            // 一括保存
            List<ReadingRecord> savedRecords = readingRecordService.saveReadingRecords(records);

            redirectAttributes.addFlashAttribute("message", 
                savedRecords.size() + "件の読書記録を登録しました。");
            return REDIRECT;

        } catch (Exception e) {
            logger.error("CSV一括登録中にエラーが発生しました: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "一括登録中にエラーが発生しました。");
            return "redirect:/reading-records/upload";
        }
    }

    /**
     * 一時的なMultipartFileの実装
     */
    private static class TempMultipartFile implements MultipartFile {
        private final byte[] content;
        private final String name;
        private final String contentType;

        public TempMultipartFile(byte[] content, String name, String contentType) {
            this.content = content;
            this.name = name;
            this.contentType = contentType;
        }

        @Override
        public String getName() { return name; }

        @Override
        public String getOriginalFilename() { return name; }

        @Override
        public String getContentType() { return contentType; }

        @Override
        public boolean isEmpty() { return content.length == 0; }

        @Override
        public long getSize() { return content.length; }

        @Override
        public byte[] getBytes() { return content; }

        @Override
        public java.io.InputStream getInputStream() {
            return new java.io.ByteArrayInputStream(content);
        }

        @Override
        public void transferTo(java.io.File dest) throws IOException {
            throw new UnsupportedOperationException();
        }
    }
}
