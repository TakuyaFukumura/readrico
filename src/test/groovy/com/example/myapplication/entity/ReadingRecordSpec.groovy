package com.example.myapplication.entity

import com.example.myapplication.status.ReadingStatus
import spock.lang.Specification

import java.time.LocalDateTime

/**
 * ReadingRecordエンティティのテストクラス
 * エンティティの基本的な動作をテストする
 */
class ReadingRecordSpec extends Specification {

    def "ReadingRecord - デフォルト値が正しく設定される"() {
        when: "新しいReadingRecordを作成"
        def record = new ReadingRecord()

        then: "デフォルト値が設定される"
        record.readingStatus == ReadingStatus.UNREAD
        record.currentPage == 0
        record.id == null
        record.title == null
        record.author == null
        record.totalPages == null
        record.summary == null
        record.thoughts == null
        record.createdAt == null
        record.updatedAt == null
    }

    def "ReadingRecord - 全てのプロパティを設定できる"() {
        given: "設定する値"
        def now = LocalDateTime.now()

        when: "ReadingRecordのプロパティを設定"
        def record = new ReadingRecord()
        record.id = 1L
        record.title = "テストタイトル"
        record.author = "テスト著者"
        record.readingStatus = ReadingStatus.READING
        record.currentPage = 50
        record.totalPages = 200
        record.summary = "要約テキスト"
        record.thoughts = "感想テキスト"
        record.createdAt = now
        record.updatedAt = now

        then: "設定した値が正しく保持される"
        record.id == 1L
        record.title == "テストタイトル"
        record.author == "テスト著者"
        record.readingStatus == ReadingStatus.READING
        record.currentPage == 50
        record.totalPages == 200
        record.summary == "要約テキスト"
        record.thoughts == "感想テキスト"
        record.createdAt == now
        record.updatedAt == now
    }

    def "ReadingRecord - 読書ステータスの変更をテストする"() {
        given: "読書記録"
        def record = new ReadingRecord()
        record.title = "テスト本"

        when: "読書ステータスを変更"
        record.readingStatus = status

        then: "ステータスが正しく設定される"
        record.readingStatus == status

        where: "全ての読書ステータス"
        status << [
                ReadingStatus.UNREAD,
                ReadingStatus.READING,
                ReadingStatus.COMPLETED,
                ReadingStatus.PAUSED
        ]
    }

    def "ReadingRecord - 進捗ページ数の境界値テストをおこなう"() {
        given: "読書記録"
        def record = new ReadingRecord()
        record.totalPages = 100

        when: "現在ページ数を設定"
        record.currentPage = currentPage

        then: "設定した値が保持される"
        record.currentPage == currentPage

        where: "様々な現在ページ数"
        currentPage << [0, 1, 50, 99, 100, 150]  // 境界値や異常値も含む
    }

    def "ReadingRecord - equals と hashCode の動作を確認する"() {
        given: "同じ内容の読書記録を2つ作成"
        def record1 = new ReadingRecord()
        record1.id = 1L
        record1.title = "同じタイトル"
        record1.author = "同じ著者"

        def record2 = new ReadingRecord()
        record2.id = 1L
        record2.title = "同じタイトル"
        record2.author = "同じ著者"

        expect: "Lombokによって生成されたequalsとhashCodeが動作する"
        record1 == record2
        record1.hashCode() == record2.hashCode()
    }

    def "ReadingRecord - toString の動作を確認する"() {
        given: "読書記録"
        def record = new ReadingRecord()
        record.id = 1L
        record.title = "テストタイトル"
        record.author = "テスト著者"
        record.readingStatus = ReadingStatus.READING

        when: "toStringを呼び出し"
        def result = record.toString()

        then: "適切な文字列表現が返される"
        result.contains("ReadingRecord")
        result.contains("id=1")
        result.contains("title=テストタイトル")
        result.contains("author=テスト著者")
        result.contains("readingStatus=READING")
    }
}
