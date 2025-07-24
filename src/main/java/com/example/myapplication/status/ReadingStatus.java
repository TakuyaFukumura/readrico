package com.example.myapplication.status;

import lombok.Getter;

@Getter
public enum ReadingStatus {
    UNREAD("未読"),
    READING("読書中"),
    COMPLETED("読了"),
    PAUSED("中止");

    private final String displayName;

    ReadingStatus(String displayName) {
        this.displayName = displayName;
    }
}
