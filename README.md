# Replify

Replify is an intelligent AI reply assistant designed to streamline your communication across various chat applications. It provides a non-intrusive, floating interface that helps you generate and paste smart replies instantly.

## Features

- **Floating Bubble UI**: Quick access to AI assistance without leaving your current chat app.
- **Smart Context Extraction**: Automatically identifies the last received message in popular apps like WhatsApp using advanced accessibility heuristics.
- **Seamless Integration**: Generates replies and pastes them directly into the input field for you.
- **Dismissible Interface**: Easily hide the assistant by dragging the bubble to the bottom of the screen.
- **Privacy Focused**: Operates via Android Accessibility Services to understand context locally on your device.

## How it Works

1. **Enable Permissions**: Replify requires Accessibility Service and Overlay permissions to function.
2. **Open a Chat**: Navigate to any messaging app (e.g., WhatsApp).
3. **Tap the Bubble**: When you want to reply, tap the floating Replify bubble.
4. **Generate & Send**: The AI analyzes the context, suggests a reply, and can even auto-send it if configured.

## Technical Details

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **AI Engine**: Powered by Gemini API via Google AI Studio
- **Architecture**: MVVM with Foreground Services for the floating UI

## Development & Build

To build the project locally:

1. Clone the repository.
2. Open in Android Studio.
3. Sync Gradle and build the project.
4. Ensure you have a Gemini API key configured if you are running your own backend.

## License

This project is for personal use and creative exploration.
