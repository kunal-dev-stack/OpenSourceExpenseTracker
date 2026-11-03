# Easy Expense Tracker (Android) 📱💰
**Developed by Kunal Turkar**

A modern, high-performance, **100% offline-first** personal finance and expense tracker built for Android with **Jetpack Compose**, **Material Design 3**, **Room SQLite**, and **Native PDF Statement Generation**.

Engineered from an in-depth analysis of top Google Play Store finance apps (**MyMoney**, **1Money**, **Cashew**, **Spendee**), **Easy Expense Tracker** unites the speed of 1Money's instant calculator keypad with Cashew's flexible visual budgets, multi-account transfers, native PDF invoice statements, and an interactive dark & light theme system.

---

## 🌟 Key Highlights & Competitive Features

### 1. 📅 Custom Monthly Budgets & Spending Goals
- **Any Month Navigation**: Easily navigate forward and backward (`< November 2026 >`) or set budgets for any custom month and year.
- **Total Monthly Spending Goal**: Set an overarching monthly ceiling to track your total spending velocity in real-time.
- **Category-Wise Budgets**: Configure custom limits per expense category with real-time progress indicators, remaining funds, and status badges (*Safe*, *Warning*, *Exceeded*).
- **Edit & Delete Budgets**: One-tap editing to update monthly limits, delete old budgets, or recalculate thresholds instantly.

### 2. 🎨 Interactive Dark & Light Theme System
- **Light Theme**: Modern paper-white background (`#FFFFFF`), subtle slate borders (`#E2E8F0`), high-contrast typography, and vibrant emerald accents.
- **Dark Theme**: Deep stealth dark (`#0B0F17`) and AMOLED Pure Black mode for OLED displays to maximize battery life.
- **Dynamic Color Support**: Optional Material You dynamic theming based on system wallpaper (Android 12+).
- **7 Accent Color Themes**: Emerald Green, Sapphire Blue, Royal Violet, Sunset Orange, Ruby Crimson, Cyber Teal, and Rose Pink.
- **Privacy Mode**: One-tap toggle to mask balances and numbers with `••••••` on the dashboard to protect your financial privacy in public.

### 3. ⚙️ Extensive Settings Customization
- **15+ World Currencies**: Indian Rupee (₹), US Dollar ($), Euro (€), British Pound (£), Japanese Yen (¥), Canadian Dollar (C$), Australian Dollar (A$), South Korean Won (₩), UAE Dirham (د.إ), Swiss Franc (CHF), Singapore Dollar (S$), Thai Baht (฿), and more.
- **Flexible Date Formats**: `dd MMM yyyy`, `EEE, dd MMM yyyy`, `dd/MM/yyyy`, `MM/dd/yyyy`, `yyyy-MM-dd`.
- **Decimal Precision Toggle**: Switch between clean integers (`₹1,250`) and precision decimals (`₹1,250.00`).
- **Custom Category Manager**: Create personalized expense and income categories with 28+ icons and 16+ color swatches.
- **Configurable Alert Thresholds**: Adjust budget warnings (70%, 75%, 80%, 85%, 90%) and low-balance triggers.
- **About Section**: Full developer attribution to **Kunal Turkar**, version info, and 100% offline-first pledge.

### 4. ⚡ 3-Second Transaction Logging
- Built-in **financial keypad with instant arithmetic** (+, -, =, decimal, backspace).
- **Preset Quick-Add chips** (`+100`, `+500`, `+1000`) for rapid entry.
- Direct **Type Selector** (Expense, Income, Transfer) with instant UI color shifts.
- Dynamic category grid with custom color-coded iconography.

### 5. 📄 Professional PDF Statement & CSV Export
- **One-Tap PDF Report Generator**: Generates an invoice-grade statement formatted with:
  - Header & statement period range.
  - Consolidated Net Worth, Total Inflow, Total Outflow, and Savings Rate %.
  - Top category expenditure breakdown table.
  - Itemized transaction ledger with dates, categories, accounts, notes, and colored amounts (+ Green, - Red).
  - Developer attribution: *"Easy Expense Tracker • Developed by Kunal Turkar"*.
- **Direct Android Print / Share Intent**: Instantly share via WhatsApp, Gmail, Drive, or send directly to a wireless printer.
- **RFC 4180 CSV Data Sheet**: Export all transactions to CSV for Microsoft Excel, Google Sheets, or tax filing.

### 6. 🔔 Smart Notifications & "Add Money" Alerts
- **Configurable Daily Reminder**: Prompts you at your chosen evening hour (e.g., 20:30) with an actionable *"Log Today's Spending"* notification.
- **"Add Money" Low-Balance Alert**: Monitors your accounts (Cash, Bank Checking, Savings). If an account drops below its safety threshold, sends an alert: *"⚠️ Cash is low! Tap to add funds."*
- **Budget Threshold Alerts**: Warns you when spending reaches your configured percentage of your monthly limit.
- **Boot Resilient**: Uses `RECEIVE_BOOT_COMPLETED` so alarms survive device restarts.
- **Interactive Testing Hub**: Live test buttons in the Budgets screen allow triggering and testing all notifications on your device.

### 7. 💼 Multi-Account & Atomic Transfers
- Unlimited accounts: **Cash**, **Bank Checking**, **Savings**, **Credit Card**, and **Digital Wallets**.
- Transfer money between accounts with guaranteed atomic consistency (deducts from source, credits to destination with zero double-counting).
- Atomic editing and deletion: Reverts old transaction impact and applies new balance adjustments automatically.

### 8. 🔒 100% Private & Offline-First
- Zero servers. Zero ads. Zero third-party telemetry.
- All financial data is stored locally in an encrypted Room SQLite database on your device.
- Full JSON **Backup & Restore** engine for seamless manual migrations.

---

## 🛠️ Technology Stack & Architecture

- **Language:** Kotlin 1.9+
- **UI Toolkit:** Jetpack Compose + Material Design 3
- **Local Persistence:** Room Database 2.6.1 (SQLite) with Reactive Kotlin `Flow`
- **Background Schedulers:** Android `AlarmManager`, `BroadcastReceiver`, `NotificationChannel` (API 26+)
- **Report Engine:** Native Android `PdfDocument` & Canvas graphics (A4 format)
- **Architecture:** Clean Architecture + MVVM (Model-View-ViewModel) + Repository Pattern
- **SDK Target:** `minSdk: 26` (Android 8.0 Oreo), `compileSdk: 34` / `targetSdk: 34` (Android 14)

---

## 🚀 How to Run & Deploy via Android Studio (Step-by-Step)

### Prerequisites
1. **Android Studio**: Install **Android Studio Iguana, Jellyfish, Koala, or Ladybug** (Download from [developer.android.com](https://developer.android.com/studio)).
2. **Java Development Kit (JDK)**: **JDK 17** (Embedded in modern Android Studio).
3. **Android SDK**: Android API 34 SDK platform installed via SDK Manager.

---

### Step 1: Open the Project in Android Studio
1. Launch Android Studio.
2. On the Welcome screen, click **Open** (or go to **File > Open...** from the menu).
3. Browse and select the project root folder:
   `D:\EXPENSE TRACKER APP`
4. Click **OK**.

---

### Step 2: Configure Gradle JDK & Sync
1. In Android Studio, go to **File > Settings** (or **Android Studio > Settings** on macOS).
2. In the left panel, navigate to **Build, Execution, Deployment > Build Tools > Gradle**.
3. Under **Gradle JDK**, make sure **Java 17 (embedded JDK)** or your installed JDK 17 is selected.
4. Click **Apply** and **OK**.
5. Click the **Sync Project with Gradle Files** button (the elephant icon in the top right toolbar) or go to **File > Sync Project with Gradle Files**.
6. Wait for the sync to finish with `BUILD SUCCESSFUL`.

---

### Step 3: Run on Physical Device or Emulator

#### Running on a Physical Android Phone:
1. On your phone, go to **Settings > About Phone** and tap **Build Number** 7 times to enable **Developer Options**.
2. Go to **Settings > System > Developer Options** and turn ON **USB Debugging**.
3. Connect your phone to your computer via USB.
4. On your phone, tap **Allow USB Debugging** when prompted.
5. In Android Studio's top toolbar, select your device from the target device dropdown.
6. Click the green **Run 'app'** button (or press `Shift + F10`).
7. The app will install and open on your device!

#### Running on Android Studio Emulator:
1. Go to **Tools > Device Manager**.
2. Click **Create Virtual Device**.
3. Select a phone model (e.g., **Pixel 8** or **Pixel 7**), click **Next**.
4. Choose system image **UpsideDownCake (API 34)** or **Tiramisu (API 33)**, click **Next** and **Finish**.
5. Select your virtual device from the device dropdown and click **Run 'app'**.

---

### Step 4: Build a Standalone APK for Sharing or Testing
If you want an `.apk` file that you can send to your phone or share with others:
1. In Android Studio, go to **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
2. Wait a few seconds for the build to complete.
3. A notification will appear in the bottom right: *"APK(s) generated successfully."*
4. Click **locate** to open the folder containing:
   `app/build/outputs/apk/debug/app-debug.apk`
5. You can copy this APK directly to any Android device and tap it to install!

---

### Step 5: Generate a Signed Release APK / AAB (For Google Play Store)
When you are ready to publish **Easy Expense Tracker** to Google Play:
1. In Android Studio, go to **Build > Generate Signed Bundle / APK...**.
2. Select:
   - **Android App Bundle (.aab)** (Required for Google Play Store upload), OR
   - **APK** (If you want a standalone signed release APK).
3. Click **Next**.
4. In the **Key store path**, click **Create new...** if you don't already have a keystore:
   - Choose a safe path (e.g., `easy_expense_keystore.jks`).
   - Enter a password and alias name.
5. Select **release** build variant.
6. Click **Create / Finish**.
7. Your signed `.aab` or `.apk` will be generated in `app/release/`.

---

## 👨‍💻 Developer & Credits
- **Software Name:** Easy Expense Tracker
- **Lead Developer:** Kunal Turkar
- **License:** Proprietary & Open Personal License
- **Privacy:** 100% Offline-First. No servers, no tracking, zero telemetry.
