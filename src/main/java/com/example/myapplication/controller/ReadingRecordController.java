package com.example.myapplication.controller;

import com.example.myapplication.entity.ReadingRecord;
import com.example.myapplication.service.ReadingRecordService;
import com.example.myapplication.status.ReadingStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/reading-records")
public class ReadingRecordController {

    private final ReadingRecordService readingRecordService;

    @Autowired
    public ReadingRecordController(ReadingRecordService readingRecordService) {
        this.readingRecordService = readingRecordService;
    }

    /**
     * 読書記録一覧画面
     */
    @GetMapping
    public String index(@RequestParam(defaultValue = "UNREAD") String status, Model model) {
        ReadingStatus readingStatus = ReadingStatus.valueOf(status);

        model.addAttribute("readingRecords", readingRecordService.getReadingRecordsByStatus(readingStatus));
        model.addAttribute("currentStatusName", readingStatus.name());

        // 各ステータスの件数を追加
        model.addAttribute("unreadCount", readingRecordService.getCountByStatus(ReadingStatus.UNREAD));
        model.addAttribute("readingCount", readingRecordService.getCountByStatus(ReadingStatus.READING));
        model.addAttribute("completedCount", readingRecordService.getCountByStatus(ReadingStatus.COMPLETED));
        model.addAttribute("pausedCount", readingRecordService.getCountByStatus(ReadingStatus.PAUSED));

        return "reading-records/index";
    }

    /**
     * 読書記録詳細・編集画面
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Optional<ReadingRecord> readingRecord = readingRecordService.getReadingRecordById(id);

        if (readingRecord.isEmpty()) {
            return "redirect:/reading-records";
        }

        // 進捗率の計算
        int progressPercent = readingRecordService.getProgressPercent(
                readingRecord.get().getTotalPages(),
                readingRecord.get().getCurrentPage()
        );
        model.addAttribute("progressPercent", progressPercent);

        model.addAttribute("readingRecord", readingRecord.get());
        model.addAttribute("statuses", ReadingStatus.values());
        return "reading-records/detail";
    }

    /**
     * 読書記録新規登録画面
     */
    @GetMapping("/new")
    public String newRecord(Model model) {
        model.addAttribute("readingRecord", new ReadingRecord());
        model.addAttribute("statuses", ReadingStatus.values());
        return "reading-records/form";
    }

    /**
     * 読書記録編集画面
     */
    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        Optional<ReadingRecord> readingRecord = readingRecordService.getReadingRecordById(id);

        if (readingRecord.isEmpty()) {
            return "redirect:/reading-records";
        }

        model.addAttribute("readingRecord", readingRecord.get());
        model.addAttribute("statuses", ReadingStatus.values());
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
            return "redirect:/reading-records/" + saved.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "保存中にエラーが発生しました。");
            return "redirect:/reading-records/new";
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
        return "redirect:/reading-records";
    }
}
