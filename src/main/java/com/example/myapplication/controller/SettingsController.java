package com.example.myapplication.controller;

import com.example.myapplication.service.ReadingRecordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/settings")
public class SettingsController {

    private static final Logger logger = LoggerFactory.getLogger(SettingsController.class);

    private final ReadingRecordService readingRecordService;

    @Autowired
    public SettingsController(ReadingRecordService readingRecordService) {
        this.readingRecordService = readingRecordService;
    }

    /**
     * 設定画面
     */
    @GetMapping
    public String settings() {
        return "settings";
    }

    /**
     * 全読書記録削除処理
     */
    @PostMapping("/delete-all")
    public String deleteAll(RedirectAttributes redirectAttributes) {
        try {
            readingRecordService.deleteAllReadingRecords();
            redirectAttributes.addFlashAttribute("message", "全ての読書記録を削除しました。");
        } catch (Exception e) {
            logger.error("一括削除中にエラーが発生しました: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "一括削除中にエラーが発生しました。");
        }
        return "redirect:/settings";
    }
}