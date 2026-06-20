# Shop Manager – Polish Plan (Phase 1)

> **Created:** 2026-06-17  
> **Based on:** APP_AUDIT.md and SECURITY_AUDIT.md findings  
> **Target:** Transform from functional prototype → polished, production-quality Android app

---

## Visual Polishing Goals

### Typography System

**Current State:** Arbitrary `textSize` values scattered across XML (11sp, 12sp, 13sp, 14sp, 15sp, 16sp, 18sp, 20sp, 22sp, 28sp). No consistent scale, no defined font family.

**Proposed Type Scale** (Material Design 3 compliant):

| Style Name | Size | Weight | Use Case |
|-----------|------|--------|----------|
| `displayLarge` | 28sp | Bold | Dashboard stat numbers |
| `headlineMedium` | 22sp | Bold | Section headlines, price display |
| `titleLarge` | 20sp | SemiBold | Screen titles, form title |
| `titleMedium` | 16sp | SemiBold | Card titles, row prices |
| `bodyLarge` | 15sp | Normal | Detail text, item names |
| `bodyMedium` | 14sp | Normal | Default body text |
| `bodySmall` | 13sp | Normal | Item count labels |
| `labelLarge` | 14sp | Medium | Button text |
| `labelMedium` | 12sp | Medium | Chip text, secondary info |
| `labelSmall` | 11sp | Medium | Field labels (NAME, TYPE, etc.) |

**Font Family:** Use the system default (Roboto on most Android devices) or consider adding Google Sans / Inter via downloadable fonts.

### Color Palette

**Current State:** Good foundation with indigo primary (#4F46E5). Missing dark mode, missing intermediate shades, no semantic color tokens.

**Proposed Palette:**

| Token | Light Mode | Dark Mode | Usage |
|-------|-----------|-----------|-------|
| `colorPrimary` | #4F46E5 (Indigo 600) | #818CF8 (Indigo 400) | Buttons, links, active states |
| `colorPrimaryContainer` | #EEF2FF (Indigo 50) | #312E81 (Indigo 900) | Card backgrounds, chips |
| `colorOnPrimary` | #FFFFFF | #1E1B4B | Text on primary |
| `colorSurface` | #FFFFFF | #1C1B1F | Card surfaces |
| `colorSurfaceVariant` | #F5F7FA | #2B2930 | Backgrounds |
| `colorOnSurface` | #1E293B | #E6E1E5 | Primary text |
| `colorOnSurfaceVariant` | #64748B | #CAC4D0 | Secondary text |
| `colorOutline` | #E2E8F0 | #49454F | Borders, dividers |
| `colorError` | #EF4444 | #F2B8B5 | Delete, errors |
| `colorSuccess` | #22C55E | #4ADE80 | Positive indicators |

**Action Items:**
- [ ] Create `values-night/colors.xml` for dark mode
- [ ] Create `values-night/themes.xml` for dark mode theme
- [ ] Replace all hardcoded color references with theme attributes

### Spacing & Layout Grid

**Current State:** Inconsistent mix of 4dp, 6dp, 8dp, 12dp, 16dp margins/padding.

**Proposed 4-point grid with preferred multiples of 8:**

| Token | Value | Usage |
|-------|-------|-------|
| `space_xs` | 4dp | Minimal gaps (between label and value) |
| `space_sm` | 8dp | Card padding, list item spacing |
| `space_md` | 12dp | Inner card padding, row padding |
| `space_lg` | 16dp | Screen padding, section spacing |
| `space_xl` | 24dp | Major section breaks |
| `space_xxl` | 32dp | Screen top/bottom padding |

**Action Items:**
- [ ] Create `dimens.xml` with spacing constants
- [ ] Replace all inline margin/padding values with dimension references

### Empty States, Error States, Loading Skeletons

**Current State:** Only text ("No items yet"). No visual empty states, no error states, no loading indicators.

**Proposed:**

| State | Implementation |
|-------|---------------|
| **Empty Dashboard** | Illustration + "Add your first item" CTA button |
| **Empty Items List** | Illustration + "Your inventory is empty" + Add button |
| **Search No Results** | Illustration + "No items match your search" |
| **Loading** | ShimmerFrameLayout skeleton on dashboard cards and item list |
| **Error (generic)** | Snackbar with retry action |
| **Form Validation Error** | Shake animation on invalid fields + TextInputLayout error |

---

## Animation & Micro-interaction Inventory

### Screen Transitions

**Current State:** Instant show/hide via `View.VISIBLE`/`View.GONE`. No animation at all.

**Proposed:**

| Transition | Animation | Duration |
|-----------|-----------|----------|
| Dashboard ↔ Items | Material Fade Through | 300ms |
| Items → Item Detail | Shared element (image + title) | 350ms |
| Items → Add Item | Slide up from bottom | 300ms |
| Add Item → Items (after save) | Fade out + slide down | 250ms |
| Item Detail → Edit | Cross-fade | 200ms |

**Implementation Notes:**
- Since we're not using Fragments/Navigation, transitions can be done with `ViewAnimationUtils`, `TransitionManager`, or custom `ObjectAnimator` sequences
- Consider migrating to Fragments + Navigation component for proper shared element transitions

### Button Press States

| Element | Animation |
|---------|-----------|
| All `MaterialButton` | Scale down to 0.95 + elevation change on press |
| List item rows | Ripple effect (Material default) + subtle scale to 0.98 |
| Bottom nav items | Icon bounce on selection |
| FAB (if added) | Scale + rotation on press |

### Scroll-Driven Effects

| Effect | Screen |
|--------|--------|
| Toolbar collapse with title scaling | Dashboard (via `AppBarLayout` + `CollapsingToolbarLayout`) |
| Parallax hero image | Dashboard header area |
| Floating shadow on scroll | Items list toolbar |
| Pull-to-refresh | Items list (SwipeRefreshLayout already in dependencies) |

### Loading Animations

| Current | Replace With |
|---------|-------------|
| No loading state | Shimmer skeleton placeholders (Facebook-style) |
| No save confirmation | Animated checkmark → navigate |
| Instant `Toast` | Material Snackbar with slide-in animation |

### List Item Animations

| Animation | Implementation |
|-----------|---------------|
| Item enter | Staggered fade-in + slide-up (50ms delay per item) |
| Item removal (delete) | Slide-out left + collapse height |
| Search filter change | `DefaultItemAnimator` with `DiffUtil` |

### Modal/Popup

| Dialog | Animation |
|--------|-----------|
| Delete confirmation | Material Dialog with scale-in |
| Camera permission rationale | Slide-up bottom sheet |

---

## Performance Improvements

### RecyclerView Optimization
- [ ] Replace `RecyclerView.Adapter` with `ListAdapter` + `DiffUtil.ItemCallback`
- [ ] Remove `notifyDataSetChanged()` — use `submitList()` with diff calculation
- [ ] Add `setHasFixedSize(true)` to RecyclerView
- [ ] Add `itemViewPool` for recycled views
- [ ] Add `stateRestorationPolicy = PREVENT_WHEN_EMPTY`

### Image Performance
- [ ] Glide: add `DiskCacheStrategy.ALL` and memory cache sizing
- [ ] Add `thumbnail(0.1f)` for faster perceived loading
- [ ] Consider using `RecyclerView.addOnScrollListener` to pause Glide loads during fast scroll
- [ ] Already using WebP + resize ✅ (ImageUtils handles this well)

### Database Optimization
- [ ] Fix N+1 query in `loadTypes()` — use a single `GROUP BY` query with count
- [ ] Replace `fallbackToDestructiveMigration()` with proper `Migration` objects
- [ ] Add database indexes (already has index on `Sale.itemId` and `Sale.saleDate` ✅)

### Memory & Lifecycle
- [ ] Remove `android:largeHeap="true"` from manifest
- [ ] Ensure proper cleanup of Glide loads on view destroy
- [ ] Use `viewLifecycleOwner` if migrating to Fragments

### View Binding
- [ ] Actually USE the enabled View Binding instead of `findViewById` calls
  - Replace 35+ `findViewById` calls with generated binding class
  - Reduces runtime overhead and improves type safety

### Unused Code Removal
- [ ] `currencyFormat` field — initialized but never used
- [ ] `SwipeRefreshLayout` dependency — imported but not used (remove or implement pull-to-refresh)
- [ ] `Screen` enum in `MainViewModel` — declared but not observed by Activity
- [ ] `navigateTo()` in ViewModel — never called from Activity (Activity has its own)
- [ ] `deleteItemById()` in ViewModel — never called

---

## Security Hardening Roadmap

> [!NOTE]
> This app is 100% offline with no network traffic, no authentication, and no sensitive user data (just shop inventory). Security hardening focuses on data integrity and build safety rather than network/auth security.

### Priority 1: Critical (Before Release)
- [ ] **Add ProGuard/R8 rules** for Room, Glide, CameraX, and Kotlin coroutines to prevent release build crashes
- [ ] **Replace `fallbackToDestructiveMigration()`** with proper Room migration to prevent silent data loss
- [ ] **Add release signing configuration** in build.gradle

### Priority 2: Recommended
- [ ] **Set `android:allowBackup="false"`** or configure `fullBackupContent` to control backup scope
- [ ] **Add input validation** — max length on text fields, price range validation (≥ 0)
- [ ] **Strip EXIF metadata** from captured photos before storing
- [ ] **Handle permanent permission denial** — guide user to app settings

### Priority 3: Optional (For Sensitive Apps)
- [ ] Encrypt database with SQLCipher (only if storing sensitive data)
- [ ] Add root/jailbreak detection
- [ ] Implement app-level PIN/biometric lock
- [ ] Certificate pinning (only relevant if network features are added later)

---

## Hero Image & Visual Assets

### Current State
- **App icon:** Simple indigo square with white document icon — generic and not distinctive
- **No hero images, illustrations, or visual assets** in the app
- **Bottom nav uses deprecated system icons** — outdated look

### Proposed Visual Enhancements

#### App Launcher Icon
- Design a distinctive, modern app icon:
  - Indigo gradient background with rounded super-ellipse shape
  - White storefront/shop bag icon with subtle shadow
  - Material You adaptive icon with proper safe zone

#### Dashboard Hero Section
- Add a hero card at the top of the dashboard with:
  - Gradient background (indigo to purple)
  - Shop name / greeting text
  - Quick stats overlay
  - Parallax scroll effect

#### Empty State Illustrations
- Custom vector illustrations for:
  - Empty inventory (empty shelf/box illustration)
  - No search results (magnifying glass with question mark)
  - Success state (checkmark animation)

#### Bottom Navigation Icons
- Replace system icons with Material Symbols (Outlined, weight 400):
  - Dashboard: `dashboard` or `space_dashboard`
  - Items: `inventory_2` or `package_2`
  - Add: `add_circle`

#### Image Asset Specifications
| Asset | Format | Sizes Needed |
|-------|--------|-------------|
| Launcher icon | Vector (XML) + PNG | mdpi through xxxhdpi + adaptive |
| Bottom nav icons | Vector drawable (XML) | 24dp (single vector) |
| Empty state illustrations | Vector drawable (XML) | Scalable |
| Hero gradient | Shape drawable (XML) | Scalable |

---

## Per-Screen Enhancement Plan

### Screen 1: Dashboard
- [ ] Add hero card with gradient header
- [ ] Redesign stat cards with icons and better hierarchy
- [ ] Add category breakdown with colored chips/bars
- [ ] Recent items: make tappable → navigate to detail
- [ ] Add shimmer loading skeleton
- [ ] Add empty state illustration
- [ ] Collapsing toolbar with parallax

### Screen 2: Items List
- [ ] Redesign search bar with Material 3 SearchBar component
- [ ] Better item cards with rounded image, clearer typography
- [ ] Add swipe-to-delete with undo
- [ ] Add sort/filter options (by name, price, type, date)
- [ ] Pull-to-refresh (utilize existing dependency)
- [ ] Staggered entrance animation
- [ ] Empty state when no items
- [ ] FAB for adding items (in addition to bottom nav)

### Screen 3: Add/Edit Item
- [ ] Better photo section with gallery option (not just camera)
- [ ] Add quantity field to the form (entity supports it but UI doesn't)
- [ ] Category/type dropdown or autocomplete with existing types
- [ ] Form validation animation (shake on error)
- [ ] Save confirmation animation before navigation
- [ ] "Unsaved changes" warning when navigating away

### Screen 4: Item Detail
- [ ] Full-width hero image with gradient overlay
- [ ] Better field layout with icons
- [ ] Share item action (generate text description)
- [ ] Quantity display and quick +/- adjustment
- [ ] Edit FAB instead of inline button
- [ ] Confirm delete with undo snackbar instead of dialog

---

## Verification Plan

### After Each Change Branch
1. Build the app: `./gradlew assembleDebug`
2. Verify no compilation errors
3. Install on device/emulator and test the modified screen
4. Check both light and dark mode (once implemented)
5. Test edge cases (empty data, long text, rapid taps)

### Before Final Merge
1. Full regression: navigate through all screens
2. Verify database operations (add, edit, delete, search)
3. Test camera flow (permission request, capture, compression)
4. Check memory usage with Android Profiler
5. Build release APK and verify R8/ProGuard doesn't break anything
6. Test on multiple screen sizes (phone, small tablet)

---

## Implementation Priority Order

| Phase | Branch | Scope | Risk |
|-------|--------|-------|------|
| **2.1** | `polish/theme` | Design system, colors, typography, dark mode, spacing constants | Low |
| **2.2** | `polish/viewbinding` | Replace all `findViewById` with View Binding | Low |
| **2.3** | `polish/navigation` | Screen transitions, back-stack handling | Medium |
| **2.4** | `polish/recyclerview` | DiffUtil, ListAdapter, item animations | Low |
| **2.5** | `polish/micro-interactions` | Button feedback, scroll effects, toast→snackbar | Low |
| **3.1** | `polish/screen/dashboard` | Hero card, stat redesign, shimmer, empty state | Medium |
| **3.2** | `polish/screen/items` | Search redesign, swipe-delete, sort, FAB | Medium |
| **3.3** | `polish/screen/add-edit` | Form improvements, quantity, validation | Low |
| **3.4** | `polish/screen/detail` | Hero image, layout redesign, share | Low |
| **4.1** | `polish/performance` | Image caching, query optimization, cleanup | Low |
| **4.2** | `polish/security` | ProGuard rules, migration, input validation | Medium |
| **5.0** | `polish/icons-assets` | App icon, nav icons, illustrations | Low |

---

> [!IMPORTANT]
> **This plan requires your review and approval before any code changes begin.**  
> Please review and confirm, or suggest modifications to the scope/priorities.
