# mini台灣電台 App 圖示

## 設計規格

- 原始向量稿：[app-icon.svg](app-icon.svg)
- 主畫布：`108 x 108 dp`
- 外框裁切區：`72 x 72 dp`，座標 `18..90`
- 核心安全區：`66 x 66 dp`，座標 `21..87`
- Android 前景與背景資源位於專案根目錄的 `app/src/main/res/`。

## 視覺元素

- 暖黃色背景：`#FFF1B8`
- 深藍色收音機外框：`#12304A`
- 珊瑚紅內圈電波：`#FF765F`
- 天空藍外圈長波紋：`#3D91D4`
- 圖案由收音機、天線、訊號燈與兩道廣播電波組成。

前景保持透明，包含完整收音機與電波圖案；背景資源只提供暖黃色底層。珊瑚紅中心與電波均屬前景，使 Adaptive Icon 在不同啟動器裁切形狀下維持一致。

## 資源對應

| 用途 | 路徑 |
|---|---|
| SVG 原稿 | [`app-icon.svg`](app-icon.svg) |
| Android 前景 | [`ic_launcher_foreground.xml`](../../app/src/main/res/drawable/ic_launcher_foreground.xml) |
| Android 背景 | [`ic_launcher_background.xml`](../../app/src/main/res/drawable/ic_launcher_background.xml) |
| Adaptive Icon | [`ic_launcher.xml`](../../app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml) |

SVG 是設計原稿，Android Vector Drawable 是實際執行資源。兩者使用相同構圖與色彩，但 Android 資源可為 Adaptive Icon 的視覺置中進行位置微調。修改顏色或主要路徑時應同步檢查兩者，並在圓形、圓角方形等不同 Launcher 遮罩下確認安全區沒有被裁切。
