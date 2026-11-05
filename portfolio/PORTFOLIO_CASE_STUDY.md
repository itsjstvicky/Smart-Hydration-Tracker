# Smart Hydration Tracker — UX/UI Portfolio Case Study

**Project Title:** Smart Hydration Tracker — Interactive 3D Fluid Hydration Companion  
**Role:** UI/UX Designer & Android Product Designer  
**Platform:** Native Android (`Kotlin`, `Jetpack Compose`, `Material Design 3`, `Room Database`, `Exact AlarmManager`)  
**Visual Style:** Luxury Dark Sapphire Glassmorphism & Bioluminescent Fluid Physics

---

## 1. Executive Summary & Overview

**Smart Hydration Tracker** is a native Android hydration companion crafted to turn daily water intake from a forgettable chore into an engaging, tactile habit. Instead of relying on flat progress bars and passive notifications that get lost in the notification shade, the app centers around a custom-drawn **3D Animated Fluid Water Bottle Canvas** and a **Google-Clock Inspired Full-Screen Lock-Screen Wake-Up Alert**.

---

## 2. Problem Statement & UX Challenge

### Why Most Hydration Trackers Fail Within Week 1
1. **Passive Notification Fatigue:** Standard status-bar notifications blend in with dozens of daily messaging and work alerts. Users swipe them away or ignore them without drinking water.
2. **Sterile Data Entry:** Utilitarian trackers feel like filling out a spreadsheet, offering zero emotional or visual reward when logging a glass of water.
3. **Static Interval Timers:** Fixed schedules fail to adapt dynamically when a user logs water ahead of time, leading to annoying redundant alerts.

### The UX & Product Solution
1. **Tactile 3D Fluid Physics (`WaterBottleCanvas`):** A centerpiece glass bottle rendered in Jetpack Compose Canvas with dual-phase sine waves, rising translucent air bubbles, specular glass edge highlights, and dynamic liter tick marks (`0L` to `2.0L MAX`) that respond immediately to every logged drink.
2. **Full-Screen Wake-Up Alarm Experience (`ReminderAlertActivity`):** When a hydration timer fires, the app wakes the screen with a dedicated lock-screen overlay, looping custom alarm audio, and pulsing waveform vibration—offering **1-tap `+250ml` logging** or a **10-minute snooze** right from the lock screen.
3. **Smart Dynamic Recalculation:** Logging water from either the Home screen or the Lock-Screen Alert automatically recalculates and schedules the next exact reminder based on the user's chosen interval (`10m` to `4h`).

---

## 3. Visual Design System

### Color Palette & Glassmorphism Tokens

| Token Name | Hex Code | Usage in Interface |
| :--- | :--- | :--- |
| **Navy Dark** | `#080C16` | Primary deep nocturnal background canvas |
| **Navy Deep** | `#0C1322` | Elevated card containers & chart backdrop |
| **Navy Light** | `#162035` | Active reminder card surface & bottle glass tint |
| **Navy Glow** | `#1E2D4A` | Ambient radial backdrop highlights |
| **Sapphire Blue** | `#4A8CFF` | Primary brand accent, active switches, bars & CTA buttons |
| **Azure Blue** | `#2E6BFF` | Secondary fluid wave layer & vertical bar gradient |
| **Light Blue Glow** | `#82B1FF` | Specular bottle highlights, chart cap dots & badges |
| **Mint Cyan** | `#26E6E6` | Bioluminescent liquid crest & highlight accents |
| **Glass Card Bg** | `#B315223D` | Translucent frosted card fill (`70%` opacity) |
| **Glass Card Border** | `#3D4A8CFF` | `1dp` specular glass border (`24%` Sapphire opacity) |
| **Glass Text Secondary** | `#99B3C8E8` | Muted silver-blue hierarchy for timestamps & captions |

### Typography Hierarchy (`Roboto / System Geometric Sans`)

- **Alarm Hero Display:** `38sp`, `Black` (`-1.2sp` letter-spacing) — *"Drink Water 💧"*
- **Clock Readout Display:** `32sp`, `ExtraBold` (`0.5sp` letter-spacing) — *"03:30 PM"*
- **Screen Header Title:** `26sp`, `ExtraBold` (`-0.5sp` letter-spacing) — *"Hey, Vignesh" / "Hydration Stats" / "Preferences"*
- **Card Section Header:** `17sp – 18sp`, `Bold` — *"6 / 8 Glasses Completed" / "5 Day Streak! 🔥"*
- **Body & Interactive Controls:** `14sp`, `Bold` / `Medium` — Settings rows, preset chips, and log entries
- **Micro Captions & Chart Axes:** `11sp – 12sp`, `Medium` — Target lines, timestamps, and helper subtitles

---

## 4. Screen-by-Screen Breakdown (All 4 Core Screens)

### 01. Home Dashboard (`HomeScreen.kt`)
- **Personalized Greeting & Quick Mute:** Greets the user dynamically by name with the current date and a 1-tap circular glass bell button to toggle reminders on/off.
- **3D Glass Water Bottle Canvas:** Displays live daily hydration progress (`75% Completed`) with animated dual sine waves, rising bubbles, and illuminated liter markings.
- **Glassmorphic Progress Card:** Summarizes completed glasses (`6 / 8 Glasses Completed`), total volume (`1.5L / 2.0L`), and a circular progress indicator.
- **Google-Clock Style Next Reminder Card:** Displays the exact time of the next scheduled alert (`03:30 PM`), interval (`Every 1 Hour`), and an instant toggle switch.
- **Glowing Primary Action FAB:** Circular `86dp` Sapphire button with neon drop-shadow to log `+250ml` in one tap, paired with a quick `"Subtract Last"` undo action.

### 02. Hydration Statistics (`StatisticsScreen.kt`)
- **Streak & Weekly Average Hero Card:** Highlights consecutive goal-met days (`5 Day Streak! 🔥`), goal completion ratio (`Met goal: 6 of past 7 days`), and `Weekly Avg` in liters.
- **7-Day Habits Custom Canvas Bar Chart:** Plots the past 7 days of water intake against a horizontal dashed target line (`2.0L`). Goal-met days illuminate with a `SapphireBlue -> AzureBlue` vertical gradient and glowing top cap dots.
- **Chronological Today's Intake Log:** Lists every glass consumed today with exact timestamps (`02:15 PM`), milliliter badges (`250 ml`), individual delete buttons, and a `"Clear All"` action.

### 03. Preferences & Smart Calibration (`SettingsScreen.kt`)
- **User Profile Card:** Editable display name (`Vignesh`) for personalized dashboard greetings.
- **Daily Hydration Goals Card:**
  - **Intake Goal Slider:** Smooth stepped slider from `1.0L` to `4.0L`.
  - **Water Glass Size Presets:** Quick-select vessel chips (`200ml`, `250ml`, `330ml`, `500ml`).
- **Alarm & Schedule Card:**
  - Independent switches for **Intake Reminders**, **Vibration Alert**, and **Status Notifications**.
  - Automatic **Next Reminder Target** readout.
  - **Reminder Interval Chips:** `10m`, `30m`, `1h`, `2h`, `3h`, `4h`.
  - **Custom Reminder Sound Picker:** Supports system ringtones or custom local audio/music files (`MP3/WAV/OGG`).
- **Lock Screen Compatibility Card:** Real-time permission verification badges (`Allowed` / `Requires Action`) for Android 14+ Full-Screen Intents, Display Over Other Apps, and OEM lock-screen settings.

### 04. Full-Screen Lock-Screen Reminder Alert (`ReminderAlertActivity.kt`)
- **Immersive Wake-Up Header:** High-contrast `"Time to Drink"` / `"Drink Water 💧"` alert header that wakes the screen over the lock screen.
- **Live 3D Bottle Context:** Shows the interactive 3D water bottle and `"Today's Progress"` card (`6 / 8 Glasses`, `1.5L / 2.0L`, `75%`) so users immediately see how close they are to their daily goal.
- **Multi-Sensory Alerting:** Loops the user's chosen alarm tone via `MediaPlayer` (`USAGE_ALARM`) and triggers a repeating waveform vibration pattern (`420ms` bursts) when vibration is enabled.
- **Frictionless Bottom Action Bar:**
  - **`✓ I Drank Water` (Primary CTA):** Logs `+250ml` to the Room database, schedules the next alarm interval, stops audio/vibration, and dismisses the screen.
  - **`⏰ Snooze 10 Minutes` (Secondary CTA):** Reschedules the exact alarm for `+10 minutes` and updates the dashboard readout.
  - **`Dismiss`:** Silences and closes the alert without logging.

---

## 5. Technical Architecture Summary

- **UI Framework:** 100% Jetpack Compose with Material Design 3, custom `Canvas` drawing (`WaterBottleCanvas`, 7-day bar chart), and `enableEdgeToEdge()` safe window insets.
- **Architecture Pattern:** MVVM (`HydrationViewModel` + `HydrationRepository`) exposing reactive `StateFlow` streams collected lifecycle-safely in Compose.
- **Local Persistence:** Room SQLite Database (`HydrationDatabase` v4) storing `HydrationLog` records and `UserSettings` preferences.
- **Background & Lock-Screen Reliability:** `AlarmManager.setExactAndAllowWhileIdle()`, `WaterReminderReceiver` with partial `WakeLock`, full-screen notification intent fallback, and `BootReceiver` for reboot persistence.
