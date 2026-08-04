# Seek and You Will Find

A Kotlin + XML Android app that keeps **one** favorite Bible verse at hand —
chosen from a fully **offline**, bundled King James Version dataset — with a
daily wake-up reminder, a home screen widget, and a biblically-themed
parchment / gold / burgundy UI.

No internet access, no accounts, no ads. The entire Bible is stored on your
device and every lookup is instant.

---

## Screens

### Home screen (`ui/home/HomeFragment`)
- **Empty state**: shown when no verse has been saved yet — a scroll icon, a
  headline, a short subtitle, and a **"Get a Verse"** button.
- **Verse detail card**: once a verse is saved, the empty state is replaced by
  a parchment-style card showing:
  - the verse text in large display type, framed by quotation marks
  - an ornamental divider
  - the full reference (e.g. `John 3:16`)
  - Book, Testament (Old / New), Chapter, and Verse number
  - Translation name + ID (e.g. `King James Version (KJV)`)
  - the date/time the verse was saved
  - a **"Change Verse"** button that re-opens the verse picker
- **Alarm card**: always visible at the bottom:
  - an alarm icon plus the current status text ("Alarm off" / "Alarm set for 7:00 AM")
  - **"Set Alarm"** when off → opens the alarm bottom sheet
  - **"Change Alarm"** (outlined) when on → re-opens the alarm bottom sheet

### Verse picker — bottom sheet (`ui/filter/VerseFilterBottomSheet`)
- **Testament** spinner: Any / Old Testament / New Testament.
- **Book** spinner: populated from the chosen testament (all 66 books when
  "Any").
- **Chapter** spinner: populated from the chosen book; disabled until a book
  is selected.
- **Verse** spinner: populated from the chosen chapter; disabled until a
  chapter is selected.
- Every field defaults to **Any**, and anything left unspecified is chosen at
  random within the narrowed-down selection.
- **"Get Verse"** resolves the verse offline and shows a **preview** state
  with the verse text, reference, and translation.
- From the preview you can **"Try Another"** (re-roll within the same filters)
  or **"Save as Favorite"** — saving overwrites the single previous favorite.
- Errors (e.g. an out-of-range reference) show an inline error message.

### Alarm — bottom sheet (`ui/alarm/AlarmTimeBottomSheet`)
- Material `TimePicker` (12-hour clock) pre-filled with the current alarm
  time (defaults to 7:00 AM).
- **"Save Alarm"** schedules the daily reminder and persists the time.
- **"Remove Alarm"** (shown only when an alarm is active) turns it off.
- **"Cancel"** closes the sheet without changes.
- Opening the alarm sheet on Android 13+ requests the
  `POST_NOTIFICATIONS` runtime permission if it hasn't been granted yet.

### Daily notification (`notification/`)
- Fires at the scheduled time every day.
- Custom parchment-themed `RemoteViews` in both **collapsed** and **expanded**
  sizes (collapsed = verse + reference; expanded adds the translation name).
- Set to `VISIBILITY_PUBLIC`, so the full verse also appears on the **lock
  screen**.
- Tapping the notification opens the app.

### Home screen widget (`widget/BibleVerseWidgetProvider`)
- Shows the current favorite verse and its reference.
- Tapping the widget opens the app.
- Automatically refreshes whenever a new favorite is saved.

---

## Features

- **Fully offline Bible** — the entire King James Version ships inside the app
  as `assets/bible_kjv.json` (~4.5 MB, public domain). Parsed once and cached
  in memory; there is zero network usage and no `INTERNET` permission.
- **Random verse by filter** — narrow by Testament → Book → Chapter → Verse;
  unset fields are chosen randomly. Chapter/verse options are range-checked so
  you can never pick an invalid reference.
- **One favorite at a time** — saving a new verse replaces the previous one
  (single-row Room table), live-updating the UI, notification, and widget.
- **One daily alarm** — exact (`setExactAndAllowWhileIdle`), repeats every day,
  survives reboots, and can be changed or removed at any time.
- **Rich notification** — custom collapsed/expanded layouts, public lock-screen
  visibility, reminder category.
- **Home screen widget** — current verse at a glance, always in sync.
- **Parchment/gold/burgundy theme** — a consistent biblical aesthetic across
  the app, bottom sheets, notification, and widget.

---

## Tech stack

| Concern | Technology |
| --- | --- |
| Language | Kotlin 1.9.24 |
| UI | XML layouts + ViewBinding (no Compose) |
| Architecture | MVVM + Repository |
| DI | Hilt 2.51.1 |
| Local DB | Room 2.6.1 (single favorite verse) |
| Preferences | DataStore 1.1.1 (alarm time) |
| JSON parsing | Gson 2.11.0 (bundled offline asset) |
| Async | Coroutines + Flow |
| Background | WorkManager 2.9.1, AlarmManager |
| Build | Gradle 8.7, AGP 8.5.2 |

Min SDK **26** (Android 8.0) · Target / Compile SDK **34**.

---

## Architecture

```
MainActivity
 └─ HomeFragment ──────────────── HomeViewModel (Hilt)
     ├─ VerseFilterBottomSheet ────► VerseRepository ─► OfflineBibleSource (KJV JSON asset)
     └─ AlarmTimeBottomSheet ───────►                ├► FavoriteVerseDao (Room, single row)
                                   │                 └► AlarmPreferences (DataStore)
                                   │
AlarmManager ─► AlarmReceiver ─► WorkManager ─► VerseNotificationWorker ─► notification
     ▲
     └── BootReceiver (BOOT_COMPLETED / MY_PACKAGE_REPLACED re-arms)

BibleVerseWidgetProvider ─► reads favorite via repository, pushes RemoteViews
```

Layers:

- **`data/`** — repository, Room database/DAO/entity, DataStore alarm
  preferences, `OfflineBibleSource` (loads + caches the KJV asset), and the
  in-code catalogue of all 66 books (`BibleBooksProvider`).
- **`ui/`** — `MainActivity`, `HomeFragment` + `HomeViewModel`, the verse
  filter bottom sheet, and the alarm time bottom sheet.
- **`alarm/`** — `AlarmScheduler` (exact daily scheduling),
  `AlarmReceiver` (posts the notification + self-reschedules), `BootReceiver`
  (re-arms after reboot / app update).
- **`notification/`** — `VerseNotificationHelper` (custom RemoteViews) and
  `VerseNotificationWorker` (WorkManager job).
- **`widget/`** — `BibleVerseWidgetProvider` (home screen widget).
- **`di/`** — Hilt modules; an entry point lets non-Hilt classes (widget,
  worker) reach the repository.

---

## Project structure

```
app/src/main/
├── assets/bible_kjv.json          # Offline KJV dataset (all 66 books)
├── java/com/christopher/bibleverse/
│   ├── BibleVerseApplication.kt   # Hilt app + notification channel
│   ├── alarm/                     # Scheduler + broadcast receivers
│   ├── data/
│   │   ├── local/                 # Room, DataStore, offline source
│   │   ├── model/                 # VerseDetail, BibleBook, Testament, catalogue
│   │   └── repository/
│   ├── di/                        # Hilt modules + entry point
│   ├── notification/              # Notification helper + worker
│   ├── ui/
│   │   ├── alarm/                 # Alarm time bottom sheet
│   │   ├── filter/                # Verse filter bottom sheet
│   │   ├── home/                  # Home fragment + view model
│   │   └── main/                  # MainActivity
│   ├── util/                      # DateTimeUtils, Resource
│   └── widget/                    # Home screen widget provider
└── res/
    ├── drawable/                  # Vectors + parchment/gold themed backgrounds
    ├── layout/                    # fragment_home, bottom sheets, notification & widget layouts
    └── values/                    # Colors, themes, strings, dimens
```

---

## Permissions

- `SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM` — the daily reminder.
- `POST_NOTIFICATIONS` — requested at runtime on Android 13+ when you first
  set an alarm.
- `RECEIVE_BOOT_COMPLETED` — re-arms the alarm after a device restart.
- `WAKE_LOCK` — used by WorkManager to reliably post the notification.

No `INTERNET` permission — the app never makes a network request.

---

## Build & run

1. Open Android Studio (Koala / 2024.1+ recommended).
2. **File → Open** → select the `BibleVerseApp` folder.
3. Let Gradle sync (the 8.7 wrapper downloads automatically).
4. Run on a device/emulator with **API 26 (Android 8.0)** or higher.

Or from the command line:

```bash
./gradlew assembleDebug
```

---

## Swapping the bundled translation

The app reads a JSON file shaped like `[{ "abbrev": "gn", "chapters": [[...]], ... }, ...]`.
To ship a different public-domain translation:

1. Drop the new JSON into `app/src/main/assets/`.
2. Update `ASSET_FILE_NAME` and `TRANSLATION_NAME` (and `TRANSLATION_ID`) in
   `data/local/OfflineBibleSource.kt`.
3. Make sure its `abbrev` keys match `BibleBooksProvider`'s `abbrev` values
   (or update those values to match the new file).

---

## License

The bundled scripture text is the **King James Version**, public domain.
All application code and design are original to this project.
