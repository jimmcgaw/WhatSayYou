# Architecture — WhatSayYou

## Overview

WhatSayYou records audio, saves it to a local file, and transcribes it to text after the fact. Everything — audio files, transcripts, metadata — is stored on-device only. No authentication, no network dependency for the core flow (the initial `SpeechRecognizer`-based engine may use network recognition depending on device, but nothing else in the app requires it).

## Tech Stack

- Kotlin, Gradle Kotlin DSL
- Jetpack Compose + Navigation Compose (chosen over the classic Fragment/XML "Navigation UI Activity" template)
- Room (local database)
- WorkManager (durable background transcription jobs)
- ExoPlayer / Media3 (audio playback)
- `AudioRecord` (audio capture)
- Manual dependency injection (no Hilt)
- Package: `com.jimmcgaw.whatsayyou`; minSdk 26, compileSdk/targetSdk latest stable

## Screens & Navigation

Three tab destinations via Navigation Compose, plus one detail screen reached by navigation, not a tab:

### Home
Single button to start/stop recording. While recording: an elapsed-time display and a clear recording-in-progress indicator (in addition to the button's own state) — both driven by `MutableStateFlow` in the screen's ViewModel, updated from the coroutine running the capture loop.

### List
Shows all recordings, sorted by `lastAccessedAt` descending (most recently recorded-or-played first). Backed by a `Flow<List<AudioRecordEntity>>` from the Repository, so the list updates automatically when a transcription completes or a recording is deleted — no manual refresh needed. Tapping a row navigates to View.

### View (detail screen)
Reached from List. Shows:
- Playback controls (start/stop) and a seek bar, both driven by ExoPlayer's position callbacks.
- The transcript, displayed as static text below the controls. Not synced to playback position — no per-word timestamps required, which keeps this screen decoupled from which `TranscriptionEngine` produced the text.
- In-place rename: editing the title updates it immediately without interrupting playback. Empty-name input shows inline validation text under the field rather than a Snackbar.
- Delete: `AlertDialog` confirmation, then remove the DB row, then best-effort delete the audio file (see **Deletion** below), then navigate back to List (which reflects the deletion automatically via its `Flow` query).

Opening this screen (starting playback) updates the recording's `lastAccessedAt` timestamp via the Repository.

### Settings
Placeholder tab. No content yet; will eventually hold things like transcription language and other app-level preferences. Language defaults to English until this exists.

## Data Model (Room)

Single entity, `AudioRecordEntity`:

| Field | Type | Notes |
|---|---|---|
| `id` | `Long` | Primary key, `autoGenerate = true` |
| `audioFilePath` | `String` | Path to the WAV file in internal storage |
| `transcript` | `String?` | Nullable — distinct from empty string, so "not yet transcribed" is unambiguous |
| `transcriptionStatus` | `TranscriptionStatus` (enum) | `PENDING`, `IN_PROGRESS`, `COMPLETED`, `FAILED`, `NO_SPEECH_DETECTED` |
| `recordedAt` | `Long` | Epoch millis, set at recording time |
| `lastAccessedAt` | `Long` | Epoch millis, initialized to `recordedAt`, updated on playback start. Drives List sort order |
| `durationMs` | `Long` | Captured when recording stops, avoids re-reading the file for display |
| `transcriptionEngine` | `String?` | Which engine produced the transcript (e.g. `"SpeechRecognizer"`) — useful once multiple engines are tested |
| `language` | `String?` | Locale used for that transcription |
| `title` | `String?` | User-editable display name; falls back to a timestamp-based label when null |

The DAO exposes list queries as `Flow<List<AudioRecordEntity>>` for reactive UI updates, and has an index on `lastAccessedAt` to keep the sort cheap as the table grows.

## Core Components

### `AudioCaptureEngine`
Wraps `AudioRecord`. Captures 16kHz mono 16-bit PCM and streams it directly to a WAV file on internal app storage (`getFilesDir`) as it records, rather than buffering the full recording in memory. Internal storage was chosen because recordings don't need to persist beyond the app or be visible to other apps/file managers.

### `TranscriptionEngine` (interface)
```
interface TranscriptionEngine {
    suspend fun transcribe(audioFile: File, language: String): TranscriptionResult
}
```
Operates on a completed WAV file — never live audio — so it works the same way regardless of whether recording just finished or transcription is being retried later. The initial implementation wraps Android's `SpeechRecognizer`. Because `SpeechRecognizer` manages its own microphone session rather than accepting external audio, and later engines (whisper.cpp, sherpa-onnx, `AudioRecord`-based test harnesses) instead accept a file or buffer, the interface is intentionally file-based: every implementation reads from the already-persisted WAV file rather than being fed live audio, which keeps the contract uniform across engines.

### `AudioRecordRepository`
Sole point of access to both Room and the filesystem. ViewModels depend on this, not on `AudioRecordDao` or `File` APIs directly. Responsibilities: CRUD on `AudioRecordEntity` rows, reading/writing/deleting the associated audio files, and updating `lastAccessedAt`.

### Transcription `CoroutineWorker`
Enqueued via WorkManager after a recording is saved. Looks up the record by ID, calls `TranscriptionEngine.transcribe()`, and writes the result back through the Repository. Runs as a suspend function (`doWork()`), so the transcribe-then-persist sequence reads as plain sequential code. Durable across process death and app backgrounding. On failure, status is set to `FAILED` and the affected screen prompts the user to manually retry — no automatic retry policy for v1.

## Data Flow

1. User taps record on Home → `AudioCaptureEngine` starts, streaming PCM to a WAV file; ViewModel exposes elapsed time + recording state.
2. User taps stop → file is finalized; Repository inserts an `AudioRecordEntity` row (`transcriptionStatus = PENDING`, empty transcript).
3. A transcription `CoroutineWorker` is enqueued for that record ID.
4. Worker calls `TranscriptionEngine.transcribe()`, then updates the row via the Repository (`transcript`, `transcriptionStatus = COMPLETED` or `FAILED`).
5. List screen, observing the Repository's `Flow`, updates automatically — no polling or manual refresh.
6. User taps a recording → View screen opens, `lastAccessedAt` updates, ExoPlayer loads the file for playback.

## Dependency Injection

Manual: constructor injection throughout, with a small factory/container responsible for building Repository, `TranscriptionEngine`, and `AudioCaptureEngine` instances and handing them to ViewModels. No Hilt, no annotation processing — chosen so the object graph stays easy to trace for a learning project. Revisit if the graph grows unwieldy.

## Permissions

`RECORD_AUDIO` only, requested at runtime before the first recording. Foreground-service requirements, notification permissions, and audio-focus/interruption handling are deliberately out of scope for now (see **Deferred / Future Work**).

## Error Handling & User Feedback

Three mechanisms, chosen per situation:

- **`AlertDialog`** — blocking confirmations, e.g. "Delete this recording?"
- **`Snackbar`**, triggered via a one-time event `Channel`/`SharedFlow` from the ViewModel (not `StateFlow`, which would re-emit and re-show the message on rotation/recomposition) — transient action failures: delete/rename failures, and prompting retry on a failed transcription.
- **Inline error text** — form-level validation, e.g. empty name on rename.

## Storage & Deletion

Audio files live in internal app storage, private to the app. Deleting a recording is not atomic across Room and the filesystem — there's no transaction spanning both storage systems, so the Repository deletes the **DB row first, then the file**, best-effort. If the file delete fails, the result is an orphaned file consuming storage but invisible to the user (chosen over the reverse order, which risks the user encountering a dangling reference to a missing file). An optional cleanup sweep for orphaned files is a candidate for later, not required for v1.

## Deferred / Future Work

- Authentication
- Foreground service / background recording resilience, audio focus handling (phone calls, other apps)
- Word-level synced transcript scrubbing (needs per-word timestamps; not all engines provide these)
- Settings screen content (language selection, other preferences)
- Storage quota management, orphaned file cleanup sweep
- Automatic retry policy for failed transcriptions (currently manual, user-prompted)
