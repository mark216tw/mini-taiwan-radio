# mini台灣電台

mini台灣電台是一款以 Kotlin、Jetpack Compose 與 AndroidX Media3 製作的 Android 網路收音機 App。它提供台灣電台清單、最愛排序、背景播放、系統媒體控制、深淺色模式及自訂主題色。

> 目前版本為 `1.0.0-prerelease`。這是測試發行版本，不是正式上線版本。電台串流網址仍屬候選資料，正式發布前必須確認穩定性、官方來源與第三方使用授權。

## 主要功能

- 播放 MP3、AAC、HLS／M3U8，以及 HTTP／HTTPS 電台串流。
- 使用 MediaSession 與前景服務支援背景及鎖定畫面播放。
- 將電台加入最愛，並以長按拖曳調整最愛順序。
- 使用本機快取與 APK 內建清單，在離線狀態仍可顯示電台資料。
- 從設定頁手動更新 GitHub Pages 上的電台清單。
- 支援系統、淺色、深色顯示模式及自訂 Hue 主題色。
- 使用迷你播放器顯示播放狀態並快速停止或定位目前電台。

## 系統需求

| 項目 | 版本 |
|---|---|
| Android | Android 8.0（API 26）以上 |
| Compile／Target SDK | API 35 |
| JDK | 17 |
| Android Gradle Plugin | 8.7.3 |
| Kotlin | 2.0.21 |
| 建議 Gradle | 8.9 |

## 快速開始

目前專案尚未提供預建 APK 或公開 Release，需先依下列步驟自行建置。

1. 使用 Android Studio 開啟專案根目錄。
2. 安裝 Android SDK 35，並確認 Gradle JDK 為 JDK 17。
3. 等待 Gradle Sync 完成。
4. 選擇 `app` configuration，在 API 26 以上的模擬器或實機執行。

若本機已安裝相容的 Gradle，可在專案根目錄建置 Debug APK：

```powershell
gradle :app:assembleDebug
```

APK 輸出位置：

```text
app/build/outputs/apk/debug/app-debug.apk
```

建置啟用 R8 與資源縮減、使用 Debug 金鑰簽署的測試發行 APK：

```powershell
gradle :app:assemblePrerelease
```

```text
app/build/outputs/apk/prerelease/app-prerelease.apk
```

目前儲存庫未包含 Gradle Wrapper。若後續加入 Wrapper，Windows 可改用：

```powershell
.\gradlew.bat :app:assembleDebug
```

完整建置說明請見 [`docs/DEVELOPMENT.md`](docs/DEVELOPMENT.md)。

## 電台資料

遠端權威清單：

```text
https://mark216tw.github.io/mini-taiwan-radio/data/stations.v1.json
```

此網址需先將 GitHub Pages 設定為由 `main` 分支的 `/docs` 發布；未完成部署時會無法下載。正式發布前應確認網址回傳 HTTP 200 且內容可解析。

對應檔案與內建備援：

- GitHub Pages：`docs/data/stations.v1.json`
- APK assets：`app/src/main/assets/stations.json`

App 執行時依序使用本機快取、APK 內建清單；兩者均沒有可顯示資料時才自動下載遠端清單。使用者也可在設定頁手動更新。

## 專案結構

```text
.
├── app/
│   └── src/main/
│       ├── assets/stations.json
│       ├── java/com/mark216tw/minitaiwanradio/
│       └── res/
├── docs/
│   ├── data/stations.v1.json
│   ├── design/
│   └── *.md
├── LICENSE
└── README.md
```

## 文件

- [文件總覽](docs/README.md)
- [App 功能規格](docs/APP_SPEC.md)
- [使用指南](docs/USER_GUIDE.md)
- [系統架構與技術文件](docs/ARCHITECTURE.md)
- [系統設計文件](docs/SYSTEM_DESIGN.md)
- [開發與建置指南](docs/DEVELOPMENT.md)
- [電台資料格式與維護](docs/STATION_DATA.md)
- [測試計畫](docs/TEST_PLAN.md)
- [隱私與安全說明](docs/PRIVACY_SECURITY.md)

## 授權

程式碼以 [MIT License](LICENSE) 授權。電台名稱、商標、Logo、節目內容及第三方串流不因本專案採用 MIT License 而取得授權，使用與發布前應另行確認權利。
