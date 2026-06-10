# OneLife ⏳ for Android

Android companion of [OneLife](https://github.com/rjxznb/dead-clock) — a life countdown app that turns your remaining time into motivation.

Kotlin + Jetpack Compose · minSdk 26 (Android 8.0+) · English & 简体中文

## Features (v1)

- Live countdown with rainbow-gradient digits (0.1s tick), units breakdown, life progress bar
- Four themes: Dark / Light / Flowing gradient / Alert red
- Daily check-in journal with streak tracking
- Bedtime reminder via exact alarms (auto-silenced once checked in)

Planned: posters, home screen widget (Glance), photo slideshow theme.

## Build

```bash
gradle assembleDebug
# APK at app/build/outputs/apk/debug/app-debug.apk
```

CI builds a debug APK on every push (Actions artifact `OneLife-debug-apk`).

Install on device: enable USB debugging, then `adb install app-debug.apk` — or copy the APK to the phone and open it.

> Huawei/EMUI note: for reliable reminders, allow the app to auto-launch and exempt it from battery optimization (Settings → Battery → App launch).

---

🤖 Built with [Claude Code](https://claude.com/claude-code)
