package com.example.myapplication.service

import com.example.myapplication.entity.ReadingRecord
import com.example.myapplication.repository.ReadingRecordRepository
import com.example.myapplication.status.ReadingStatus
import spock.lang.Specification
import spock.lang.Subject

/**
 * ReadingRecordServiceのテストクラス
 * Spockフレームワークを使用してサービス層のビジネスロジックをテストする
 */
class ReadingRecordServiceSpec extends Specification {

    // テスト対象のサービス
    @Subject
    ReadingRecordService readingRecordService

    // モックオブジェクト
    ReadingRecordRepository mockRepository = Mock()

    def setup() {
        // テスト対象のサービスを初期化
        readingRecordService = new ReadingRecordService(mockRepository)
    }

    def "getReadingRecordsByStatus - 指定されたステータスの読書記録一覧を取得する"() {
        given: "期待される読書記録のリスト"
        def expectedRecords = [
                new ReadingRecord(id: 1L, title: "テスト本1", readingStatus: ReadingStatus.READING),
                new ReadingRecord(id: 2L, title: "テスト本2", readingStatus: ReadingStatus.READING)
        ]

        when: "読書記録一覧を取得"
        def result = readingRecordService.getReadingRecordsByStatus(ReadingStatus.READING)

        then: "期待される結果が返される"
        1 * mockRepository.findByReadingStatusOrderByUpdatedAtDesc(ReadingStatus.READING) >> expectedRecords
        result == expectedRecords
    }

    def "getCountByStatus - 指定されたステータスの件数を取得する"() {
        when: "件数を取得"
        def result = readingRecordService.getCountByStatus(ReadingStatus.COMPLETED)

        then: "期待される件数が返される"
        1 * mockRepository.countByReadingStatus(ReadingStatus.COMPLETED) >> 5L
        result == 5L
    }

    def "getReadingRecordById - 存在するIDで読書記録を取得する"() {
        given: "存在する読書記録"
        def expectedRecord = new ReadingRecord(id: 1L, title: "テスト本")

        when: "IDで読書記録を取得"
        def result = readingRecordService.getReadingRecordById(1L)

        then: "読書記録が返される"
        1 * mockRepository.findById(1L) >> Optional.of(expectedRecord)
        result.isPresent()
        result.get() == expectedRecord
    }

    def "getReadingRecordById - 存在しないIDで読書記録を取得する"() {
        when: "存在しないIDで読書記録を取得"
        def result = readingRecordService.getReadingRecordById(999L)

        then: "空のOptionalが返される"
        1 * mockRepository.findById(999L) >> Optional.empty()
        result.isEmpty()
    }

    def "saveReadingRecord - 新規読書記録を保存する"() {
        given: "新規読書記録（IDがnull）"
        def newRecord = new ReadingRecord(title: "新しい本", author: "著者名")
        def savedRecord = new ReadingRecord(id: 1L, title: "新しい本", author: "著者名")

        when: "読書記録を保存"
        def result = readingRecordService.saveReadingRecord(newRecord)

        then: "作成日時と更新日時が設定され、保存される"
        1 * mockRepository.save(newRecord) >> savedRecord
        result == savedRecord
        newRecord.createdAt != null
        newRecord.updatedAt != null
    }

    def "saveReadingRecord - 既存読書記録を更新する"() {
        given: "既存読書記録（IDが設定済み）"
        def existingRecord = new ReadingRecord(id: 1L, title: "既存の本", author: "著者名")
        def savedRecord = new ReadingRecord(id: 1L, title: "既存の本", author: "著者名")

        when: "読書記録を保存"
        def result = readingRecordService.saveReadingRecord(existingRecord)

        then: "更新日時のみが設定され、作成日時は変更されない"
        1 * mockRepository.save(existingRecord) >> savedRecord
        result == savedRecord
        existingRecord.createdAt == null  // 新規作成時のみ設定される
        existingRecord.updatedAt != null
    }

    def "deleteReadingRecord - 読書記録を削除する"() {
        when: "読書記録を削除"
        readingRecordService.deleteReadingRecord(1L)

        then: "リポジトリの削除メソッドが呼ばれる"
        1 * mockRepository.deleteById(1L)
    }

    def "getProgressPercent - 様々な条件での進捗率計算をテストする"() {
        expect: "進捗率が正しく計算される"
        readingRecordService.getProgressPercent(total, current) == expected

        where: "パラメータ化テスト - 様々な条件をテスト"
        total | current | expected | description
        100   | 50      | 50       | "正常ケース: 50%"
        100   | 0       | 0        | "読書開始前: 0%"
        100   | 100     | 100      | "読書完了: 100%"
        100   | 150     | 100      | "現在値が総数を超える場合: 100%"
        null  | 50      | 0        | "総数がnull: 0%"
        100   | null    | 0        | "現在値がnull: 0%"
        0     | 50      | 0        | "総数が0: 0%"
        -10   | 50      | 0        | "総数が負数: 0%"
        100   | -5      | 0        | "現在値が負数: 0%"
        3     | 1       | 33       | "端数の四捨五入テスト: 33%"
        3     | 2       | 67       | "端数の四捨五入テスト: 67%"
        7     | 1       | 14       | "小数点以下切り捨て: 14%"
        7     | 5       | 71       | "小数点以下切り上げ: 71%"
    }

    def "getProgressPercent - 複雑な計算パターンをテストする"() {
        given: "複雑な値での進捗率計算"
        def total = totalPages
        def current = currentPage

        when: "進捗率を計算"
        def result = readingRecordService.getProgressPercent(total, current)

        then: "期待される進捗率が返される"
        result == expectedPercent

        where: "大きな数値や特殊なケース"
        totalPages | currentPage | expectedPercent
        1000       | 333         | 33
        1000       | 334         | 33
        1000       | 335         | 34  // 四捨五入
        999        | 1           | 0
        999        | 500         | 50
        1          | 0           | 0
        1          | 1           | 100
        2          | 1           | 50
    }

    def "getAllReadingRecords - 全ての読書記録を取得する"() {
        given: "すべての読書記録のリスト"
        def allRecords = [
                new ReadingRecord(id: 1L, title: "テスト本1", readingStatus: ReadingStatus.READING),
                new ReadingRecord(id: 2L, title: "テスト本2", readingStatus: ReadingStatus.COMPLETED)
        ]

        when: "全ての読書記録を取得"
        def result = readingRecordService.getAllReadingRecords()

        then: "期待される結果が返される"
        1 * mockRepository.findAll() >> allRecords
        result == allRecords
    }

    def "exportToCsv - 読書記録をCSV形式で出力する"() {
        given: "テスト用の読書記録"
        def records = [
                new ReadingRecord(
                        id: 1L,
                        title: "テスト本1",
                        author: "テスト著者1",
                        readingStatus: ReadingStatus.READING,
                        currentPage: 50,
                        totalPages: 100,
                        summary: "概要1",
                        thoughts: "感想1",
                        createdAt: java.time.LocalDateTime.of(2024, 11, 27, 10, 0, 0),
                        updatedAt: java.time.LocalDateTime.of(2024, 11, 27, 11, 0, 0)
                )
        ]

        when: "CSV出力を実行"
        byte[] csvData = readingRecordService.exportToCsv()

        then: "CSVデータが正常に生成される"
        1 * mockRepository.findAll() >> records
        
        // CSVデータを文字列に変換して内容を検証
        String csvContent = new String(csvData, java.nio.charset.StandardCharsets.UTF_8)
        csvContent.contains("ID,タイトル,著者,読書状態,現在ページ,総ページ数,概要,感想,作成日時,更新日時")
        csvContent.contains("1,テスト本1,テスト著者1,読書中,50,100,概要1,感想1,2024-11-27 10:00:00,2024-11-27 11:00:00")
    }

    def "exportToCsv - 空の読書記録リストでもヘッダーのみのCSVが生成される"() {
        given: "空の読書記録リスト"
        def records = []

        when: "CSV出力を実行"
        byte[] csvData = readingRecordService.exportToCsv()

        then: "ヘッダーのみのCSVが生成される"
        1 * mockRepository.findAll() >> records
        
        String csvContent = new String(csvData, java.nio.charset.StandardCharsets.UTF_8)
        csvContent.contains("ID,タイトル,著者,読書状態,現在ページ,総ページ数,概要,感想,作成日時,更新日時")
        !csvContent.contains("1,") // データ行が含まれていない
    }

    def "generateCsvFileName - CSVファイル名が正しい形式で生成される"() {
        when: "CSVファイル名を生成"
        String fileName = readingRecordService.generateCsvFileName()

        then: "正しい形式のファイル名が生成される"
        fileName.startsWith("reading-records_")
        fileName.endsWith(".csv")
        fileName.matches("reading-records_\\d{8}_\\d{6}\\.csv")
    }
}
