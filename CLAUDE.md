# CLAUDE.md

This file gives an AI coding assistant (or a new contributor) the context needed to work in this repository. For full detail, see `docs/ARCHITECTURE.md` and `docs/CODING_CONVENTIONS.md`.

## Project Overview

WhatSayYou is an Android app for recording audio and transcribing it to text for later review. Recordings and their transcripts live entirely on-device — no auth, no backend, no sync.

- Language: Kotlin
- Build: Gradle with Kotlin DSL (`build.gradle.kts`)
- UI: Jetpack Compose + Navigation Compose
- Package: `com.jimmcgaw.whatsayyou`
- Min SDK: 26 (compile/target SDK: latest stable)

## Core Concept

Three primary destinations, navigated via Navigation Compose:

1. **Home** — a single button to start/stop recording, plus an elapsed-time display and a clear recording-in-progress indicator while active.
2. **List** — past recordings sorted by most recently recorded-or-played (`lastAccessedAt` descending). Tapping a row opens View.
3. **Settings** — placeholder tab for future preferences (e.g. transcription language). Empty for now.

**View** (reached by tapping a recording in List; not a tab itself): playback via ExoPlayer (start/stop + seek bar), the transcript displayed below the controls, in-place rename, and delete (with confirmation).

## Key Architectural Decisions

- **Capture and transcription are decoupled.** `AudioCaptureEngine` (built on `AudioRecord`) always records 16kHz mono 16-bit PCM straight to a WAV file on internal storage. `TranscriptionEngine` is a swappable interface that operates on that finished file. It starts as a wrapper around Android's `SpeechRecognizer`, and is designed so whisper.cpp, sherpa-onnx, or `AudioRecord`-based test harnesses can be swapped in later without touching capture code.
- **Transcription is post-processing only.** No live/streaming transcription in v1; it runs after a recording completes, via a `CoroutineWorker` (WorkManager), so the job survives app backgrounding or process death.
- **Repository layer is the only thing that touches Room or the filesystem.** ViewModels talk to the Repository, never to `AudioRecordDao` or `File` APIs directly.
- **Manual dependency injection**, not Hilt — a deliberate choice for a learning project, so the object graph (constructor injection + a small factory/container) stays visible rather than generated.
- **Coroutines throughout**: `viewModelScope` for UI-scoped work, `Dispatchers.IO` for blocking I/O, one-time UI events (errors, retry prompts) exposed via `Channel`/`SharedFlow` rather than `StateFlow`, to avoid re-firing on recomposition or rotation.

## Explicitly Out of Scope (for now)

- Authentication
- Background/foreground-service handling for recording, and audio-focus interruptions (phone calls, other apps)
- Word-level synced transcript scrubbing (would require per-word timestamps from the transcription engine)
- Settings screen content
- Storage quota management / orphaned audio file cleanup sweep

## Commands

Once the project is scaffolded, standard Gradle wrapper commands apply:

- Build: `./gradlew build`
- Unit tests: `./gradlew test`
- Instrumented tests: `./gradlew connectedAndroidTest`
- Install debug build: `./gradlew installDebug`

## Where to Look

- `docs/ARCHITECTURE.md` — data model, component responsibilities, data flow, error-handling patterns.
- `docs/CODING_CONVENTIONS.md` — Kotlin style and structural conventions used throughout the codebase.
