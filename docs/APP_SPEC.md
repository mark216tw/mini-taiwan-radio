# mini台灣電台 App 功能規格

## 1. 產品資訊

- App 名稱：mini台灣電台
- 平台：Android
- Application ID：`com.mark216tw.minitaiwanradio`
- 目前版本：`1.0.0-prerelease.3`（versionCode 3）
- 最低 Android 版本：Android 8.0（API 26）
- Target SDK：API 35
- 開發技術：Kotlin、Jetpack Compose、Material 3、AndroidX Media3

## 2. 電台資料

### 2.1 資料來源

電台資料的遠端權威來源為 GitHub Gist：

```text
https://gist.githubusercontent.com/mark216tw/8de9db577e248f2bce47e2e3a31c02f8/raw
```

專案資料快照：

```text
docs/data/stations.v1.json
```

APK 內建備援清單：

```text
app/src/main/assets/stations.json
```

目前清單包含 28 個台灣電台項目。直播網址為候選資料，上架前仍須確認穩定性、官方來源及第三方使用授權。

### 2.2 載入順序

1. 每次開啟 App 時優先使用上次成功下載的本機快取。
2. 沒有有效快取時，使用 APK 內建 `stations.json`。
3. 本機快取與 APK 內建清單都沒有可顯示電台時，才自動下載 GitHub Gist 遠端清單。
4. 自動下載成功後，更新畫面並寫入本機快取；若下載失敗則顯示無資料狀態，不影響 App 運作。

### 2.3 手動更新

設定頁提供文字按鈕「更新電台清單」。

- 更新期間按鈕停用並顯示「更新中...」。
- 更新成功顯示「更新成功，共 N 個電台」。
- 更新失敗顯示「更新失敗，已保留目前的電台資料」。
- 更新成功後主畫面立即套用新清單。

### 2.4 JSON 欄位

```json
{
  "id": "news98",
  "name": "九八新聞台 (News98)",
  "network": "News98",
  "frequency": "FM 98.1",
  "region": "台北",
  "streamUrl": "https://example.com/live",
  "websiteUrl": "https://example.com/",
  "logoUrl": "",
  "enabled": true
}
```

欄位行為：

- `id`：電台唯一識別值，也用於最愛與排序資料。
- `streamUrl`：直播音訊網址。
- `enabled: false`：電台仍顯示，但顯示「準備中」且不可播放。

## 3. 主畫面

- 顯示 App 標題「mini台灣電台」及副標題「把台灣電台帶著走」。
- 右上角提供齒輪設定按鈕。
- 電台以直向卡片清單呈現。
- 卡片顯示電台名稱、聯播網、頻率及地區。
- 無直播網址或 `enabled: false` 時顯示「準備中」。
- 播放中的電台以主題色外框標示，卡片內部背景維持一般樣式。

## 4. 電台播放

### 4.1 支援格式

- MP3
- AAC
- HLS／M3U8
- HTTP 及 HTTPS 串流

目前為相容既有候選串流，App 暫時允許 cleartext HTTP。正式發布時應優先改用 HTTPS。

### 4.2 操作方式

- 清單右側由上至下顯示最愛與播放／停止控制。
- 播放與停止按鈕為圓形樣式。
- 播放／停止圖示使用目前主題色。
- 點擊其他電台時立即切換直播來源。

### 4.3 播放狀態

- 開始播放或切換電台時：顯示「正在連線...」。
- 成功播放時：顯示「播放中」。
- 播放失敗時：顯示「串流暫時無法播放」。
- 播放錯誤不應造成 App 閃退。

## 5. 迷你播放器

- 有選定的播放電台時顯示於主畫面底部。
- 第一列顯示「現正播放」及緊鄰右側的播放狀態。
- 第二列顯示電台名稱。
- 右側提供圓形停止按鈕。
- 不顯示 loading 圖示。
- 點擊迷你播放器的電台區域，清單會捲動到目前播放的電台。
- 迷你播放器會避開 Android 導覽列。

### 5.1 睡眠定時器

- 迷你播放器第一列在播放狀態右側顯示小型時鐘按鈕。
- 設定定時器後，時鐘右側顯示剩餘時間。
- 提供 5、10、15、30、45 分鐘及 1 小時選項。
- 再次點擊時鐘可重新設定或取消定時器。
- 切換電台時維持原倒數。
- 手動停止、串流錯誤、從最近使用清單移除 App 或服務結束時取消定時器。
- 時間到時停止播放、停止播放服務並移除通知。
- 倒數由 `PlaybackService` 執行，按 Home 或鎖定螢幕後仍會繼續。

## 6. 最愛電台

- 每個電台提供愛心圖示，可加入或取消最愛。
- 加入最愛後顯示「電台名稱已加入最愛」。
- 最愛愛心固定使用淺紅色 `#FF8A8A`，不跟隨主題色。
- 愛心圖示不顯示圓形背景。
- 最愛電台固定排列在一般電台之前。
- 非最愛電台維持 JSON 原始順序。
- 最愛設定及順序儲存在手機本機 `SharedPreferences`。
- App 關閉、從最近使用清單刷掉或重新啟動後，最愛設定仍會保留。

### 6.1 最愛排序

- 長按最愛電台卡片後可上下拖曳排序。
- 拖曳中的卡片會放大、提高陰影及顯示於最上層。
- 其他項目會使用清單排列動畫移動。
- 排序完成後立即儲存新的最愛順序。

## 7. 背景播放

- 按 Home 回到桌面時繼續播放。
- 鎖定螢幕時繼續播放。
- 使用 MediaSession 與系統媒體通知控制播放。
- 點擊媒體通知內容會開啟 App，並優先回到既有的主畫面 Activity。
- Android 13 以上會要求通知權限。
- 從最近使用的 App 清單刷掉 App 時停止播放、停止播放服務並移除通知。
- 重新開啟 App 時不會自動恢復上次播放。

## 8. 設定

設定頁可由主畫面右上角齒輪圖示進入。

- 左上角返回箭頭可回到主畫面。
- Android 系統返回鍵可回到主畫面。
- 標題及內容避開狀態列與導覽列。

### 8.1 顯示模式

提供以下選項，預設為系統：

- 系統
- 淺色
- 深色

點擊後立即套用，並儲存在手機本機。

### 8.2 主題色彩

提供六個預設色彩：

- 珊瑚紅
- 天空藍
- 薄荷綠
- 活力紫
- 明亮黃
- 橘色

選中的預設色彩會在圓形中央顯示勾勾，不會改成白色圓形。

### 8.3 自訂色彩

- 提供單一 Hue 彩色滑桿。
- 滑桿左側顯示目前自訂色彩圓形色塊。
- 滑桿以外框定位圈標示目前 Hue 位置。
- 拖動滑桿時立即更新 App 主題色彩。
- 預設色彩與 Hue 滑桿位置連動。
- 自訂色彩儲存在手機本機。

### 8.4 系統列

- 狀態列及導覽列圖示會依淺色／深色模式調整。
- 主畫面與設定頁使用安全 Insets，內容不與系統列重疊。

## 9. App 圖示

- 風格：活潑、友善、粗線條卡通工具圖示。
- 圖案：收音機、天線及電波。
- 主畫布：108 × 108dp。
- 外框裁切區：72 × 72dp。
- 核心安全區：66 × 66dp。
- 天線、訊號燈及電波描邊均限制在安全區內。
- 外圈長波紋使用天空藍 `#3D91D4`，與深藍色收音機外框區隔。

圖示來源檔：

```text
docs/design/app-icon.svg
app/src/main/res/drawable/ic_launcher_foreground.xml
app/src/main/res/drawable/ic_launcher_background.xml
```

## 10. 本機儲存資料

App 使用 `SharedPreferences` 儲存：

- 遠端電台清單快取
- 最愛電台 ID 與自訂順序
- 顯示模式
- 主題色彩

App 不會將使用者最愛或主題設定上傳至 GitHub Gist 或其他遠端服務。

## 11. 權限

- `INTERNET`：下載電台清單及播放直播串流。
- `FOREGROUND_SERVICE`：背景播放。
- `FOREGROUND_SERVICE_MEDIA_PLAYBACK`：媒體播放前景服務。
- `POST_NOTIFICATIONS`：Android 13 以上顯示播放通知。

## 12. 建置

目前儲存庫未包含 Gradle Wrapper。已安裝相容 Gradle 時，Debug APK 建置指令為：

```text
gradle :app:assembleDebug
```

APK 輸出位置：

```text
app/build/outputs/apk/debug/app-debug.apk
```

若後續加入 Gradle Wrapper，Windows 可使用：

```text
.\gradlew.bat :app:assembleDebug
```

Pre-release 測試發行版本使用 `prerelease` Build Type，啟用 R8 與資源縮減，並以 Debug 金鑰簽署：

```text
gradle :app:assemblePrerelease
```

```text
app/build/outputs/apk/prerelease/app-prerelease.apk
```

目前 Pre-release 版本為 `1.0.0-prerelease.3`，僅供測試，不代表正式上線版本。
