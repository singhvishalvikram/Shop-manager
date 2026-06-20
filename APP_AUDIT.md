# Shop Manager – Application Audit (Phase 0)

> **Audit Date:** 2026-06-17  
> **Auditor:** Automated Deep Review  
> **App Version:** 2.0 (versionCode 1)  
> **Package:** `com.shopmanager`

---

## 1. Technology Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| **Language** | Kotlin | 1.9.21 |
| **Platform** | Android (single-platform) | API 24–34 |
| **Build System** | Gradle | 8.2.0 (AGP), Wrapper 8.2 |
| **JDK** | Java 17 | — |
| **UI Framework** | XML Views + Material Design 3 | `material:1.11.0` |
| **Database** | Room (SQLite) | 2.6.1 |
| **Image Loading** | Glide | 4.16.0 |
| **Camera** | CameraX | 1.3.1 |
| **Architecture** | MVVM (ViewModel + LiveData + Flow) | lifecycle:2.7.0 |
| **Async** | Kotlin Coroutines | 1.7.3 |
| **View Binding** | Enabled | — |
| **RecyclerView** | AndroidX | 1.3.2 |
| **CardView** | AndroidX | 1.0.0 |
| **ConstraintLayout** | AndroidX | 2.1.4 |
| **SwipeRefreshLayout** | AndroidX | 1.1.0 (imported but not used) |

### Build Features
- **View Binding:** Enabled (but NOT actually used — all views use `findViewById`)
- **KAPT:** Used for Room annotation processor
- **ProGuard/R8:** Enabled for release builds (`minifyEnabled true`)
- **Kotlin KAPT:** For Room compiler

---

## 2. Version Control Status

| Property | Value |
|----------|-------|
| **Git Repo** | ✅ Exists |
| **Current Branch** | `main` |
| **Remote** | `origin/main` (up to date) |
| **Working Tree** | ✅ Clean (no uncommitted changes) |
| **Total Commits** | 10 |

### Recent Git History
```
8ee0f86 Fix lifecycleScope.launch + repeatOnLifecycle pattern and add coroutines import
ea131d7 Fix repeatOnLifecycle and collect imports for StateFlow observation
79ee82d Fix compilation errors: setOnItemClickListener, StateFlow collection, nullable Double handling
a2c44ff Fix code abnormalities: LiveData type mismatches, SaleSummary column aliases, remove hardcoded buildToolsVersion/ndkVersion
98e34f4 Fix gradle-wrapper.jar - proper version with GradleWrapperMain
67102a6 Fix gradlew script - proper APP_HOME detection
0e4cf2d Resolve merge conflict
105ea54 Initial commit: Shop Manager Android app
53baa44 Add files via upload
20d3366 Add files via upload
```

> [!NOTE]
> The commit history suggests the project went through several rounds of compilation fixes after the initial upload. The code is now in a stable state.

---

## 3. App Entry Point

- **Launcher Activity:** `com.shopmanager.MainActivity`
- **Intent Filter:** `MAIN` + `LAUNCHER`
- **Initial Screen:** Dashboard (set in `onCreate` → `navigateTo(Screen.DASHBOARD)`)
- **Theme:** `@style/Theme.ShopManager` (Material3.Light.NoActionBar)

---

## 4. Navigation Structure

### Architecture: **Single-Activity with View Visibility Toggling**

There is **NO** Fragment-based navigation, no Jetpack Navigation, no NavController. All 4 "screens" are `View` containers within a single `activity_main.xml` layout (581 lines). Navigation is achieved by toggling `View.VISIBLE` / `View.GONE` on each screen container.

```
MainActivity (single activity)
├── MaterialToolbar (top bar)
├── FrameLayout (content area)
│   ├── screenDashboard (ScrollView) ──── Dashboard
│   ├── screenItems (LinearLayout) ────── Items List
│   ├── screenAddItem (ScrollView) ────── Add/Edit Item Form
│   └── screenItemDetail (ScrollView) ── Item Detail View
└── BottomNavigationView (bottom bar)
```

### Bottom Navigation Tabs
| Tab | ID | Icon | Action |
|-----|----|------|--------|
| Dashboard | `nav_dashboard` | `ic_menu_sort_by_size` (system) | Show Dashboard |
| Items | `nav_items` | `ic_menu_agenda` (system) | Show Items List |
| Add | `nav_add` | `ic_menu_add` (system) | Show Add Item Form |

### Screen Enum
```kotlin
private enum class Screen { DASHBOARD, ITEMS, ADD_ITEM, ITEM_DETAIL }
```

> [!WARNING]
> **No back-stack management.** The system back button behavior is unhandled — pressing back always exits the app regardless of which "screen" is showing. There's also a duplicate `Screen` enum in both `MainActivity` and `MainViewModel`.

---

## 5. Components & File Paths

### Source Files (8 files)

| File | Path | Purpose | LOC |
|------|------|---------|-----|
| **MainActivity.kt** | `app/src/main/java/com/shopmanager/MainActivity.kt` | Single activity containing all UI logic, adapter, navigation | 454 |
| **MainViewModel.kt** | `app/src/main/java/com/shopmanager/ui/MainViewModel.kt` | MVVM ViewModel with LiveData + StateFlow | 154 |
| **Item.kt** | `app/src/main/java/com/shopmanager/data/Item.kt` | Room `@Entity` for items table | 19 |
| **ItemDao.kt** | `app/src/main/java/com/shopmanager/data/ItemDao.kt` | Room DAO with 15+ query methods | 108 |
| **Sale.kt** | `app/src/main/java/com/shopmanager/data/Sale.kt` | Room `@Entity` for sales table | 26 |
| **SaleDao.kt** | `app/src/main/java/com/shopmanager/data/SaleDao.kt` | Room DAO for sales (basic CRUD) | 30 |
| **AppDatabase.kt** | `app/src/main/java/com/shopmanager/data/AppDatabase.kt` | Room database singleton | 32 |
| **ImageUtils.kt** | `app/src/main/java/com/shopmanager/utils/ImageUtils.kt` | Camera file creation + image compression to WebP | 127 |

### Layout Files (2 files)

| File | Path | Size | Purpose |
|------|------|------|---------|
| **activity_main.xml** | `res/layout/activity_main.xml` | 30KB, 581 lines | Monolithic layout for all 4 screens |
| **item_row.xml** | `res/layout/item_row.xml` | 2KB, 52 lines | RecyclerView item layout |

### Resource Files

| File | Path | Purpose |
|------|------|---------|
| **colors.xml** | `res/values/colors.xml` | 11 color definitions |
| **strings.xml** | `res/values/strings.xml` | 16 string resources |
| **themes.xml** | `res/values/themes.xml` | Single light theme |
| **bottom_nav_menu.xml** | `res/menu/bottom_nav_menu.xml` | 3-tab bottom nav menu |
| **file_paths.xml** | `res/xml/file_paths.xml` | FileProvider paths for camera |
| **ic_launcher_background.xml** | `res/drawable/` | Simple filled rectangle (#4F46E5) |
| **ic_launcher_foreground.xml** | `res/drawable/` | Simple document icon in white |
| **ic_launcher.xml** | `res/mipmap-anydpi-v26/` | Adaptive icon definition |
| **ic_launcher.png** | `res/mipmap-{mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}/` | Pre-rendered launcher icons |

---

## 6. State Management

### Pattern: **MVVM with Mixed LiveData + StateFlow**

| State | Type | Location |
|-------|------|----------|
| `searchQuery` | `MutableStateFlow<String>` | MainViewModel |
| `searchResults` | `StateFlow<List<Item>>` (debounced 300ms) | MainViewModel |
| `allItems` | `Flow<List<Item>>` | MainViewModel (from DAO) |
| `totalCount` | `LiveData<Int>` | MainViewModel (from DAO Flow → asLiveData) |
| `averagePrice` | `LiveData<Double?>` | MainViewModel |
| `totalStockValue` | `LiveData<Double?>` | MainViewModel |
| `recentItems` | `LiveData<List<Item>>` | MainViewModel |
| `types` | `MutableLiveData<List<String>>` | MainViewModel (manually loaded) |
| `typeCounts` | `MutableLiveData<Map<String, Int>>` | MainViewModel (manually loaded) |
| `currentItem` | `MutableLiveData<Item?>` | MainViewModel |
| `currentScreen` | `MutableLiveData<Screen>` | MainViewModel (declared but NOT used by Activity) |
| `editingItemId` | `MutableLiveData<Long?>` | MainViewModel |

> [!IMPORTANT]
> **Mixed reactive paradigms:** The codebase uses both `StateFlow` (for search) and `LiveData` (for everything else), plus a custom `Flow<T>.asLiveData()` extension at the bottom of MainViewModel.kt. This extension re-implements `androidx.lifecycle:lifecycle-livedata-ktx`'s built-in `asLiveData()`.

### Issues Found
1. **`currentScreen` in ViewModel is declared but never observed** — navigation is managed entirely in `MainActivity`
2. **Duplicate `Screen` enum** — exists in both `MainActivity` (private) and `MainViewModel`
3. **`notifyDataSetChanged()`** used in `ItemAdapter.submitList()` instead of `DiffUtil`
4. **No `ListAdapter`** — the adapter is a basic `RecyclerView.Adapter` with manual list management

---

## 7. API Calls, Backend & Storage

### Network/API
- **None.** This is a 100% offline app. No REST API calls, no backend, no network requests.

### Local Storage
- **Database:** Room SQLite database named `shop_manager_db`
  - Tables: `items`, `sales`
  - `fallbackToDestructiveMigration()` is used (⚠️ data loss on schema changes)
- **Image Storage:** Internal files directory at `{filesDir}/images/`
  - Images compressed to WebP format, max 1024px, 80% quality
  - Average output: 50–150KB per image

### Authentication
- **None.** No login, no user accounts, no tokens.

---

## 8. Permissions

| Permission | Declared In | Justified? |
|------------|-------------|------------|
| `CAMERA` | AndroidManifest.xml | ✅ Yes — used for item photos |
| `android.hardware.camera` | AndroidManifest.xml (feature, `required=false`) | ✅ Yes — optional camera feature |

> [!NOTE]
> No storage permissions are needed — the app uses internal storage (`filesDir`) which doesn't require runtime permissions. The `CAMERA` permission is requested at runtime with proper rationale dialog.

---

## 9. Identified Issues & Jank

### Architecture Issues
1. **God Activity:** `MainActivity.kt` (454 lines) contains ALL UI logic, the RecyclerView adapter, navigation, form handling, camera logic — everything
2. **Monolithic layout:** `activity_main.xml` (581 lines, 30KB) contains all 4 screens in a single file
3. **No Fragments/Navigation:** View visibility toggling is fragile, has no back-stack, no transitions
4. **No `DiffUtil`:** Uses `notifyDataSetChanged()` causing full list re-renders
5. **View Binding declared but unused:** Build config enables viewBinding but all views use `findViewById`
6. **SwipeRefreshLayout imported but unused** in dependencies

### UI/UX Issues
7. **No screen transitions** — screens appear/disappear instantly (GONE ↔ VISIBLE)
8. **No loading states** — no progress indicators, no skeleton screens
9. **No empty states** — lists just show nothing when empty (dashboard shows "No items yet" text only)
10. **No error states** — only one `inputName.error` validation, no general error handling
11. **No tap feedback** on list items (no ripple, no scale, no press state)
12. **System icons used for bottom nav** (`ic_menu_sort_by_size`, `ic_menu_agenda`, `ic_menu_add`) — these are deprecated and look outdated
13. **System icons used for search** (`ic_menu_search`, `ic_menu_close_clear_cancel`)
14. **No dark mode** — only light theme defined
15. **Hardcoded strings in XML:** "Type Breakdown", "Recently Added", "NAME", "TYPE", "DESCRIPTION", "PRICE", "LOCATION", "Add New Item", "Save Item", "← Back to Items", etc.
16. **Inconsistent spacing:** Mix of 4dp, 6dp, 8dp, 12dp, 16dp margins without a consistent scale
17. **Missing `contentDescription`** on images (accessibility issue)
18. **No pull-to-refresh** despite having SwipeRefreshLayout dependency
19. **Price hardcoded for Indian Rupees** (`₹` symbol hardcoded, `Locale("en", "IN")`)

### Data/Logic Issues
20. **`saveItem()` navigates away without confirming success** — Toast appears but navigation is in ViewModel
21. **After saving, form is NOT cleared** — the `clearEditing()` in ViewModel doesn't clear the actual form fields
22. **`quantity` field exists in Item entity but has no UI** — can't be set from the Add/Edit form (always 0)
23. **Sales entity/DAO defined but never used in UI** — no sales screen, no way to record sales
24. **`SearchInput` declared as `EditText` in layout but cast to `TextInputEditText`** in Activity — works but inconsistent
25. **`currencyFormat` initialized but never used** — `formatPrice()` uses manual string formatting instead
26. **Search doesn't use Flow for live updates** — uses `suspend fun searchItems()` wrapped in `flow { emit() }`, so search results don't auto-update when data changes

### Performance Issues
27. **`largeHeap="true"`** in manifest — potential OOM mask, should be removed and proper memory management used
28. **No RecyclerView optimizations** — no `setHasFixedSize(true)`, no item view pool, no `stateRestorationPolicy`
29. **Dashboard `loadTypes()` makes N+1 queries** — one query for types list, then one query per type for count

---

## 10. Project Structure Summary

```
Shop-manager/ (root)
├── .git/
├── .github/workflows/build-apk.yml      # GitHub Actions CI
├── .gitignore
├── README.md
├── build.gradle                          # Root build config (AGP 8.2.0, Kotlin 1.9.21)
├── settings.gradle                       # rootProject.name = 'ShopManager'
├── gradle.properties                     # AndroidX, Jetifier, JVM args
├── gradle/wrapper/gradle-wrapper.properties
├── gradlew
└── app/
    ├── build.gradle                      # App dependencies (14 deps)
    └── src/main/
        ├── AndroidManifest.xml           # Single activity, FileProvider
        ├── java/com/shopmanager/
        │   ├── MainActivity.kt           # God activity (454 LOC)
        │   ├── data/
        │   │   ├── Item.kt               # Room entity
        │   │   ├── ItemDao.kt            # DAO (108 LOC, 15+ queries)
        │   │   ├── Sale.kt               # Room entity (unused in UI)
        │   │   ├── SaleDao.kt            # DAO (unused in UI)
        │   │   └── AppDatabase.kt        # Singleton database
        │   ├── ui/
        │   │   └── MainViewModel.kt      # MVVM ViewModel (154 LOC)
        │   └── utils/
        │       └── ImageUtils.kt         # Camera + compression (127 LOC)
        └── res/
            ├── drawable/                 # 2 vector drawables (launcher icons)
            ├── layout/                   # 2 layouts (activity_main + item_row)
            ├── menu/                     # 1 bottom nav menu
            ├── mipmap-*/                 # Launcher icons (5 density buckets + adaptive)
            ├── values/                   # colors, strings, themes
            └── xml/                      # FileProvider paths
```

**Total source files:** 8 Kotlin files  
**Total lines of Kotlin:** ~950 LOC  
**Total layout XML lines:** ~633 lines  
**Total dependencies:** 14 (all AndroidX/Google)
