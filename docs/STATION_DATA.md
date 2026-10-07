# 電台資料格式與維護

## 1. 資料位置

| 用途 | 位置 |
|---|---|
| GitHub Pages 發布檔 | `docs/data/stations.v1.json` |
| 遠端網址 | `https://mark216tw.github.io/mini-taiwan-radio/data/stations.v1.json` |
| APK 內建備援 | `app/src/main/assets/stations.json` |
| App 本機快取 | SharedPreferences `station_cache/stations_json` |

遠端檔案是維護與發布的權威來源；App 執行時優先使用本機快取，其次是 APK assets，兩者都沒有可顯示資料時才自動下載遠端檔案。

## 2. 頂層格式

```json
{
  "schemaVersion": 1,
  "updatedAt": "2026-10-07T00:00:00Z",
  "stations": []
}
```

| 欄位 | 型別 | 說明 |
|---|---|---|
| `schemaVersion` | Integer | 資料格式版本；目前為 1 |
| `updatedAt` | String | ISO 8601 UTC 更新時間 |
| `stations` | Array | 電台項目；目前 parser 必須取得此欄位 |

目前 App 會保存但不驗證 `schemaVersion` 與 `updatedAt`。格式演進時必須先更新 parser，再發布新 schema。

## 3. 電台欄位

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
  "enabled": true,
  "noshow": false
}
```

| 欄位 | 必要 | 預設 | 行為 |
|---|---|---|---|
| `id` | 是 | 無 | 唯一識別、最愛、排序、Media ID、LazyColumn key |
| `name` | 是 | 無 | 清單、迷你播放器及媒體通知名稱 |
| `network` | 否 | `""` | 卡片次要資訊 |
| `frequency` | 否 | `""` | 卡片次要資訊 |
| `region` | 否 | `""` | 卡片次要資訊 |
| `streamUrl` | 否 | `""` | 空值時顯示「準備中」 |
| `websiteUrl` | 否 | `""` | 已載入 model，目前 UI 未使用 |
| `logoUrl` | 否 | `""` | 已載入 model，目前 UI 未使用 |
| `enabled` | 否 | `true` | `false` 時顯示但不可播放 |
| `noshow` | 否 | `false` | `true` 時完全不顯示，且不計入更新成功數量 |

## 4. `enabled` 與 `noshow` 的選擇

- 尚未取得串流，但希望使用者知道電台正在準備：`enabled: false`、`noshow: false`。
- 資料待查核、不希望出現在 App：`noshow: true`。
- 可正常播放：`enabled: true`、`noshow: false`，並提供非空 `streamUrl`。

若 `noshow: true`，其他欄位不會進入畫面模型。

## 5. 新增或修改電台

1. 選擇穩定且不重複的 `id`。上架後不應任意更改，否則使用者最愛資料會失效。
2. 填寫正式名稱、聯播網、頻率與地區。
3. 優先使用官方 HTTPS 串流。
4. 在不同網路與 Android 版本實際播放至少數分鐘。
5. 確認切台、背景播放及重新連線行為。
6. 記錄並確認串流與品牌素材的使用授權。
7. 更新頂層 `updatedAt`。
8. 同步遠端來源檔與 APK assets。

## 6. 排序規則

- 非最愛電台按照 JSON 原始順序顯示。
- 最愛電台固定在前方，並依使用者儲存的 ID 順序排列。
- `id` 必須唯一；重複 ID 會破壞 Compose stable key 與最愛對應。

## 7. 發布前資料檢查

- JSON 語法正確。
- `schemaVersion` 為 App 支援版本。
- `updatedAt` 為有效 UTC 時間。
- 每個 `id` 唯一且非空。
- 每個 `name` 非空。
- `enabled: true` 的可見項目具有可播放 URL。
- URL scheme 僅使用預期的 HTTP 或 HTTPS。
- 手動更新顯示的數量等於 `noshow != true` 的項目數。
- `docs/data/stations.v1.json` 與 `app/src/main/assets/stations.json` 內容同步。

PowerShell 可用下列方式確認兩檔案是否一致：

```powershell
git diff --no-index -- docs/data/stations.v1.json app/src/main/assets/stations.json
```

## 8. 快取行為

- 手動或必要的自動下載成功後，App 將完整 JSON 原文寫入 SharedPreferences。
- 解析時才排除 `noshow: true`。
- 快取目前沒有期限、ETag 或版本比較。
- 清除 App 資料或解除安裝會移除快取；Android 系統備份還原行為依裝置設定而定。

## 9. 授權與內容責任

本專案的 MIT License 僅適用於專案程式碼與文件，不代表取得任何電台商標、Logo、節目、音訊或串流的授權。資料合併與正式發布前，維護者必須完成來源及權利確認。
