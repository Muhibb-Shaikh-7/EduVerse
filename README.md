# EduVerse - AI-Powered Educational Platform

An Android educational application powered by **RunanywhereAI SDK** for privacy-first, on-device AI
capabilities.

## 🌟 Features

- **On-Device AI Chat**: Privacy-first conversational AI running entirely on your device
- **Multiple AI Models**: Choose from 3 different models based on your needs (119 MB - 815 MB)
- **Streaming Responses**: Real-time token generation for immediate feedback
- **Model Management**: Easy download, load, and switch between models
- **Modern UI**: Beautiful Material 3 design with Jetpack Compose
- **Offline Capable**: Works without internet after model download

## 🚀 Quick Start

### Prerequisites

- Android Studio (latest version)
- JDK 17 or higher
- Android device/emulator with Android 7.0+ (API 24+)

### Setup

1. **Clone the repository**
   ```bash
   git clone <your-repo-url>
   cd EduVerse
   ```

2. **Open in Android Studio**
    - Open Android Studio
    - Select "Open an existing project"
    - Navigate to the EduVerse folder

3. **Sync Gradle**
    - Click **File > Sync Project with Gradle Files**
    - Wait 2-3 minutes for first sync (JitPack builds the SDK)
    - Subsequent syncs will be instant

4. **Build and Run**
    - Connect your Android device or start an emulator
    - Click **Run > Run 'app'**
    - The app will launch!

### First Use

1. **Download a Model**
    - Select a model on the Model Selector screen
    - Recommended: Start with "SmolLM2 360M Q8_0" (119 MB)
    - Click "Download" and wait for completion

2. **Load the Model**
    - Click "Load Model" once download completes
    - Wait a few seconds for the model to load into memory

3. **Start Chatting**
    - Type your message in the text field
    - Hit "Send" and watch the AI respond in real-time!

## 📱 Available AI Models

| Model | Size | Memory | Speed | Best For |
|-------|------|--------|-------|----------|
| SmolLM2 360M Q8_0 | 119 MB | ~500 MB | Fast | Testing, quick responses |
| Qwen 2.5 0.5B Instruct | 374 MB | ~800 MB | Medium | General conversations |
| Llama 3.2 1B Instruct | 815 MB | ~1.5 GB | Slower | High-quality chat |

## 🏗️ Architecture

### Technology Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material 3
- **Architecture**: MVVM with ViewModel
- **Async**: Kotlin Coroutines & Flow
- **AI SDK**: RunanywhereAI SDK v0.1.3-alpha
- **LLM Engine**: llama.cpp (7 ARM64 variants)

### Project Structure

```
app/
├── src/main/java/com/example/eduverse/
│   ├── EduVerseApplication.kt    # SDK initialization
│   ├── MainActivity.kt            # Chat UI & ViewModel
│   └── ui/theme/                  # Material 3 theme
├── AndroidManifest.xml            # App configuration
└── build.gradle.kts               # Dependencies
```

## 🔐 Privacy & Security

- **100% On-Device**: All AI processing happens locally
- **No Data Collection**: Zero data sent to external servers
- **Offline Capable**: Works without internet after model download
- **Private Storage**: Models stored in app-private directory

## 🛠️ Development

### Building from Source

```bash
# Clone the repository
git clone <your-repo-url>
cd EduVerse

# Build with Gradle
./gradlew clean assembleDebug

# Install on connected device
./gradlew installDebug
```

### Adding New Models

Edit `EduVerseApplication.kt` and add:

```kotlin
addModelFromURL(
    url = "https://huggingface.co/your-model.gguf",
    name = "Your Model Name",
    type = "LLM"
)
```

## 📖 Documentation

- **[Integration Guide](RUNANYWHERE_INTEGRATION.md)**: Detailed SDK integration documentation
- **[RunanywhereAI SDK](https://github.com/RunanywhereAI/runanywhere-sdks)**: Official SDK
  repository
- *
  *[Android Quick Start](https://github.com/RunanywhereAI/runanywhere-sdks/blob/main/QUICKSTART_ANDROID.md)
  **: SDK quick start guide

## 🐛 Troubleshooting

### Common Issues

**Gradle sync takes too long**

- First sync takes 2-3 minutes (JitPack builds SDK)
- Check internet connection
- Try: `./gradlew clean`

**Model download fails**

- Check internet connection
- Verify storage space available
- Try a smaller model first

**App crashes when loading model**

- Close other apps to free memory
- Try a smaller model
- Verify `android:largeHeap="true"` in manifest

**Generation is slow**

- Normal on low-end devices
- Try SmolLM2 360M (fastest)
- Consider a device with better CPU

See [Integration Guide](RUNANYWHERE_INTEGRATION.md) for more troubleshooting tips.

## 🚀 Future Enhancements

- [ ] Persistent chat history with Room database
- [ ] Custom system prompts for personalized AI behavior
- [ ] Voice input with speech-to-text
- [ ] Multiple conversation threads
- [ ] Export/share conversations
- [ ] Educational quizzes powered by AI
- [ ] Structured learning paths
- [ ] Multi-language support

## 📄 License

This project uses the RunanywhereAI SDK, which is licensed under the Apache License 2.0.

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 💬 Support

- **Discord**: [Join Community](https://discord.gg/pxRkYmWh)
- **Email**: founders@runanywhere.ai
- **GitHub Issues**: [Report Bugs](https://github.com/RunanywhereAI/runanywhere-sdks/issues)

## 🙏 Acknowledgments

- **RunanywhereAI** for the privacy-first on-device AI SDK
- **llama.cpp** for the efficient LLM inference engine
- **HuggingFace** for hosting open-source AI models

---

**Built with ❤️ for Education and Privacy**
