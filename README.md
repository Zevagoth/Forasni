# Forasni (فرصني)

[![Download Forasni APK](https://img.shields.io/badge/Download_APK-Forasni_v1.0.0_Preview-1B5E20?style=for-the-badge&logo=android&logoColor=white)](../../releases/download/v1.0.0-preview.1/Forasni.apk)

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

## Private Android preview

Repository collaborators can [download the signed Forasni APK](../../releases/download/v1.0.0-preview.1/Forasni.apk) from the [preview release](../../releases/tag/v1.0.0-preview.1). The repository is private, so a GitHub account with access is required.

This preview runs in guest mode with local sample data. Google Sign-In and Firebase cloud synchronization require a configured Firebase project and are not available in the current APK.

The GitHub Actions workflow builds a signed release APK automatically (using repository secrets if configured, or an automatically generated signing key). Development builds require Android SDK, JDK 21, and the signing configuration referenced in `app/build.gradle.kts`.
