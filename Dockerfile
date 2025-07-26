# --- 実行用ステージ ---
# 実行用の軽量イメージ
FROM eclipse-temurin:21-jre-alpine

# 非rootユーザーでの実行
RUN addgroup -g 1001 spring && adduser -u 1001 -G spring -s /bin/sh -D spring

USER spring:spring

# 作業ディレクトリの設定
WORKDIR /app

# 事前にビルドされたJARファイルをコピー
COPY --chown=spring:spring target/readrico.jar app.jar

# アプリケーションのポート
EXPOSE 8080

# アプリケーションの起動
ENTRYPOINT ["java", "-jar", "app.jar"]
