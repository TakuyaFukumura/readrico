package com.example.myapplication.status

import spock.lang.Specification

/**
 * ReadingStatusの列挙型テストクラス
 * 列挙型の値と表示名の対応をテストする
 */
class ReadingStatusSpec extends Specification {

    def "ReadingStatus - 全ての列挙値が定義されている"() {
        expect: "4つの読書ステータスが定義されている"
        ReadingStatus.values().length == 4
        ReadingStatus.values() == [
                ReadingStatus.UNREAD,
                ReadingStatus.READING,
                ReadingStatus.COMPLETED,
                ReadingStatus.PAUSED
        ]
    }

    def "ReadingStatus - 各ステータスの表示名が正しく設定されている"() {
        expect: "ステータスと表示名が正しく対応している"
        status.displayName == expectedDisplayName

        where: "全てのステータスとその表示名"
        status                  | expectedDisplayName
        ReadingStatus.UNREAD    | "未読"
        ReadingStatus.READING   | "読書中"
        ReadingStatus.COMPLETED | "読了"
        ReadingStatus.PAUSED    | "中止"
    }

    def "ReadingStatus - valueOf で文字列から列挙値を取得できる"() {
        expect: "文字列からステータスを取得できる"
        ReadingStatus.valueOf(statusName) == expectedStatus

        where: "ステータス名と期待されるステータス"
        statusName  | expectedStatus
        "UNREAD"    | ReadingStatus.UNREAD
        "READING"   | ReadingStatus.READING
        "COMPLETED" | ReadingStatus.COMPLETED
        "PAUSED"    | ReadingStatus.PAUSED
    }

    def "ReadingStatus - 無効な文字列でvalueOfを呼ぶと例外が発生する"() {
        when: "存在しないステータス名でvalueOfを呼ぶ"
        ReadingStatus.valueOf("INVALID_STATUS")

        then: "IllegalArgumentExceptionが発生する"
        thrown(IllegalArgumentException)
    }

    def "ReadingStatus - name() メソッドでステータス名を取得できる"() {
        expect: "ステータス名が正しく取得できる"
        status.name() == expectedName

        where: "ステータスとその名前"
        status                  | expectedName
        ReadingStatus.UNREAD    | "UNREAD"
        ReadingStatus.READING   | "READING"
        ReadingStatus.COMPLETED | "COMPLETED"
        ReadingStatus.PAUSED    | "PAUSED"
    }

    def "ReadingStatus - ordinal() メソッドで順序を取得できる"() {
        expect: "定義順序が正しく取得できる"
        status.ordinal() == expectedOrdinal

        where: "ステータスとその定義順序"
        status                  | expectedOrdinal
        ReadingStatus.UNREAD    | 0
        ReadingStatus.READING   | 1
        ReadingStatus.COMPLETED | 2
        ReadingStatus.PAUSED    | 3
    }

    def "ReadingStatus - equals と hashCode の動作を確認する"() {
        given: "同じステータスの参照"
        def status1 = ReadingStatus.READING
        def status2 = ReadingStatus.READING
        def status3 = ReadingStatus.COMPLETED

        expect: "同じステータスは等しく、異なるステータスは等しくない"
        status1 == status2
        status1 != status3
        status1.hashCode() == status2.hashCode()
        status1.hashCode() != status3.hashCode()
    }

    def "ReadingStatus - toString の動作を確認する"() {
        expect: "toStringがステータス名を返す"
        status.toString() == expectedToString

        where: "ステータスとtoStringの結果"
        status                  | expectedToString
        ReadingStatus.UNREAD    | "UNREAD"
        ReadingStatus.READING   | "READING"
        ReadingStatus.COMPLETED | "COMPLETED"
        ReadingStatus.PAUSED    | "PAUSED"
    }
}
