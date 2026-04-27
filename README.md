# Deni

Deni is a mobile-first lending app built with Android + Supabase.  
It focuses on a fast phone-auth onboarding flow, clear credit visibility, and a complete loan lifecycle (apply, top up, repay, and track history).

## Core Product Features

- **Phone-only authentication**
  - Sign in and sign up with phone number + PIN
  - Biometric sign-in support (device-dependent)
  - Google/third-party OAuth removed from active auth flow
- **Guided onboarding**
  - Get Started, Sign In/Up, and Complete Profile flows with modern Compose UI
  - KYC document upload and next-of-kin capture
  - Contacts/SMS permission-driven risk signal collection during profile completion
- **Loan lifecycle**
  - Apply for loans with policy and eligibility checks
  - Top up active loans when headroom allows
  - Repay and generate payment receipts
  - Loan history, transaction feed, and repayment-related views
- **Account and experience**
  - Profile and settings surfaces
  - Home and insights dashboards
  - Sync/background processing via WorkManager

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose + Material 3 + Navigation Compose
- **Architecture:** MVVM + Repository pattern
- **State & async:** StateFlow + Coroutines
- **Dependency injection:** Hilt
- **Local data/security:** Room (SQLCipher-enabled setup in project), DataStore, encrypted preferences utilities
- **Backend:** Supabase (Postgres, Edge Functions, Auth, Realtime)
- **Background jobs:** WorkManager
- **Build tooling:** Gradle Kotlin DSL, KSP

## Backend and Data Model

### Supabase

The project uses Supabase as the primary backend for data and server-side business logic:

- `supabase/functions` contains Edge Functions used by app flows
- `supabase/migrations` contains schema and policy migrations
- Core write operations are designed to run through backend logic rather than unrestricted client writes

### Phone-only auth enforcement

Current project state enforces phone-only strategy across layers:

- App UI/routes expose phone auth flow
- Google auth endpoint is disabled in functions
- DB migration enforces phone as the allowed provider on `users.auth_provider`

## Repository Structure

- `app/src/main/java/com/loki/deni/ui/` - Compose screens, components, navigation, viewmodels
- `app/src/main/java/com/loki/deni/data/` - local/remote data, entities, repository implementations
- `app/src/main/java/com/loki/deni/domain/` - domain rules and interfaces
- `app/src/main/java/com/loki/deni/di/` - dependency injection modules
- `app/src/main/java/com/loki/deni/sync/` - background sync workers/services
- `app/src/main/res/` - Android resources
- `supabase/functions/` - Edge Functions
- `supabase/migrations/` - SQL migrations
- `docs/` - project docs and notes

## Getting Started

### Prerequisites

- Android Studio (latest stable recommended)
- JDK 21
- Android SDK configured for the project
- Supabase CLI (optional, needed for local backend/migration/function workflows)

### Run the app

```bash
./gradlew installDebug
```

### Useful checks

```bash
./gradlew :app:compileDebugKotlin
./gradlew :app:assembleDebug
```

## Configuration

Set environment-specific values in `local.properties` (example):

```properties
ADMOB_APP_ID=ca-app-pub-xxxxxxxxxxxxxxxx~yyyyyyyyyy
```

You may also need Firebase/Supabase project credentials depending on your environment and enabled modules.

## Notes

- This repository currently includes substantial ongoing modernization/refactor work.
- Prefer validating critical user journeys on-device/emulator after pulls:
  - auth and onboarding
  - profile completion + permissions
  - loan apply/top up/repay paths

## License

MIT License  
Copyright (c) Dominic Bor
