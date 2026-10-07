# SubScript 💳

> **Privacy-first, local-only subscription and recurring expense tracker for Android.**  
> Fully open source (FOSS), zero cloud dependencies, zero analytics, and zero internet permission.

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-35%20(Android%2015)-green.svg)](https://developer.android.com)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-26%20(Android%208.0)-lightgrey.svg)](https://developer.android.com)
[![F-Droid Compliant](https://img.shields.io/badge/F--Droid-Ready-brightgreen.svg)](https://f-droid.org)
[![Zero Internet](https://img.shields.io/badge/Internet%20Permission-None-red.svg)](#privacy--f-droid-compliance)

---

## 🔒 Privacy & F-Droid Compliance Guarantees

1. **Zero Internet Permission**: The app **does not** request `android.permission.INTERNET` in `AndroidManifest.xml`. It is physically impossible for the application to transmit data over the network.
2. **Zero Proprietary Dependencies**: No Google Play Services, Firebase, AdMob, Crashlytics, telemetry, or third-party proprietary binary blobs.
3. **Local-First Architecture**: All database transactions are stored on-device using Android Jetpack Room with SQLite.
4. **Local Native Notifications**: Alerts are scheduled purely through the Android system's native `AlarmManager` and `NotificationManager`. No Firebase Cloud Messaging (FCM) or remote push daemons.
5. **Boot Recovery**: A native `BootReceiver` automatically restores all scheduled payment reminder alarms when the device reboots or the app updates.

---

## ✨ Features

- **Dashboard with Recurring Expense Analytics**: Automatically calculates and aggregates your total monthly recurring cost across all active subscriptions, normalizing weekly, monthly, and yearly cycles.
- **Scannable Due Date Timeline**: Subscriptions are chronologically sorted by upcoming due dates with intuitive visual indicators (`Due tomorrow`, `Due in 3 days`, `Overdue`).
- **Validated Add / Edit Form**: 
  - Subscription Name validation (non-empty, length checks).
  - Cost input with real-time numeric validation.
  - Multi-currency support (`$`, `€`, `£`, `¥`, `₹`, `CHF`, `C$`, `A$`).
  - Billing cycle selection (`Weekly`, `Monthly`, `Yearly`).
  - Interactive Material 3 calendar date picker.
  - Per-subscription reminder alarm toggle.
- **24-Hour Advance Alerts**: Local broadcast receiver issues a reminder 24 hours prior to payment due dates and automatically advances the cycle for ongoing recurrence.
- **Material 3 UI**: Clean, responsive, and adaptive edge-to-edge UI supporting system dynamic colors, light mode, and dark mode.

---

## 🏗️ Architecture

```
com.dacraezy1.subscript/
├── SubScriptApp.kt                   # Application singleton (channels & dependency container)
├── MainActivity.kt                   # Edge-to-edge Activity entry point
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt            # Room database instance
│   │   └── SubscriptionDao.kt        # Local SQL operations & Flow queries
│   ├── model/
│   │   ├── BillingCycle.kt           # Weekly / Monthly / Yearly calculations
│   │   └── Subscription.kt           # Room entity
│   └── repository/
│       └── SubscriptionRepository.kt # Coordination between DB & AlarmScheduler
├── notification/
│   ├── SubscriptionAlarmScheduler.kt # Native AlarmManager calculator & dispatcher
│   ├── SubscriptionBroadcastReceiver.kt # Notification poster & cycle advancer
│   └── BootReceiver.kt               # Alarms restorer on system reboot
└── ui/
    ├── components/
    │   ├── AddEditSubscriptionDialog.kt # Form with date picker & validation
    │   ├── DashboardSummaryCard.kt      # Monthly cost overview card
    │   ├── NotificationPermissionBanner.kt # Android 13+ permission helper
    │   └── SubscriptionCard.kt          # Individual subscription card
    ├── screens/
    │   └── DashboardScreen.kt        # Primary dashboard scaffold & list
    ├── theme/
    │   ├── Color.kt                  # Material 3 colors
    │   ├── Theme.kt                  # Light/Dark dynamic theme
    │   └── Type.kt                   # Typography
    ├── util/
    │   └── DateUtils.kt              # Date formatting & due countdowns
    └── viewmodel/
        └── SubscriptionViewModel.kt  # StateFlow & form validation logic
```

---

## 🛠️ Building the Project

### Prerequisites
- JDK 17 (Eclipse Temurin recommended)
- Android SDK with API 34+ installed

### Build Unsigned Release APK
```bash
chmod +x gradlew
./gradlew assembleRelease
```
The output APK will be located at:
```
app/build/outputs/apk/release/app-release-unsigned.apk
```

### Run Tests
```bash
./gradlew test
```

---

## 🚀 Continuous Integration (GitHub Actions)

A GitHub Actions workflow is pre-configured at `.github/workflows/build-apk.yml`.  
Whenever a Git tag matching `v*` is pushed (e.g. `git tag v1.0.0 && git push origin v1.0.0`), the workflow automatically:
1. Checks out the code in a clean Ubuntu runner.
2. Compiles an unsigned release APK using JDK 17 and Gradle.
3. Renames the artifact to `SubScript-vX.Y.Z-unsigned.apk`.
4. Creates a GitHub Release and attaches the compiled APK.

---

## 📄 License

This program is free software: you can redistribute it and/or modify it under the terms of the **GNU General Public License v3.0** as published by the Free Software Foundation.

See the [LICENSE](LICENSE) file for the full license text.
