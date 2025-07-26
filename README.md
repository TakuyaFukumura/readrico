# Readrico

読書記録を管理するWebアプリケーションです。本のタイトル、著者、読書進捗、感想などを記録・管理できます。

## 概要

Readricoは、あなたの読書ライフをサポートする読書記録管理アプリケーションです。以下の機能を提供します：

- 📚 読書記録の登録・編集・削除
- 📊 読書ステータス管理（未読・読書中・読了・中止）
- 📈 読書進捗の追跡（現在のページ / 総ページ数）
- 📝 本の概要と感想の記録
- 🔍 ステータス別の記録一覧表示

## 技術スタック

### フレームワーク・ライブラリ
- **Java**: 17
- **Spring Boot**: 3.5.3
- **Spring Data JPA**: データアクセス層の簡素化
- **Thymeleaf**: テンプレートエンジン（Webインターフェース）

### データベース
- **H2 Database**: 2.2.220（軽量なインメモリデータベース）

### ツール・ユーティリティ
- **Lombok**: 1.18.38（冗長なコードを簡潔にするライブラリ）
- **Maven**: ビルド管理ツール

### フロントエンド
- **Bootstrap**: 5.3.3（UIフレームワーク）

## セットアップ

### 必要環境
- Java 17以上
- Maven（または同梱のMaven Wrapper使用）

### インストールと起動

1. **リポジトリのクローン**
```bash
git clone https://github.com/TakuyaFukumura/readrico.git
cd readrico
```

2. **アプリケーションの起動**
```bash
./mvnw spring-boot:run
```

3. **ブラウザでアクセス**
```
http://localhost:8080
```

### ビルドと実行

```bash
# プロジェクトのクリーンとコンパイル
./mvnw clean compile

# パッケージの作成
./mvnw clean package

# JARファイルの実行
java -jar target/readrico.jar
```

## 使用方法

### 基本操作

1. **ホーム画面**: `http://localhost:8080`
   - アプリケーションのメイン画面
   - 読書記録一覧へのリンク

2. **読書記録一覧**: `http://localhost:8080/reading-records`
   - ステータス別で記録を表示
   - 各ステータスの件数表示
   - 新規記録の追加

3. **記録の新規作成**: 
   - タイトル、著者名の入力
   - 読書ステータスの選択
   - 総ページ数と現在のページの設定

4. **記録の詳細・編集**:
   - 読書進捗の更新
   - 概要と感想の追加・編集
   - 読書ステータスの変更

### 読書ステータス

- **未読**: まだ読み始めていない本
- **読書中**: 現在読んでいる本
- **読了**: 読み終わった本
- **中止**: 読むのを中断した本

## データベース

### H2コンソール（開発・デバッグ用）

アプリケーション起動中は、H2データベースコンソールにアクセスできます：

```
URL: http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:testdb
ユーザー名: sa
パスワード: （空白）
```

### データベーススキーマ

`reading_record`テーブル：
- `id`: 主キー（自動生成）
- `title`: 本のタイトル（必須）
- `author`: 著者名
- `reading_status`: 読書ステータス（必須）
- `current_page`: 現在のページ
- `total_pages`: 総ページ数
- `summary`: 本の概要
- `thoughts`: 感想
- `created_at`: 作成日時
- `updated_at`: 更新日時

## 開発情報

### プロジェクト構成

```
src/
├── main/
│   ├── java/com/example/myapplication/
│   │   ├── Main.java                     # アプリケーションメインクラス
│   │   ├── controller/                   # コントローラー層
│   │   │   ├── IndexController.java      # ホーム画面
│   │   │   └── ReadingRecordController.java # 読書記録CRUD
│   │   ├── entity/                       # エンティティ層
│   │   │   └── ReadingRecord.java        # 読書記録エンティティ
│   │   ├── repository/                   # リポジトリ層
│   │   │   └── ReadingRecordRepository.java # データアクセス
│   │   ├── service/                      # サービス層
│   │   │   └── ReadingRecordService.java # ビジネスロジック
│   │   └── status/                       # 列挙型
│   │       └── ReadingStatus.java        # 読書ステータス
│   └── resources/
│       ├── application.properties        # アプリケーション設定
│       ├── schema.sql                    # データベーススキーマ
│       ├── data.sql                      # 初期データ
│       └── templates/                    # Thymeleafテンプレート
│           ├── index.html                # ホーム画面
│           └── reading-records/          # 読書記録画面
│               ├── list.html             # 一覧画面
│               ├── detail.html           # 詳細画面
│               └── form.html             # 登録・編集画面
```

### ビルド設定

- **GroupId**: com.example
- **ArtifactId**: readrico
- **Version**: 0.4.0
- **Packaging**: jar
- **Final Name**: readrico

## ライセンス

このプロジェクトはMITライセンスの下で公開されています。

## 貢献

プルリクエストやイシューの報告を歓迎します。貢献する前に、既存のコーディングスタイルを確認してください。
