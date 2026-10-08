# 開發與建置指南

## 1. 開發環境

建議安裝：

- Android Studio，支援 Android Gradle Plugin 8.7.x。
- JDK 17。
- Android SDK Platform 35。
- Android SDK Build Tools 及 Platform Tools。
- Git。

主要版本：

| 工具／套件 | 版本 |
|---|---|
| Android Gradle Plugin | 8.7.3 |
| Kotlin／Compose plugin | 2.0.21 |
| 建議 Gradle | 8.9 |
| Compose BOM | 2024.12.01 |
| Media3 | 1.5.1 |

## 2. 開啟專案

1. 使用 Android Studio 選擇「Open」。
2. 指向專案根目錄。
3. 將 Gradle JDK 設為 17。
4. 安裝缺少的 SDK 35 元件。
5. 執行 Gradle Sync。

專案只有 `:app` 模組，Application ID 為 `com.mark216tw.minitaiwanradio`。

## 3. 建置 Debug APK

### Android Studio

使用 `Build > Build APK(s)`，或直接執行 `app` configuration。

### 命令列

目前儲存庫未包含 `gradlew`、`gradlew.bat` 與 `gradle/wrapper`。若本機已有 Gradle 8.9：

```powershell
gradle :app:assembleDebug
```

若日後加入 Gradle Wrapper，Windows 指令為：

```powershell
.\gradlew.bat :app:assembleDebug
```

輸出：

```text
app/build/outputs/apk/debug/app-debug.apk
```

### Pre-release 測試發行版本

`prerelease` Build Type 繼承 release 設定，啟用 R8 程式碼最佳化與資源縮減，並使用 Android 預設 Debug 金鑰簽署。此產物只供測試，不可視為正式上線版本。

```powershell
gradle :app:assemblePrerelease
```

輸出：

```text
app/build/outputs/apk/prerelease/app-prerelease.apk
```

目前版本名稱為 `1.0.0-prerelease.2`，versionCode 為 2。

## 4. 常用工作

```powershell
# 清除建置產物
gradle clean

# 編譯 Debug APK
gradle :app:assembleDebug

# 編譯啟用 R8、以 Debug 金鑰簽署的測試發行 APK
gradle :app:assemblePrerelease

# 執行 Android Lint
gradle :app:lintDebug

# 若未來加入單元測試
gradle :app:testDebugUnitTest
```

## 5. 專案目錄

```text
app/src/main/
├── AndroidManifest.xml
├── assets/stations.json
├── java/com/mark216tw/minitaiwanradio/
│   ├── MainActivity.kt
│   ├── PlaybackService.kt
│   ├── Station.kt
│   ├── StationRepository.kt
│   └── UiPreferences.kt
└── res/
    ├── drawable/
    ├── mipmap-anydpi-v26/
    └── values/
```

## 6. 修改電台資料

1. 編輯 `docs/data/stations.v1.json`。
2. 依 [`STATION_DATA.md`](STATION_DATA.md) 驗證欄位與行為。
3. 同步更新 `app/src/main/assets/stations.json`，作為下一版 APK 的內建備援。
4. 確認 JSON 可解析、ID 不重複、可見數量正確。
5. 實機測試新增或修改的串流。

App 從 GitHub Gist 取得遠端電台資料。更新 Gist 後，應確認 raw URL 回傳 HTTP 200、內容為可解析的 JSON，並同步專案資料快照與 APK assets。

## 7. 修改 UI

- 主要 Compose 畫面目前集中於 `MainActivity.kt`。
- 既有視覺語言採圓角卡片、粗體標題及可變主題色。
- 保留 `WindowInsets.safeDrawing` 與迷你播放器的 `navigationBarsPadding()`。
- 播放控制、最愛與設定元件應提供可理解的 content description。
- 調整卡片高度時需重新驗證最愛拖曳排序，排序使用實際卡片量測值。

## 8. 修改播放功能

- Activity 透過 Intent action 控制 `PlaybackService`。
- Service 擁有 ExoPlayer 與 MediaSession；不要在 Composable 直接建立 player。
- 新增播放格式時確認 Media3 是否需要額外模組。
- 修改停止或背景行為時，至少驗證 Home、鎖定畫面、通知控制與最近使用清單移除。

## 9. 除錯建議

### 串流無法播放

- 檢查 URL 是否可連線、是否重新導向及憑證是否有效。
- 確認 HTTP 串流是否受 cleartext 設定影響。
- 使用 Logcat 篩選 `PlaybackService`。
- 以實機測試，部分來源可能依 User-Agent、地區或網路環境限制。

### 電台清單未更新

- 確認 GitHub Gist raw URL 可取得 JSON。
- 檢查 JSON 是否具有 `stations` array。
- 清除 App 資料可移除既有 SharedPreferences 快取。
- 注意 App 已有本機清單時不會自動下載，請使用設定頁手動更新。

### 背景播放或通知異常

- Android 13 以上確認通知權限。
- 檢查 `FOREGROUND_SERVICE` 與 `FOREGROUND_SERVICE_MEDIA_PLAYBACK` 權限。
- 確認 service 的 `foregroundServiceType="mediaPlayback"`。

## 10. 發布前檢查

- 確認所有串流來源與使用授權。
- 優先將 HTTP 串流改為 HTTPS。
- 建立 release signing config，安全保管 keystore。
- 設定 versionCode 與 versionName。
- 執行 [`TEST_PLAN.md`](TEST_PLAN.md)。
- 檢查通知、前景服務、備份、隱私政策及商店資料安全聲明。
- 建議補入 Gradle Wrapper，以固定且重現建置環境。

## 11. 目前限制

- 尚無 release build/signing 文件與 CI。
- `prerelease` 使用 Debug 金鑰，只適合測試與 Pre-release 發布。
- 尚無 `src/test` 或 `src/androidTest` 自動化測試。
- 專案未包含 Gradle Wrapper。
- UI 字串多數直接位於 Kotlin 程式碼，尚未完整資源化與多語系化。
