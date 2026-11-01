# Brave Music Companion (Android)

A small **unofficial** Android launcher for YouTube Music in Brave.

## What it does

- On opening the APK, checks for Brave (`com.brave.browser`).
- If missing, prompts the user to install Brave from Google Play.
- If installed, automatically launches `https://music.youtube.com/` using a Brave-targeted **Custom Tab**.
- Offers a backup button to launch YouTube Music in **regular Brave**.
- Does not use Android WebView; does not scrape, download, bypass restrictions, intercept logins, or use YouTube APIs.

## What it cannot guarantee

- **No guaranteed background playback in Custom Tabs.** The fact that Brave's normal tabs continue playing on your phone doesn't prove Custom Tabs will. Test Home and screen lock.
- **No complete fullscreen toolbar-free YouTube Music window.** A Trusted Web Activity requires ownership verification of the served website. You cannot verify `music.youtube.com` with Digital Asset Links.
- The page is YouTube Music, not a custom app interface. Its UI is outside your control.
- Google/Brave may change background playback behavior.

## Build without Android Studio (GitHub Actions)

1. Create a new GitHub repository (recommended name: `brave-music-companion`).
2. Extract the ZIP and upload **its contents**, including hidden `.github/workflows/build-apk.yml`, to the repository root. Do not upload the ZIP as a single file.
3. Commit the changes to `main`.
4. Visit **Actions** > **Build Brave Music Companion APK**. A push-triggered build may start automatically; otherwise use **Run workflow**.
5. Open the successful run > **Artifacts** > `Brave-Music-Companion-APK`.
6. Extract the artifact ZIP and install `app-debug.apk` on your phone.
7. Install Brave if prompted; start playback and test Home and lock screen.

Uses GitHub-hosted Android SDK, Java 17, Gradle 8.9, Android Gradle Plugin 8.7.3, androidx.browser 1.8.0.

## Test cases

| Test | Expected / record result |
| --- | --- |
| Brave not installed | APK prompts to install Brave, no Chrome fallback |
| Brave installed | Compact view opens YouTube Music in Brave |
| Login | Browser should use Brave account/session, verify on device |
| Home button with music playing | Record whether playback persists |
| Screen lock | Record whether playback persists |
| Compact background audio stops | Try **Open in regular Brave** and compare |

## Notes

Only Brave Stable (`com.brave.browser`) is checked. Brave Beta and Nightly use different packages. The debug APK is for personal testing, not a Play Store release. There is no included Gradle wrapper executable; the GitHub workflow installs Gradle itself.
