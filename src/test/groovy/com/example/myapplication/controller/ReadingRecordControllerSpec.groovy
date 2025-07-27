package com.example.myapplication.controller

import com.example.myapplication.entity.ReadingRecord
import com.example.myapplication.service.ReadingRecordService
import com.example.myapplication.status.ReadingStatus
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Specification
import spock.lang.Subject

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

/**
 * ReadingRecordControllerのテストクラス
 * Spring MVCのコントローラ層をテストする
 */
class ReadingRecordControllerSpec extends Specification {

    // テスト対象のコントローラ
    @Subject
    ReadingRecordController controller

    // モックオブジェクト
    ReadingRecordService mockService = Mock()

    // MockMvcセットアップ
    MockMvc mockMvc

    def setup() {
        controller = new ReadingRecordController(mockService)
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build()
    }

    def "list - デフォルトパラメータで読書記録一覧を表示する"() {
        given: "期待される読書記録のリスト"
        def readingRecords = [
                new ReadingRecord(id: 1L, title: "テスト本1", readingStatus: ReadingStatus.UNREAD),
                new ReadingRecord(id: 2L, title: "テスト本2", readingStatus: ReadingStatus.UNREAD)
        ]

        when: "一覧画面にアクセス"
        def result = mockMvc.perform(get("/reading-records"))

        then: "正常にレスポンスが返される"
        1 * mockService.getReadingRecordsByStatus(ReadingStatus.UNREAD) >> readingRecords
        4 * mockService.getCountByStatus(_) >> 5L  // 全ステータスの件数取得
        result.andExpect(status().isOk())
                .andExpect(view().name("reading-records/list"))
                .andExpect(model().attribute("readingRecords", readingRecords))
                .andExpect(model().attribute("currentStatusName", "UNREAD"))
    }

    def "list - ステータスパラメータを指定して読書記録一覧を表示する"() {
        given: "指定されたステータスの読書記録"
        def readingRecords = [
                new ReadingRecord(id: 1L, title: "読書中の本", readingStatus: ReadingStatus.READING)
        ]

        when: "READINGステータスで一覧画面にアクセス"
        def result = mockMvc.perform(get("/reading-records").param("status", "READING"))

        then: "正常にレスポンスが返される"
        1 * mockService.getReadingRecordsByStatus(ReadingStatus.READING) >> readingRecords
        4 * mockService.getCountByStatus(_) >> 3L
        result.andExpect(status().isOk())
                .andExpect(view().name("reading-records/list"))
                .andExpect(model().attribute("currentStatusName", "READING"))
    }

    def "list - 無効なステータスパラメータでアクセスした場合UNREADにフォールバックする"() {
        when: "無効なステータスでアクセス"
        def result = mockMvc.perform(get("/reading-records").param("status", "INVALID"))

        then: "UNREADステータスで処理される"
        1 * mockService.getReadingRecordsByStatus(ReadingStatus.UNREAD) >> []
        4 * mockService.getCountByStatus(_) >> 0L
        result.andExpect(status().isOk())
                .andExpect(model().attribute("currentStatusName", "UNREAD"))
    }

    def "detail - 存在する読書記録の詳細を表示する"() {
        given: "存在する読書記録"
        def record = new ReadingRecord(
                id: 1L,
                title: "テスト本",
                currentPage: 50,
                totalPages: 100
        )

        when: "詳細画面にアクセス"
        def result = mockMvc.perform(get("/reading-records/1"))

        then: "正常にレスポンスが返される"
        1 * mockService.getReadingRecordById(1L) >> Optional.of(record)
        1 * mockService.getProgressPercent(100, 50) >> 50
        result.andExpect(status().isOk())
                .andExpect(view().name("reading-records/detail"))
                .andExpect(model().attribute("readingRecord", record))
                .andExpect(model().attribute("progressPercent", 50))
    }

    def "detail - 存在しない読書記録の詳細にアクセスした場合一覧にリダイレクトする"() {
        when: "存在しないIDで詳細画面にアクセス"
        def result = mockMvc.perform(get("/reading-records/999"))

        then: "一覧画面にリダイレクトされる"
        1 * mockService.getReadingRecordById(999L) >> Optional.empty()
        result.andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reading-records"))
    }

    def "newRecord - 新規登録画面を表示する"() {
        when: "新規登録画面にアクセス"
        def result = mockMvc.perform(get("/reading-records/new"))

        then: "正常にレスポンスが返される"
        result.andExpect(status().isOk())
                .andExpect(view().name("reading-records/form"))
                .andExpect(model().attributeExists("readingRecord"))
                .andExpect(model().attributeExists("statuses"))
    }

    def "edit - 存在する読書記録の編集画面を表示する"() {
        given: "存在する読書記録"
        def record = new ReadingRecord(id: 1L, title: "編集対象の本")

        when: "編集画面にアクセス"
        def result = mockMvc.perform(get("/reading-records/1/edit"))

        then: "正常にレスポンスが返される"
        1 * mockService.getReadingRecordById(1L) >> Optional.of(record)
        result.andExpect(status().isOk())
                .andExpect(view().name("reading-records/form"))
                .andExpect(model().attribute("readingRecord", record))
    }

    def "edit - 存在しない読書記録の編集画面にアクセスした場合一覧にリダイレクトする"() {
        when: "存在しないIDで編集画面にアクセス"
        def result = mockMvc.perform(get("/reading-records/999/edit"))

        then: "一覧画面にリダイレクトされる"
        1 * mockService.getReadingRecordById(999L) >> Optional.empty()
        result.andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reading-records"))
    }

    def "save - 読書記録の保存に成功する"() {
        given: "保存対象の読書記録"
        def savedRecord = new ReadingRecord(id: 1L, title: "保存した本")

        when: "読書記録を保存"
        def result = mockMvc.perform(post("/reading-records/save")
                .param("title", "保存した本")
                .param("author", "著者名"))

        then: "詳細画面にリダイレクトされる"
        1 * mockService.saveReadingRecord(_) >> savedRecord
        result.andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reading-records/1"))
    }

    def "delete - 読書記録の削除に成功する"() {
        when: "読書記録を削除"
        def result = mockMvc.perform(post("/reading-records/1/delete"))

        then: "一覧画面にリダイレクトされる"
        result.andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reading-records"))

        and: "サービスメソッドが呼ばれる"
        1 * mockService.deleteReadingRecord(1L)
    }

    def "パラメータ化テスト - 様々なステータスでの一覧表示をテストする"() {
        when: "指定されたステータスで一覧画面にアクセス"
        def result = mockMvc.perform(get("/reading-records").param("status", status.name()))

        then: "正常にレスポンスが返される"
        1 * mockService.getReadingRecordsByStatus(status) >> []
        4 * mockService.getCountByStatus(_) >> 0L
        result.andExpect(status().isOk())
                .andExpect(model().attribute("currentStatusName", status.name()))

        where: "全ての有効なステータス"
        status << [
                ReadingStatus.UNREAD,
                ReadingStatus.READING,
                ReadingStatus.COMPLETED,
                ReadingStatus.PAUSED
        ]
    }

    def "exportCsv - CSV出力が正常に実行される"() {
        given: "CSV出力データ"
        def csvData = "ID,タイトル\n1,テスト本".getBytes("UTF-8")
        def fileName = "reading-records_20241127_143022.csv"

        when: "CSV出力エンドポイントにアクセス"
        def result = mockMvc.perform(get("/reading-records/export-csv"))

        then: "CSVデータが正常に返される"
        1 * mockService.exportToCsv() >> csvData
        1 * mockService.generateCsvFileName() >> fileName
        result.andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "form-data; name=\"attachment\"; filename=\"${fileName}\""))
                .andExpect(content().bytes(csvData))
    }

    def "exportCsv - CSV出力でエラーが発生した場合は500エラーを返す"() {
        when: "CSV出力エンドポイントにアクセス"
        def result = mockMvc.perform(get("/reading-records/export-csv"))

        then: "IOExceptionが発生して500エラーが返される"
        1 * mockService.exportToCsv() >> { throw new IOException("CSV出力エラー") }
        0 * mockService.generateCsvFileName()
        result.andExpect(status().isInternalServerError())
    }
}
