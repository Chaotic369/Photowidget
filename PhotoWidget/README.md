# Photo Widget

Native Android (Kotlin) home-screen photo widget.

- Open the app → **Add photos** (multi-select) → **Add widget to home screen**
- Tap the widget to show the next photo; it also auto-advances about every 30 min
- Tap the ⚙ icon on the widget to open the app

## Build the APK with GitHub Actions
1. Create a new GitHub repo and push all of these files (keep the `.github` folder).
2. Open the **Actions** tab → **Build APK** (runs on every push, or press *Run workflow*).
3. When it finishes, open the run → **Artifacts** → download `PhotoWidget-debug` → unzip → install `app-debug.apk`.
4. Optional: push a tag like `v1.0` to get the APK attached to a GitHub Release.

No Gradle wrapper is needed; the workflow installs Gradle 8.7 itself.
