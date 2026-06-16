# Shop Manager - Android App

A standalone offline Android app for managing your shop inventory. Built with Kotlin + Room database.

## Features

- **Dashboard** — Total items, categories, average price, stock value, recent items
- **Add/Edit Items** — Name, type, description, price, location, photo
- **Search** — Search by name, type, or description in real-time
- **Camera Integration** — Take photos of items with automatic compression
- **100% Offline** — All data stored locally via SQLite (Room)
- **Material Design** — Clean, modern UI

## Project Structure

```
ShopManager/
├── app/
│   ├── src/main/
│   │   ├── java/com/shopmanager/
│   │   │   ├── MainActivity.kt          # Main activity with all screens
│   │   │   ├── data/
│   │   │   │   ├── Item.kt              # Room entity
│   │   │   │   ├── ItemDao.kt           # Data access object
│   │   │   │   ├── Sale.kt          # Sales tracking entity
│   │   │   │   ├── SaleDao.kt       # Sales DAO
│   │   │   │   └── AppDatabase.kt       # Room database
│   │   │   ├── ui/
│   │   │   │   └── MainViewModel.kt     # ViewModel with LiveData
│   │   │   └── utils/
│   │   │       └── ImageUtils.kt        # Camera + image compression
│   │   ├── res/
│   │   │   ├── layout/
│   │   │   │   ├── activity_main.xml    # Main layout (all 4 screens)
│   │   │   │   └── item_row.xml         # List item row
│   │   │   ├── values/
│   │   │   │   ├── colors.xml
│   │   │   │   ├── strings.xml
│   │   │   │   └── themes.xml
│   │   │   ├── menu/
│   │   │   │   └── bottom_nav_menu.xml
│   │   │   └── xml/
│   │   │       └── file_paths.xml
│   │   └── AndroidManifest.xml
│   └── build.gradle                     # Dependencies
├── build.gradle                         # Project build config
├── settings.gradle
├── gradle.properties
├── gradle/wrapper/
│   └── gradle-wrapper.properties
└── gradlew                              # Build script
```

## How to Build

### Option 1: GitHub Actions (Recommended - Free)

1. Push this project to a GitHub repository
2. Go to **Settings > Secrets and variables > Actions**
3. Add these secrets (optional, for Telegram delivery):
   - `TELEGRAM_BOT_TOKEN` — Your Telegram bot token
   - `TELEGRAM_CHAT_ID` — Your chat ID with the bot
4. Go to **Actions** tab → Click **"Build Shop Manager APK"** → Click **"Run workflow"**
5. Wait ~5 minutes for the build to complete
6. Download the APK from the Artifacts section

### Option 2: Android Studio (Local)

1. Install [Android Studio](https://developer.android.com/studio)
2. Open this project folder
3. Let Gradle sync complete (downloads dependencies)
4. Click **Build > Build Bundle(s) / APK(s) > Build APK(s)**
5. Find the APK at `app/build/outputs/apk/debug/app-debug.apk`

### Option 3: Command Line

```bash
# Set Android SDK path
export ANDROID_HOME=/path/to/android-sdk

# Build
./gradlew assembleDebug

# APK location
# app/build/outputs/apk/debug/app-debug.apk
```

## Tech Stack

| Component | Library |
|-----------|---------|
| Language | Kotlin |
| Database | Room (SQLite) |
| UI | Material Design 3 |
| Image Loading | Glide |
| Camera | CameraX |
| Architecture | MVVM (ViewModel + LiveData) |
| Async | Kotlin Coroutines |

## Item Data Model

| Field | Type | Searchable | Required |
|-------|------|------------|----------|
| name | String | Yes | Yes |
| type | String | Yes | No |
| description | String | Yes | No |
| price | Double | No | No |
| quantity | Int | No | No |
| imagePath | String | No | No |
| location | String | No | No |

## Telegram Setup (Optional)

To get APK builds delivered to Telegram:

1. Message [@BotFather](https://t.me/BotFather) on Telegram
2. Send `/newbot` and follow the prompts
3. Copy the bot token
4. Start a chat with your new bot
5. Get your chat ID by visiting: `https://api.telegram.org/bot<YOUR_TOKEN>/getUpdates`
6. Add both as GitHub repo secrets

## License

Free to use for your shop.
