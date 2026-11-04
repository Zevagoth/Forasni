# Forasni (فرصني)

**An Arabic-first Android marketplace connecting people in Jordan with local tradespeople and service providers.**

Forasni helps customers find providers by service and location, explore profiles and work samples, contact providers by phone or WhatsApp, and post service requests. The app also includes interfaces for quotes, bookings, and provider profiles.

## Highlights

- Browse local providers across home services and other skilled categories.
- Search using Arabic terms, including common Jordanian expressions.
- View provider profiles and media showcases before making contact.
- Call or message providers directly through WhatsApp.
- Post job requests and explore available opportunities.
- Use a guest preview or sign in with Google when Firebase is configured.

## Technology

Forasni is built with **Kotlin** and **Jetpack Compose** for Android. It uses **Firebase Authentication** and **Cloud Firestore** for account and marketplace data.

## Development status

This project is under active development. There is currently **no downloadable APK release**. The GitHub Actions build requires a Firebase configuration that generates `default_web_client_id` and a signing keystore before it can produce an installable APK.

To build locally, configure the Android SDK and JDK 21, provide the app's Firebase `google-services.json` with Google Sign-In enabled, and set up the signing configuration referenced in `app/build.gradle.kts`. Then run `gradle assembleDebug`.

Release downloads will appear on the [Releases page](../../releases) once a build has passed and an APK has been published.
