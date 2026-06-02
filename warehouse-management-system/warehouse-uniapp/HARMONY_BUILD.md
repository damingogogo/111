# 鸿蒙打包说明

## 先判断系统版本

- HarmonyOS 4/5：可以继续安装 Android APK。
- HarmonyOS NEXT/鸿蒙 6：不能安装 APK，需要在 HBuilderX 里选择“运行到鸿蒙”或“App-Harmony 本地打包”，生成鸿蒙包。

## 已做兼容

- `main.js` 已改成 Vue3 入口，满足 uni-app 鸿蒙运行要求。
- `manifest.json` 已增加 `app-harmony` 配置，包名为 `com.danzi.warehouse`。
- `.hbuilderx/launch.json` 已把鸿蒙工程目录指定到 `D:/harmony-warehouse/dev` 和 `D:/harmony-warehouse/build`，避免中文目录和长路径导致构建/安装异常。

## HBuilderX 操作

1. 用 HBuilderX 打开 `warehouse-uniapp`。
2. 如果目标设备是 HarmonyOS NEXT/鸿蒙 6，不要选择 Android APK 云打包。
3. 打开菜单：运行 -> 运行到手机或模拟器 -> 运行到鸿蒙。
4. 首次运行建议选择清空缓存，重新构建。
5. 如果需要发行包，使用 App-Harmony 本地打包，并按 HBuilderX 提示配置鸿蒙签名证书。

## Android APK 仍可用

Android APK 仍适用于安卓手机和支持 APK 的鸿蒙设备。若在 HarmonyOS NEXT 上点击 APK 安装没有反应，这是系统不支持 APK 的表现，不是应用代码按钮问题。
