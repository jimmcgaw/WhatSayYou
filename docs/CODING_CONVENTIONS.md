# Kotlin Coding Conventions — WhatSayYou

These conventions exist to keep the codebase easy to read and change as it grows, especially given this is a learning project — code should make its own reasoning visible rather than relying on framework magic.

## General Principles

- **DRY, but not prematurely.** Extract shared logic once it's actually duplicated a second time, not in anticipation of duplication. Two similar-looking blocks aren't automatically worth a shared abstraction if they're likely to diverge.
- **Small, single-responsibility functions.** A function should do one thing describable in its name. If describing what a function does requires "and," it's probably two functions. Favor several small, well-named private functions over one long one with internal comments marking sections.
- **Single-responsibility classes.** `AudioCaptureEngine` captures audio. `TranscriptionEngine` transcribes a file. Neither should reach into Room or navigation. If a class's constructor needs more than ~4-5 dependencies, that's a signal it's doing too much.
- **Favor composition over inheritance.** Prefer interfaces + small implementations (as with `TranscriptionEngine`) over class hierarchies.

## Naming

- Classes/interfaces/objects: `PascalCase` (`AudioCaptureEngine`, `TranscriptionEngine`).
- Functions/properties/variables: `camelCase` (`startRecording()`, `lastAccessedAt`).
- Constants (`const val`, top-level or companion object): `UPPER_SNAKE_CASE` (`SAMPLE_RATE_HZ`).
- Boolean properties/functions read as a question or state: `isRecording`, `hasTranscript`, not `recording` or `transcriptFlag`.
- Suffix implementations of an interface with what they are, not `Impl`: `SpeechRecognizerTranscriptionEngine`, not `TranscriptionEngineImpl`. Makes it obvious at a glance which engine you're looking at once there are several.

## Immutability

- Prefer `val` over `var` everywhere it's viable.
- Model state with immutable `data class`es; when state changes, produce a new instance rather than mutating in place (`copy()` is your friend).
- Expose UI state from ViewModels as `StateFlow<T>` where `T` is an immutable data class, not as multiple separate mutable properties.
- Collections exposed outside a class should be read-only types (`List`, not `MutableList`).

## Coroutines

- Use structured concurrency: launch coroutines from `viewModelScope` (ViewModels) or the scope a `CoroutineWorker` provides — never `GlobalScope`.
- Blocking or I/O-bound work (raw `AudioRecord.read()` loops, file I/O) runs on `Dispatchers.IO`, explicitly.
- Room DAO methods are `suspend fun` (single operations) or return `Flow` (observed queries) — never wrapped in manual `Executor`/callback code.
- One-time UI events (errors, retry prompts) go through a `Channel` or `SharedFlow`, not `StateFlow` — `StateFlow`'s replay behavior means an error would incorrectly re-fire on recomposition or configuration change.
- Wrap resource-owning loops (e.g. the `AudioRecord` read loop) in `try { ... } finally { ... }` so cleanup (`release()`) runs on both normal completion and cancellation.

## Error Handling

- Prefer a sealed `Result`-style type for operations that can meaningfully fail (e.g. transcription), over throwing exceptions across architectural boundaries (Repository → ViewModel).
- Reserve exceptions for genuinely exceptional/programmer-error conditions, not expected failure paths like "transcription engine returned no speech" (that's `TranscriptionStatus.NO_SPEECH_DETECTED`, not a thrown error).
- Every `catch` block does something meaningful (log, surface to the user, retry) — no empty catch blocks.

## Architecture Boundaries

- ViewModels depend only on Repository interfaces, never directly on `AudioRecordDao`, `File`/filesystem APIs, or a specific `TranscriptionEngine` implementation.
- `TranscriptionEngine` implementations are swapped via the manual DI factory, not by changing call sites — code depending on transcription should reference the `TranscriptionEngine` interface, never a concrete engine class.
- Keep Compose UI code free of business logic — Composables read state and forward user actions to the ViewModel; they don't compute, validate, or make decisions themselves beyond simple display logic.

## Jetpack Compose

- Favor stateless Composables that take state and lambdas as parameters; hoist state to the ViewModel rather than holding it in `remember` unless it's purely UI-local (e.g. a dropdown's open/closed state).
- One Composable, one responsibility — break large screens into smaller named Composables rather than one long function with nested layout blocks.
- Preview functions (`@Preview`) for non-trivial Composables are encouraged but not required for every one.

## Testing

- Minimal but present: aim for at least one happy-path test and one failure-path test per non-trivial class (`AudioCaptureEngine`, each `TranscriptionEngine` implementation, `AudioRecordRepository`, ViewModels), not exhaustive coverage.
- Favor fake implementations of interfaces (a `FakeTranscriptionEngine`, an in-memory Repository) over mocking frameworks where practical — easier to read, and avoids over-specifying interaction details.
- ViewModel tests should not depend on Android framework classes; keep them plain JUnit + coroutines-test where possible.

## Comments & Documentation

- KDoc on public interfaces and non-obvious public functions (why, not what) — especially on `TranscriptionEngine` implementations, since the choice of engine carries trade-offs worth recording.
- Avoid comments that just restate the code (`// increment counter` above `counter++`). If a function needs a comment to explain what it does, consider whether it should be split or renamed instead.
