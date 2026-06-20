# Shop Manager – Security Audit (Phase 0.2)

> **Audit Date:** 2026-06-17  
> **App Type:** 100% Offline Android App  
> **Risk Level:** 🟢 LOW (no network, no auth, no remote data)

---

## 1. Authentication Flow

### Finding: **No authentication exists**

This is a standalone offline app with no user accounts, no login, no token management. All data is stored locally and accessible only within the app sandbox.

| Aspect | Status |
|--------|--------|
| Login/Signup | ❌ Not implemented (not needed for use case) |
| Token storage | N/A |
| API headers | N/A |
| Refresh logic | N/A |
| Session management | N/A |

> [!NOTE]
> For a personal shop inventory app running entirely offline, the lack of authentication is acceptable. However, if the app later adds multi-user support or cloud sync, authentication will become critical.

---

## 2. Hardcoded Secrets

### Finding: ✅ **No hardcoded secrets found**

A thorough search for API keys, tokens, passwords, private URLs, and secret values found **zero results**.

```
Searched patterns: api_key, API_KEY, secret, token, password, apikey
Result: No matches in any source file
```

The GitHub Actions workflow uses `${{ secrets.TELEGRAM_BOT_TOKEN }}` and `${{ secrets.TELEGRAM_CHAT_ID }}` which are properly stored as GitHub repository secrets — not hardcoded.

---

## 3. Network Security

### Finding: ✅ **No network attack surface**

| Check | Result |
|-------|--------|
| HTTP URLs in source | Only XML namespace URIs (standard Android) |
| HTTPS URLs in source | None |
| API endpoints | None |
| Network requests | None |
| Certificate pinning | N/A (no network) |
| `INTERNET` permission | ❌ Not declared (correctly omitted) |
| `android:usesCleartextTraffic` | Not set (defaults to false on API 28+) |

> [!TIP]
> The app is entirely offline by design. No network security measures are needed at this time.

---

## 4. Input Validation

### Finding: ⚠️ **Minimal input validation**

| Form Field | Validation | Issue |
|------------|-----------|-------|
| `inputName` (Item Name) | ✅ Empty check with error message | Only required field |
| `inputType` | ❌ No validation | Accepts any text |
| `inputDescription` | ❌ No validation | Accepts any text (multiline) |
| `inputPrice` | ⚠️ `toDoubleOrNull() ?: 0.0` — no range check | Accepts negative numbers, extremely large values |
| `inputLocation` | ❌ No validation | Accepts any text |

### Specific Concerns
1. **No max length enforcement** on any text field — could lead to UI overflow
2. **No price range validation** — negative prices are accepted silently
3. **No SQL injection risk** — Room uses parameterized queries (safe)
4. **No XSS risk** — no WebView, no HTML rendering
5. **Search input has no debouncing bypass** — properly debounced at 300ms in ViewModel

### Recommendation
- Add `android:maxLength` to text inputs in XML
- Validate price is ≥ 0 and within a reasonable range
- Add input sanitization (trim whitespace, etc.) — partially done with `.trim()`

---

## 5. Data Storage Security

### Finding: ⚠️ **Data stored in plaintext (acceptable for offline app)**

| Storage | Location | Encrypted? | Concern |
|---------|----------|------------|---------|
| SQLite Database | `{app_data}/databases/shop_manager_db` | ❌ No | Accessible only to app (sandboxed) |
| Item Images | `{app_data}/files/images/` | ❌ No | Accessible only to app (sandboxed) |
| Shared Preferences | Not used | N/A | — |

### Database Security
- **`fallbackToDestructiveMigration()`** is used — on schema version changes, **ALL DATA IS DESTROYED** without warning
- No database encryption (SQLCipher not used)
- No data export/backup encryption
- Database file is accessible on rooted devices

### Image Security
- Images stored in internal storage (`filesDir`) — not accessible to other apps
- Original camera photos are deleted after compression ✅
- No EXIF data stripping (WebP compression may retain some metadata)

> [!WARNING]
> **`fallbackToDestructiveMigration()`** is a significant data loss risk. Any future database schema change will silently delete all user data. This should be replaced with proper migration scripts.

---

## 6. App Permissions

| Permission | Type | Requested At | Justified? |
|------------|------|-------------|------------|
| `CAMERA` | Dangerous (runtime) | When user taps "Take Photo" | ✅ Yes |
| `android.hardware.camera` | Feature declaration (`required=false`) | Manifest | ✅ Yes (app works without camera) |

### Permission Handling
- ✅ Runtime permission request with `ActivityResultContracts.RequestPermission()`
- ✅ Shows rationale dialog via `shouldShowRequestPermissionRationale()`
- ✅ Graceful degradation — app works without camera permission
- ❌ No handling for "Don't ask again" scenario — user gets stuck if they permanently deny

---

## 7. Build & Release Security

| Check | Status | Notes |
|-------|--------|-------|
| **ProGuard/R8** | ✅ Enabled for release | `minifyEnabled true` |
| **ProGuard Rules** | ⚠️ Uses default only | `proguard-android-optimize.txt` — no app-specific rules |
| **Debuggable in release** | ✅ Off by default | AGP default for release builds |
| **Signing config** | ❌ Not configured | No release signing key defined in build.gradle |
| **Code obfuscation** | ✅ Via R8 | Default with `proguard-android-optimize.txt` |

### Missing Release Configurations
- No `signingConfigs` block — can't build signed release APK
- No ProGuard rules for Room, Glide, or CameraX (may cause runtime crashes in release)
- No `ndk.debugSymbolLevel` for crash reporting

> [!CAUTION]
> **Missing ProGuard/R8 rules** — The release build enables minification but has no rules to keep Room entities, Glide's generated API, or CameraX classes. This will likely cause `ClassNotFoundException` or `NoSuchMethodException` in release builds.

---

## 8. Manifest Security

| Check | Status |
|-------|--------|
| `android:allowBackup="true"` | ⚠️ Allows ADB backup of app data |
| `android:exported="true"` (MainActivity) | ⚠️ But necessary for launcher activity |
| `android:largeHeap="true"` | ⚠️ Potential OOM masking |
| `android:supportsRtl="true"` | ✅ Good for internationalization |
| No `android:networkSecurityConfig` | ✅ OK — no network used |

### Recommendations
- Consider `android:allowBackup="false"` or use `android:fullBackupContent` to control what's backed up
- Review if `largeHeap` is actually needed — proper image management may eliminate the need

---

## 9. Third-Party Dependencies

All dependencies are from Google/JetBrains (trusted sources):

| Dependency | Source | Known Vulnerabilities |
|------------|--------|----------------------|
| `core-ktx:1.12.0` | Google (AndroidX) | None known |
| `appcompat:1.6.1` | Google (AndroidX) | None known |
| `material:1.11.0` | Google (Material) | None known |
| `constraintlayout:2.1.4` | Google (AndroidX) | None known |
| `swiperefreshlayout:1.1.0` | Google (AndroidX) | None known |
| `room-*:2.6.1` | Google (AndroidX) | None known |
| `lifecycle-*:2.7.0` | Google (AndroidX) | None known |
| `kotlinx-coroutines:1.7.3` | JetBrains | None known |
| `glide:4.16.0` | BumpTech (Google-maintained) | None known |
| `camera-*:1.3.1` | Google (AndroidX) | None known |
| `recyclerview:1.3.2` | Google (AndroidX) | None known |
| `cardview:1.0.0` | Google (AndroidX) | None known |

> [!TIP]
> All dependencies are well-maintained and from trusted sources. No third-party SDKs with tracking, analytics, or ad networks are present.

---

## 10. Security Score Summary

| Category | Score | Notes |
|----------|-------|-------|
| Secrets Management | ✅ 10/10 | No hardcoded secrets |
| Network Security | ✅ 10/10 | No network surface |
| Input Validation | ⚠️ 5/10 | Minimal validation on forms |
| Data Storage | ⚠️ 6/10 | Plaintext but sandboxed; destructive migration risk |
| Permissions | ✅ 8/10 | Properly scoped; minor UX gap on permanent denial |
| Build Security | ⚠️ 5/10 | ProGuard enabled but missing rules; no signing config |
| Manifest Security | ⚠️ 7/10 | `allowBackup` and `largeHeap` flags |
| Dependencies | ✅ 10/10 | All trusted, no known vulnerabilities |

### **Overall Risk: 🟢 LOW**

The app has a minimal attack surface due to its fully offline nature. The main risks are data-related (destructive migration, backup exposure) rather than security exploitation vectors. Priority improvements should focus on ProGuard rules (to prevent release build crashes) and input validation.
