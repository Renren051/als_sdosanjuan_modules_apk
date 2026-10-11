# DepEd SDO San Juan Alternative Learning System (ALS) Mobile LMS

A native Android mobile application built in Kotlin using **Jetpack Compose**, **MVVM Clean Architecture**, and **Material 3 Design** for the **DepEd SDO San Juan Alternative Learning System (ALS)**. The mobile LMS seamlessly connects to an existing local Laravel 12 backend via Laravel Sanctum REST APIs.

---

## 🌟 Key Features

1. **Authentication**:
   - Secure learner & teacher login via Laravel Sanctum Bearer tokens.
   - Encrypted persistent session storage using `EncryptedSharedPreferences`.
   - Logout protection preventing state loops.

2. **Curriculum & Self-Learning Modules**:
   - Interactive cards for the 6 official ALS Learning Strands with module counts.
   - Filter modules by strand, level (*BLP*, *Elementary*, *Junior High*), or real-time keyword search.
   - Direct PDF streaming & offline download caching.

3. **Offline Telemetry & Progress Sync**:
   - Offline SQLite persistence via `AppDbHelper` tracking learner progress (`last_page_read`, `completion_status`, timestamps).
   - Automatic WorkManager `SyncWorker` uploading cached learner progress to `/api/v1/learner/sync-progress` whenever network connectivity is restored.

4. **ALSJay — DepEd AI Virtual Assistant**:
   - Bilingual AI assistant interface (Tagalog & English).
   - Interactive quick-suggestion prompt chips (e.g., *"Paano po mag-enroll sa ALS?"*, *"Ano ang mga Learning Strands?"*).

5. **UI / UX & Theme**:
   - Official DepEd color scheme: `#0038A8` DepEd Blue, `#CE1126` DepEd Red, `#FCD116` DepEd Yellow.
   - In-app **Dark Mode & Light Mode** toggle with persistent user preference.
   - High-resolution DepEd ALS & SDO San Juan branding logos.

---

## 📋 Prerequisites

- **Android Studio** (2024.1+ / Ladybug / Koala)
- **JDK 11** or **JDK 17**
- **Android SDK 37** (Minimum SDK: 24 / Android 7.0 Nougat)
- **Laravel 12 Backend** running locally on your computer

---

## 🌐 Backend & Network Setup

Before running the Android application, start your local Laravel backend server so it accepts requests from the Android Emulator or physical devices:

```bash
php artisan serve --host=0.0.0.0 --port=8000
```

> [!IMPORTANT]
> The `--host=0.0.0.0` flag allows your local Laravel server to listen for network requests outside `127.0.0.1`.

### Network Base URL Configuration

Open `app/src/main/java/com/jannlawm/alssdosanjuan/data/remote/Constants.kt` and set `currentBaseUrl` based on your testing environment:

```kotlin
object Constants {
    // For Android Emulator (10.0.2.2 points to host PC's localhost):
    const val BASE_URL_EMULATOR = "http://10.0.2.2:8000/api/v1/"

    // For Physical Device (replace with your host PC's Wi-Fi IP address):
    const val BASE_URL_DEVICE = "http://192.168.1.15:8000/api/v1/"

    // Set current active base URL
    var currentBaseUrl: String = BASE_URL_EMULATOR
}
```

---

## 🛠️ How to Build & Generate APK

### Option 1: Via Android Studio IDE
1. Open the project in **Android Studio**.
2. Perform a **Gradle Sync** if prompted.
3. In the top toolbar, go to **Build > Build APK(s) / Bundle(s) > Build APK(s)**.
4. Once finished, Android Studio will display a notification with a link to locate `app-debug.apk`.

### Option 2: Via Terminal / Command Line
Run the Gradle assemble task from the root project folder:

**Windows (PowerShell / CMD):**
```powershell
.\gradlew.bat assembleDebug
```

**Mac / Linux:**
```bash
./gradlew assembleDebug
```

### 📍 APK File Location
Once built, your debug APK file is generated at:
```
app/build/outputs/apk/debug/app-debug.apk
```

---

## 📲 How to Install & Run on Device / Emulator

### Via ADB Command Line
Connect your Android device via USB (with USB Debugging enabled) or start an Android Emulator, then run:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Via Android Studio
Click the **Run** button (green play icon) in Android Studio to build, deploy, and launch the application directly on your connected device or emulator.

---

## 🏛️ Project Architecture & Tech Stack

```
com.jannlawm.alssdosanjuan
 ├── data
 │    ├── local       # SQLite AppDbHelper & EncryptedSharedPreferences (TokenManager)
 │    ├── model       # Data models (Auth, Modules, Telemetry, Chat)
 │    ├── remote      # Retrofit ApiClient, ApiService, AuthInterceptor, Constants
 │    ├── repository  # Auth, Module, Telemetry, Chat Repositories
 │    └── worker      # WorkManager SyncWorker (Offline telemetry progress sync)
 ├── ui
 │    ├── chat        # ALSJay AI Chat Screen & ViewModel
 │    ├── dashboard   # Learning Strands & Modules Dashboard & ViewModel
 │    ├── login       # Login Screen & ViewModel
 │    ├── module      # Module Reader & Progress Tracker Screen & ViewModel
 │    └── theme       # DepEd Material 3 Colors, Type, and Theme
 └── MainActivity.kt # Compose NavHost & App Lifecycle Entry Point
```

---

## 📄 License & Attribution

Developed for **DepEd Division of City Schools - SDO San Juan Alternative Learning System (ALS)**. All logos and trademarks belong to the Department of Education (DepEd) Philippines.
