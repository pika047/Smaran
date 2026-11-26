# Smaran

A simple, elderly-friendly Android app for notes and reminders. Add a note by typing or speaking, choose when to be reminded, and see today's tasks on the home screen.

Status: early development. Android 8.0+ only, English first, built to add more languages later.

## Modules

| Module | What it holds |
| --- | --- |
| `app` | App start, navigation, dependency wiring |
| `core:model` | Plain data classes (pure Kotlin) |
| `core:domain` | Use cases and repository interfaces (pure Kotlin, no Android) |
| `core:data` | Encrypted Room database (SQLCipher) and repository implementations |
| `core:security` | Android Keystore key handling |
| `core:ui` | Large-text, high-contrast theme and shared components |
| `feature:today` | Today's notes with a large Done button |
| `feature:notes` | Adding a note |

Dependencies point inward: features depend on `core:domain` interfaces, and `core:data` implements them. Shared Gradle setup lives in `build-logic/` as convention plugins, so a new feature module needs only:

```kotlin
plugins { alias(libs.plugins.smaran.android.feature) }
android { namespace = "com.smaran.feature.yourfeature" }
```

## Security

- The database is encrypted with SQLCipher using a random 256-bit key, which is itself encrypted by an Android Keystore key.
- No internet permission. CI fails if one appears in the merged manifest.
- Backups and device transfer are turned off, so notes are never copied off the phone.
- Release builds are shrunk and obfuscated with R8 and have debug logging removed.

## Adding a language

All on-screen text is in `res/values/strings.xml` in each module. To add Hindi, for example, add `res/values-hi/strings.xml` next to each one. Dates and times are already formatted for the phone's language.

## Building

Requires JDK 17 and the Android SDK (Android Studio sets both up).

```bash
./gradlew testDebugUnitTest :core:domain:test   # unit tests
./gradlew :app:assembleDebug                    # debug APK
```
