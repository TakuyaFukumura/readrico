# Readrico - 日本語読書記録管理アプリケーション

予期しない情報に遭遇した場合にのみ、検索やbashコマンドにフォールバックし、
まずはこれらの手順書を参照してください。

## プロジェクト概要

Readricoは読書記録を管理するSpring Bootウェブアプリケーションです。
ユーザーは読書中の本、進捗、評価、感想を追跡できます。
アプリケーションはJava 17、Spring Boot 3.5.4、H2インメモリデータベース、Thymeleafテンプレート、BootstrapによるUIを使用しています。

## 効果的な作業方法

### 前提条件と環境設定
- Java 17+が必要です（環境にOpenJDK 17.0.16が利用可能）
- MavenはMaven Wrapper (./mvnw)で管理されています - 別途Mavenのインストールは不要
- コンテナ化されたデプロイメントのためにDockerが利用可能

### ビルドコマンドとタイミング
**重要**: Mavenビルドを中断しないでください
- 初回実行時は依存関係のダウンロードにより数分かかることがあります。

- **クリーンコンパイル**（初回セットアップとダウンロード）:
  ```bash
  ./mvnw clean compile
  ```
  - 初回実行時は約3分かかります（依存関係をダウンロード）
  - 中断厳禁: タイムアウトを300秒以上（5分以上）に設定

- **パッケージビルド**（テストなし）:
  ```bash
  ./mvnw clean package -DskipTests
  ```
  - 初期依存関係ダウンロード後は約1.5分
  - 中断厳禁: タイムアウトを180秒以上（3分以上）に設定

- **フルビルド（テスト含む）**:
  ```bash
  ./mvnw clean package
  ```
  - テスト完了まで約8-15秒（104個のSpockテスト）
  - 依存関係キャッシュ後の総ビルド時間: 約7-8秒
  - 中断厳禁: 依存関係ダウンロードを考慮してタイムアウトを180秒以上（3分以上）に設定

### アプリケーション実行

- **Maven経由（開発用）**:
  ```bash
  ./mvnw spring-boot:run
  ```
  - アプリケーションはポート8080で開始
  - 起動時間は約3秒
  - http://localhost:8080 でアクセス

- **JARファイル経由**:
  ```bash
  java -jar target/readrico.jar
  ```
  - 事前にビルドが必要: `./mvnw clean package`
  - Mavenと同じ起動動作

### テスト

- **全テスト実行**:
  ```bash
  ./mvnw test
  ```
  - Spockフレームワーク（Groovy）を使用した104個のテスト
  - 完了まで10-15秒
  - 全てのテストが成功する必要があります

- **テストカテゴリ**: サービス層、コントローラ層、エンティティ検証、ステータス列挙型
- 追加のリンターやコードフォーマットツールは設定されていません

### Docker デプロイメント

- **Dockerイメージビルド**（事前にJARファイルが必要）:
  ```bash
  ./mvnw clean package -DskipTests
  docker build -t readrico .
  ```
  - JAR準備後のDockerビルドは約3秒
  - Eclipse Temurin 21 JRE Alpineベースイメージを使用

- **Docker Compose デプロイメント**:
  ```bash
  docker compose up --build
  ```
  - アプリケーションをビルドして開始
  - 合計約3秒
  - http://localhost:8080 でアクセス

- **Docker Compose停止**:
  ```bash
  docker compose down
  ```

## 検証

### 手動テスト要件
変更後は**必ず**以下の検証手順を実行してください:

1. **アプリケーション起動確認**:
   - アプリケーションを開始し「Started Main in X.X seconds」に到達することを確認
   - http://localhost:8080 がタイトル「Readrico - 読書記録アプリ」のホームページHTMLを返すことを確認
   - http://localhost:8080/reading-records で読書記録インターフェースが表示されることを確認

2. **コア機能テスト**:
   - 読書記録一覧に移動: http://localhost:8080/reading-records
   - 異なる読書ステータス（未読、読書中、読了、中止）のタブとカウントが表示されることを確認
   - サンプルデータが正しく読み込まれることを確認（「銀河鉄道の夜」、「雪国」などの日本語書籍タイトルが表示される）
   - 新規レコード作成をテスト: http://localhost:8080/reading-records/new （タイトル「読書記録登録」が表示される）
   - H2コンソールがアクセス可能であることを確認: http://localhost:8080/h2-console （ログインページにリダイレクト）

3. **データベース接続テスト**:
   - H2コンソールURL: http://localhost:8080/h2-console
   - JDBC URL: jdbc:h2:mem:testdb
   - ユーザー名: sa
   - パスワード: （空白）
   - サンプルデータを含むreading_recordテーブルが表示される

4. **ビルド成果物確認**:
   - JARファイルが作成されることを確認: `ls -lh target/readrico.jar`（約58MBになる）
   - JARが実行可能であることを確認: `java -jar target/readrico.jar`（Spring Bootアプリが開始）

5. **テスト実行確認**:
   - 最終検証前に必ず`./mvnw test`を実行
   - 104個のテストが全て成功することを確認（Spockフレームワーク）
   - 変更に関連する新しいテスト失敗がないか確認

### CI/CD 検証
GitHub Actionsワークフロー（`.github/workflows/build.yml`）は以下を実行します:
- Amazon CorrettoでJava 17セットアップ
- Mavenビルド: `mvn clean package`
- 変更がCIビルドを壊さないことを必ず確認

## 主要アプリケーション機能

### 読書ステータス管理
- **UNREAD**（未読）: まだ読み始めていない本
- **READING**（読書中）: 現在読んでいる本
- **COMPLETED**（読了）: 読み終えた本
- **PAUSED**（中止）: 保留中/中断した本

### コア機能
- 読書記録の追加/編集/削除
- 読書進捗の追跡（現在のページ / 総ページ数）
- 本の評価記録（1-5段階）
- 感想と要約の追加
- CSV エクスポート/インポート機能
- ダークモード切り替え

## プロジェクト構成

### ソースコード構成
```
src/main/java/com/example/myapplication/
├── Main.java                          # アプリケーションエントリーポイント
├── controller/                        # ウェブコントローラ
│   ├── IndexController.java          # ホームページ
│   └── ReadingRecordController.java  # CRUD操作
├── entity/                           # JPAエンティティ
│   └── ReadingRecord.java           # メインデータモデル
├── repository/                       # データアクセス層
│   └── ReadingRecordRepository.java # JPAリポジトリ
├── service/                          # ビジネスロジック
│   └── ReadingRecordService.java    # コアサービス層
├── status/                           # 列挙型
│   └── ReadingStatus.java           # 読書ステータス列挙型
└── util/                            # ユーティリティ
    └── TempMultipartFile.java       # ファイル処理
```

### リソース
- `application.properties`: データベースとアプリケーション設定
- `schema.sql`: データベーステーブル定義
- `data.sql`: サンプルデータ初期化
- `templates/`: ウェブUI用Thymeleaf HTMLテンプレート

### テスト構成
- `src/test/groovy/`: Spockフレームワークテスト（総数104個）
- 全てのテストが成功する必要があります; 新しい失敗を調査してください

## よくあるトラブルシューティング

### ビルド問題
- 依存関係問題でビルドが失敗する場合、Mavenキャッシュをクリア: `rm -rf ~/.m2/repository`
- テストが予期せず失敗する場合、H2データベース初期化が動作しているか確認
- Java 17が使用されていることを確認: `java -version`（OpenJDK 17.0.16と表示されるべき）
- Maven wrapperが失敗する場合: `chmod +x mvnw`で実行権限を確認

### ランタイム問題
- ポート8080が使用中の場合: `lsof -ti:8080`でプロセスを確認、`pkill -f spring-boot:run`で停止
- H2コンソールが動作しない場合: application.propertiesに`spring.h2.console.enabled=true`があることを確認
- 日本語テキストが文字化けする場合: テンプレートでUTF-8エンコーディングを確認
- アプリケーションが起動しない場合: 「Address already in use」エラーを確認し、既存プロセスを終了

### Docker 問題
- Dockerビルド前にJARファイルが存在することを確認: `./mvnw clean package`
- コンテナが起動しない場合: `docker logs <container_name>`でDockerログを確認
- Docker Composeが失敗する場合: `docker compose down`してから`docker compose up --build`を試す

## リポジトリファイル概要

### 主要ファイル
- `README.md`: 日本語での包括的なドキュメント
- `pom.xml`: 全依存関係を含むMavenビルド設定
- `Dockerfile`: マルチステージDockerビルド設定
- `docker-compose.yml`: シンプルなサービス定義
- `.github/workflows/build.yml`: CI/CDパイプライン

### 一般的なコマンドのサンプル出力

#### ls -la（リポジトリルート）
```
total 76
-rw-r--r-- 1 runner docker   311 .gitignore
-rw-r--r-- 1 runner docker   524 Dockerfile  
-rw-r--r-- 1 runner docker  6795 README.md
-rw-r--r-- 1 runner docker   202 docker-compose.yml
-rwxr-xr-x 1 runner docker 10069 mvnw
-rw-r--r-- 1 runner docker  6607 mvnw.cmd
-rw-r--r-- 1 runner docker  4546 pom.xml
```

#### アプリケーション起動ログ
```
Started Main in 2.995 seconds (process running for 3.3)
Tomcat started on port 8080 (http) with context path '/'
H2 console available at '/h2-console'. Database available at 'jdbc:h2:mem:testdb'
```

## 開発ベストプラクティス

- 変更後は必ずMavenとDockerの両方のデプロイ方法をテスト
- HTTPエンドポイントが期待されるコンテンツを返すことを確認（ステータスコードだけでなく）
- ウェブインターフェースで日本語テキストが正しく表示されることを確認
- 変更をコミットする前にフルテストスイートを実行
- デバッグ時はH2コンソールでデータベース状態を検査

## クイックリファレンスコマンド

### 基本コマンド（コピー＆ペースト可能）
```bash
# ビルドとテスト（最も一般的なワークフロー）
./mvnw clean package
java -jar target/readrico.jar

# 開発サーバー
./mvnw spring-boot:run

# テストのみ
./mvnw test

# Docker デプロイメント
./mvnw clean package -DskipTests
docker compose up --build

# クリーンアップ
pkill -f spring-boot:run
docker compose down
```

### ステータス確認コマンド
```bash
# アプリが実行中かチェック
curl -I http://localhost:8080

# メインページコンテンツチェック
curl -s http://localhost:8080 | grep -o "<title>.*</title>"

# ビルド成果物確認
ls -lh target/readrico.jar

# Javaバージョン確認
java -version
```
