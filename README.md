# Just Right Calendar（ちょうどいいカレンダー）

**見やすさと落ち着きを重視した、日本の祝日に対応した月表示カレンダーアプリ（Android）**

「全部入り」ではなく、  
**日常でちょうどよく使えること**を目的に作っています。

現在は **v0.9.x（仮完成・調整フェーズ）** です。

---

## Status

- **Current version:** `0.9.0`
- **Stage:** Feature complete / Stabilization phase

基本機能は揃っていますが、  
UI調整・ウィジェットの安定化を進めている段階です。

**数日間の実運用で問題がなければ、v1.0.0 としてリリース予定です。**

---

## Features

- **月表示カレンダー（月曜はじまり）**
- **土曜日：青系 / 日曜・祝日・振替休日：赤系**
- **日本の祝日に対応**
- **その日の状態をマークで管理（〇 / ✓ / ☆）**
- **ホーム画面ウィジェット対応**
- 落ち着いたトーンのデザイン  
  - 本体とウィジェットで色定義を共通化

---

## Screenshots

> TODO  
> （アプリ画面・ウィジェット画面を後で追加予定）

---

## Design Policy

- 視認性を最優先
- 情報を詰め込みすぎない
- 「毎日使っても疲れない」配色とレイアウト
- 本体：操作向け  
  ウィジェット：一目確認向け

---

## Permissions

本アプリは特別な権限を必要としません。  
すべての機能はオフラインで動作します。

---

## Tech Stack

- Android
- Kotlin
- XML Layout
- Gradle (Kotlin DSL)

---

## Build & Run

1. Android Studioでプロジェクトを開く
2. エミュレータまたは実機を起動
3. Run ▶

---

## APK

生成されるAPKファイルは以下の命名規則になっています。

just-right-calendar-v<version>-debug.apk
just-right-calendar-v<version>.apk

例：
- `just-right-calendar-v0.9.0-debug.apk`

---

## Versioning

- `versionCode`：内部用の連番（毎リリース +1）
- `versionName`：表示用（SemVer）

例：
- `0.9.0` : 機能揃い・調整中
- `1.0.0` : 安定版（予定）

---

## Roadmap

- ウィジェットの安定化
- UI細部の調整
- READMEへのスクリーンショット追加
- 数日間の実運用確認
- v1.0.0 リリース

---

## License

MIT License

