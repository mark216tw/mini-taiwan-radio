# 隱私與安全說明

## 1. 資料處理摘要

mini台灣電台不提供帳號系統，也不會將使用者的最愛清單或主題設定上傳至 GitHub Gist 或其他遠端服務。App 會在裝置中儲存電台快取、最愛順序及 UI 設定。

播放與更新時，裝置會直接連線至 GitHub Gist 及第三方電台串流伺服器。這些服務可能依其政策取得一般網路連線資訊，例如 IP 位址、User-Agent 或請求時間。

## 2. 本機資料

| 資料 | 儲存方式 |
|---|---|
| 遠端電台 JSON 快取 | SharedPreferences |
| 最愛電台 ID 與順序 | SharedPreferences |
| 顯示模式 | SharedPreferences |
| 主題色 | SharedPreferences |

App 不儲存帳號、密碼、定位、聯絡人或播放歷史。

Manifest 目前設定 `android:allowBackup="true"`。因此 SharedPreferences 可能依使用者裝置與 Android 備份設定，由系統備份及還原。這不等同於 App 主動上傳至 GitHub，但正式發布時仍應在隱私聲明中揭露，或設定 backup rules 排除不希望備份的資料。

## 3. 權限

| 權限 | 用途 |
|---|---|
| `INTERNET` | 下載電台清單及播放串流 |
| `FOREGROUND_SERVICE` | 執行背景播放前景服務 |
| `FOREGROUND_SERVICE_MEDIA_PLAYBACK` | 宣告媒體播放前景服務類型 |
| `POST_NOTIFICATIONS` | Android 13 以上顯示播放通知 |

App 不要求麥克風、相機、定位或儲存空間權限。

## 4. 網路安全

目前為相容候選電台來源，Manifest 設定 `usesCleartextTraffic="true"`。HTTP 串流沒有 TLS 保護，可能面臨：

- 網路中間人觀察或修改流量。
- 串流內容或回應遭竄改。
- 使用者連線資訊暴露於不受信任網路。

正式發布前應優先：

1. 將可用來源替換為 HTTPS。
2. 使用 Network Security Config，將 cleartext 例外限制在必要網域。
3. 定期檢查串流重新導向與憑證。
4. 移除不再需要的 HTTP 來源。

## 5. 遠端資料信任邊界

App 會下載 JSON 並把 `streamUrl` 交給 ExoPlayer。目前 parser 未完整驗證：

- HTTP status 與 Content-Type。
- JSON 大小。
- `schemaVersion`。
- URL scheme、host 或允許清單。
- 重複 ID。

維護者應保護 GitHub 帳號與發布分支，審查資料變更，並考慮在程式中增加 schema、大小與 URL 驗證。

## 6. MediaSessionService

`PlaybackService` 目前在 Manifest 中設定 `android:exported="true"`，以提供 MediaSessionService intent filter。正式發布前應檢查 Media3 controller 授權策略，確認是否需要限制非信任控制器的連線或命令。

## 7. 通知權限

Android 13 以上會在 Activity 建立時要求通知權限。拒絕通知權限後，播放功能仍應可使用，但系統對前景服務通知的呈現可能依版本與裝置而異。

建議後續改善：

- 僅在開始播放前適當時機要求權限。
- 在需要時顯示用途說明。
- 處理永久拒絕與設定頁導引。

## 8. 第三方內容與授權

電台名稱、商標、Logo、節目、音訊內容與串流服務屬第三方資產，不包含在專案 MIT License 的授權範圍。正式散布 App 前應逐項確認：

- 串流是否為官方或獲授權來源。
- 是否允許第三方 App 播放或重新散布連結。
- 是否需要顯示來源、商標或其他聲明。
- 地區限制、廣告與服務條款。

## 9. 弱點回報

目前專案尚未定義公開安全聯絡管道。公開發布前建議在儲存庫加入 `SECURITY.md`，指定支援版本、私下回報方式與回應流程，避免敏感弱點直接出現在公開 issue。

## 10. 發布前安全清單

- 將 HTTP 來源降至最低並限制 cleartext 網域。
- 驗證遠端 JSON schema、大小、ID 與 URL。
- 檢查 exported service 與 MediaSession controller 權限。
- 決定 SharedPreferences 的 Android Backup 政策。
- 建立正式隱私權政策及商店資料安全聲明。
- 保護 release keystore、GitHub 帳號與 Pages 發布流程。
- 更新依賴並執行 Android Lint 與相依套件檢查。
