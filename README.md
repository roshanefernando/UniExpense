# UniExpense — Expense Calculator (Kotlin + C++/NDK + JNI + SQLite)

An offline-first student expense tracker. Package: `com.example.uniexpense`.

> **Heads up:** this project was generated outside of Android Studio (no SDK/NDK/network
> available in that environment), so it has **not** been compiled. It was hand-checked for
> consistency (every `@string`, `@drawable`, view-binding id, and JNI function name was
> cross-referenced against its definition — see "What was verified" below), but you should
> still expect to fix the odd Gradle-sync-time nit the first time you open it, the way you
> would with any large project pulled from a fresh checkout.

## 1. How the pieces talk to each other

```
UI (XML + Activities/Fragments)
      |  ViewBinding
      v
Kotlin business/UI layer  <---- JNI ---->  C++ OOP layer (User, Expense, Income, Budget, Report)
      |
      v
Repository classes (UserRepository, ExpenseRepository, IncomeRepository, BudgetRepository)
      |
      v
SQLiteOpenHelper (DatabaseHelper) -> uniexpense.db
```

- **Kotlin owns persistence and UI.** Every screen is an `Activity` extending `BaseActivity`
  (which applies the saved language before `onCreate`, via `attachBaseContext`). Repositories
  wrap raw SQLite `INSERT`/`SELECT`/`UPDATE`/`DELETE` with parameterized queries — nothing is
  string-concatenated into SQL.
- **C++ owns validation and numeric business logic**, reached through a single `NativeBridge`
  object. `Transaction` is an abstract base class; `Expense` and `Income` extend it and override
  `getType()` (polymorphism). `User`, `Budget`, and `Report` round out the OOP layer with
  encapsulated fields, constructors, getters/setters, and composition (a `Report` is assembled
  from numbers `Kotlin` pulled out of `Expense`/`Income` repositories).
- **JNI bridge:** `NativeBridge.kt` declares `external fun`s; `NativeBridge.cpp` implements the
  matching `Java_com_example_uniexpense_NativeBridge_xxx` functions. Names must match exactly —
  that's the #1 cause of `UnsatisfiedLinkError` in JNI projects, so double-check this file first
  if you add new native functions.
- **Login:** email/password validated in Kotlin -> password hashed with `PasswordUtils`
  (`SHA-256` via `java.security.MessageDigest`, salted) -> hash compared against the stored hash
  through `User::login()` in C++ -> on success, `SessionManager` persists the user id in
  `SharedPreferences` so the session survives a restart.
- **Expenses/income and the logged-in user:** every `expenses`/`income`/`budget` row carries a
  `user_id` foreign key, and every repository query filters by it — so `WHERE user_id = ?` is in
  every read, update, and delete. Two different accounts never see each other's rows.
- **Dashboard totals:** `ExpenseRepository`/`IncomeRepository` sum the current month's rows in
  SQL (`SUM(amount)` filtered by `strftime('%Y-%m', date)`); `NativeBridge.nativeCalculateBalance`
  and `nativeCheckBudget` do the arithmetic in C++ (`Report`/`Budget` classes) and hand back
  balance / remaining-budget / percentage-used / exceeded-flag.
- **Reports:** the pie chart groups the current/previous month's expenses by category
  (`ExpenseRepository.getCategoryBreakdown`); the bar chart sums the last 6 months
  (`getMonthlyTotals`). Both charts are custom `View`s (`PieChartView`, `BarChartView`) drawn with
  `Canvas` — no external charting library, so there's nothing extra to resolve from Maven.
- **10-screen navigation:** Splash → Language → Login → Register are a simple `Intent` chain.
  From Dashboard, a `BottomNavigationView` covers Home/History/Add/Budget/Reports; Profile is
  reached from the avatar icon in the Dashboard's top bar (a common pattern for a 5-slot bottom
  nav). "Add Income" is a `BottomSheetDialogFragment` launched from the Dashboard's quick actions,
  as the brief specified (no dedicated screen for it).
- **Language switching:** picking a language on the Language screen (or from Profile → Change
  Language) saves the code in `SharedPreferences`. `BaseActivity.attachBaseContext` wraps the
  activity's `Context` in that locale *before* any string resource is resolved, so every screen —
  not just the ones after the language picker — renders in the selected language immediately.
  Changing it from Profile calls `recreate()` on return so the change is instant, no restart
  needed.
- **Where each OOP concept lives (C++):**
  - *Encapsulation*: all fields in `User`/`Expense`/`Income`/`Budget`/`Report` are `private`,
    accessed only via getters/setters.
  - *Abstraction*: `User` hides whether "login" means a DB lookup or an in-memory compare — the
    caller just gets a `bool`.
  - *Inheritance*: `Expense` and `Income` both extend `Transaction` (the shared "id/user/amount/
    date/note" shape), which was a deliberate architectural choice, not inheritance-for-its-own-
    sake — see the comment at the top of `Transaction.h`.
  - *Polymorphism*: `Transaction::getType()` is pure-virtual; `Expense`/`Income` override it, and
    `Transaction::describe()` calls the overridden version.
  - *Composition*: `Report` is built from raw numbers gathered from `Expense`/`Income` objects,
    not from owning them.

## 2. What was verified (without a compiler)

Since this environment has no Android SDK/NDK, I ran static checks instead of a real build:
- All 42 XML files are well-formed (parsed cleanly).
- Every `@string`/`R.string` reference used anywhere in the layouts or Kotlin resolves to an
  entry in `strings.xml` (107 references, 113 definitions, 0 missing).
- `values-si` and `values-ta` have **exact parity** with `values/strings.xml` — same 113 keys in
  all three, so nothing silently falls back to English.
- Every `@drawable`/`@mipmap` reference resolves to an actual file.
- Every ViewBinding member access (`binding.xxxField`) was cross-checked against the `android:id`
  values in its corresponding layout file.
- JNI function names in `NativeBridge.cpp` were hand-matched 1:1 against the `external fun`
  declarations in `NativeBridge.kt`.

What I could **not** verify here: an actual Gradle sync, an actual NDK/CMake compile, or a Kotlin
compiler pass (no network, no SDK/NDK installed in this sandbox). Treat the first Gradle sync as
your real "does this compile" check.

## 3. Design choices worth knowing about

- **Dashboard "Total Income"/"Total Expenses"** are the **current month's** figures (so they line
  up meaningfully against "Monthly Budget"/"Remaining Budget"). Expense History shows *all* of a
  user's expenses with search/filter/sort. If you'd rather the dashboard show all-time totals,
  change the `month`/`year` args to `null` in `DashboardActivity.loadDashboardData()`.
- **Password reset ("Forgot Password")** is implemented as a fully offline flow (enter your email
  + a new password) since the brief explicitly disallowed a backend/Firebase/email server. It's
  intentionally not "secure" in the way an emailed reset link would be — fine for a local student
  project, worth flagging if this ever goes further than that.
- **Charts** are hand-drawn `Canvas` views rather than a Maven charting library — one less
  external dependency to break your Gradle sync.
- **Sinhala/Tamil translations** are a good-faith machine-assisted translation, not reviewed by a
  native speaker. Every string is translated (no English fallback), but please have someone fluent
  sanity-check the wording before this ships to real users.
- **Launcher icon**: generated from your uploaded logo — the mark was cropped, chroma-keyed to a
  transparent PNG for the adaptive-icon foreground (`res/drawable-nodpi/ic_launcher_foreground.png`,
  background color `@color/brand_navy`), plus flattened legacy PNGs at all 5 mipmap densities. If
  you want pixel-perfect results, re-run it through Android Studio's Image Asset tool
  (right-click `res` → New → Image Asset) using the original logo file — it'll do proper edge
  anti-aliasing that my quick chroma-key script only approximates.

## 4. Opening and running the project

1. **Install prerequisites** (Android Studio Koala/2024.1+ recommended):
   - Open Android Studio → **More Actions → SDK Manager**.
   - *SDK Platforms* tab: install **Android 14.0 (API 34)**.
   - *SDK Tools* tab: check **NDK (Side by side)** → install **26.1.10909125** (matches
     `ndkVersion` in `app/build.gradle`; if Studio only offers a newer patch, either install
     exactly `26.1.10909125` from the list, or edit `ndkVersion` in `app/build.gradle` to match
     what you installed), and check **CMake** → install **3.22.1**.
2. **Open the project**: Android Studio → Open → select the `UniExpense/` folder (the one
   containing `settings.gradle`).
3. **Gradle sync**: Studio will prompt automatically. If not, click the elephant/"Sync Project
   with Gradle Files" icon. First sync needs network access to pull dependencies from
   `google()`/`mavenCentral()`.
4. **Native build**: the C++ layer builds automatically as part of the Gradle build once
   `externalNativeBuild` picks up `app/src/main/cpp/CMakeLists.txt` — no separate step needed.
5. **Run**: create/start an emulator (API 24+, "Google APIs" or "Google Play" image — no Play
   Store dependency though) or plug in a device with USB debugging on, then click **Run ▶**.
6. **First launch**: Splash → Language (pick English/Sinhala/Tamil) → Login → tap "Don't have an
   account? Register" → create an account → you're in.

### If the build fails
- **"NDK not configured" / CMake errors**: double-check the SDK Manager NDK/CMake versions match
  step 1, or edit `ndkVersion` in `app/build.gradle` and the `cmake { version "..." }` block to
  whatever you actually installed.
- **`UnsatisfiedLinkError` at runtime**: means a Kotlin `external fun` name and a C++
  `Java_com_example_uniexpense_NativeBridge_...` name drifted out of sync — compare
  `NativeBridge.kt` and `NativeBridge.cpp` function-by-function.
- **Gradle can't resolve a dependency**: you're offline, or a version in `app/build.gradle` has
  since been pulled from Maven — bump that one line to the next patch version.
- **A resource fails to link (AAPT error)**: most likely a typo introduced by hand-editing; the
  error message names the exact file and line, which is the fastest way to find it.

## 5. Where things live

```
app/src/main/
├── cpp/                  Transaction/User/Expense/Income/Budget/Report + NativeBridge.cpp + CMakeLists.txt
├── java/.../ui/          All 10 screens + BaseActivity + AddIncomeDialog
├── java/.../model/       Data classes (User, Expense, Income, Budget) + category/source lists
├── java/.../database/    DatabaseHelper (schema)
├── java/.../repository/  User/Expense/Income/BudgetRepository (parameterized SQL)
├── java/.../adapter/     ExpenseAdapter (RecyclerView) + PieChartView/BarChartView (Canvas)
├── java/.../utils/       SessionManager, LocaleHelper, PasswordUtils, formatters
├── java/.../NativeBridge.kt   JNI declarations
└── res/                  layouts, values/values-si/values-ta strings, drawables, launcher icons
```
