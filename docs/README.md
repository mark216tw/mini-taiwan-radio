# mini台灣電台文件總覽

本目錄收錄 mini台灣電台的產品規格、使用說明、架構設計、開發維護與測試文件。根目錄 [`README.md`](../README.md) 提供專案入口，授權條款以根目錄 [`LICENSE`](../LICENSE) 為準。

## 文件索引

| 文件 | 對象 | 內容 |
|---|---|---|
| [`APP_SPEC.md`](APP_SPEC.md) | 產品、設計、開發 | App 功能與視覺行為規格 |
| [`USER_GUIDE.md`](USER_GUIDE.md) | 使用者、測試人員 | 安裝、播放、最愛、更新及設定操作 |
| [`ARCHITECTURE.md`](ARCHITECTURE.md) | 開發人員 | 元件責任、資料流、播放服務與本機儲存 |
| [`SYSTEM_DESIGN.md`](SYSTEM_DESIGN.md) | 開發、維護人員 | 系統邊界、狀態模型、循序流程與設計決策 |
| [`DEVELOPMENT.md`](DEVELOPMENT.md) | 開發人員 | 開發環境、建置、除錯與發布準備 |
| [`STATION_DATA.md`](STATION_DATA.md) | 資料維護人員 | JSON schema、載入策略及電台資料維護流程 |
| [`TEST_PLAN.md`](TEST_PLAN.md) | 開發、QA | 功能、相容性、生命週期及回歸測試清單 |
| [`PRIVACY_SECURITY.md`](PRIVACY_SECURITY.md) | 開發、發布人員 | 權限、網路、資料儲存、備份與風險說明 |
| [`design/README.md`](design/README.md) | 設計、開發人員 | App 圖示尺寸、色彩與資源對應 |

## 重要資料

- 遠端發布來源：`data/stations.v1.json`
- App 圖示原稿：`design/app-icon.svg`
- APK 內建電台清單：`../app/src/main/assets/stations.json`
- Application ID：`com.mark216tw.minitaiwanradio`
- 目前版本：`1.0.0-prerelease`（versionCode 1，測試發行版本）

## 文件維護原則

- 功能行為變更時同步更新 `APP_SPEC.md` 與相關技術文件。
- 電台欄位或載入策略變更時同步更新 `STATION_DATA.md`。
- SDK、Gradle 或依賴版本變更時同步更新 `DEVELOPMENT.md`。
- 權限、網路安全或本機資料政策變更時同步更新 `PRIVACY_SECURITY.md`。
- 正式發布前依 `TEST_PLAN.md` 完成實機與回歸驗證。

## 授權說明

本專案程式碼採 MIT License。MIT License 不涵蓋第三方電台商標、Logo、節目、音訊內容或串流服務；相關權利仍屬原權利人所有。
