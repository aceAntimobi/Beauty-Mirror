# Beauty Mirror

Beauty Mirror 是一款 Android 化妆镜应用，可以通过前置摄像头实时预览妆效，并提供辅助线和滤镜帮助用户快速完妆。

## 功能特性

- 📸 **实时相机预览**：使用 CameraX 显示前置摄像头画面。
- ✨ **化妆滤镜**：提供自然光泽、冷调修容、暖阳蜜桃三种柔和色调滤镜。
- 📐 **面部辅助线**：可选的黄金比例辅助线帮助对齐眼妆与唇妆位置。
- 💡 **化妆提示**：弹窗分享底妆、腮红、唇妆的小技巧。
- 🔐 **权限处理**：友好的相机权限请求与说明界面。

## 项目结构

```
Beauty-Mirror/
├── app/
│   ├── build.gradle.kts       # 应用模块构建配置
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/beautymirror/
│       │   ├── MainActivity.kt
│       │   └── ui/theme/
│       │       ├── Color.kt
│       │       ├── Theme.kt
│       │       └── Type.kt
│       └── res/
│           ├── drawable/ic_mirror.xml
│           └── values*/        # 主题、字符串、颜色等资源
├── build.gradle.kts
├── gradle.properties
└── settings.gradle.kts
```

## 开发环境

- Android Studio Iguana 或以上版本
- Android Gradle Plugin 8.3+ / Gradle 8.4+
- Kotlin 1.9+
- 最低支持 Android 7.0 (API 24)

克隆项目后使用 Android Studio 打开根目录即可同步依赖并运行到设备或模拟器（建议使用具备前置摄像头的真机以获得最佳体验）。

## 许可

该示例项目仅用于演示目的，可在学习与个人项目中自由使用。
