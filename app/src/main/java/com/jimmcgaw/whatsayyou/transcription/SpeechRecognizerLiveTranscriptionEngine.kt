package com.jimmcgaw.whatsayyou.transcription

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

/**
 * Uses SpeechRecognizer the way it's actually designed to work: listening live, at the same
 * time AudioCaptureEngine records the WAV, rather than fed a finished file afterward (see
 * SpeechRecognizerTranscriptionEngine's KDoc for why the file-based approach doesn't work).
 */
class SpeechRecognizerLiveTranscriptionEngine(private val context: Context) : LiveTranscriptionEngine {

    private var recognizer: SpeechRecognizer? = null

    override suspend fun startListening(language: String): TranscriptionResult =
        withContext(Dispatchers.Main) {
            suspendCancellableCoroutine { continuation ->
                if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                    continuation.resumeWith(Result.success(TranscriptionResult.Failure("No recognition service available on this device")))
                    return@suspendCancellableCoroutine
                }

                val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
                this@SpeechRecognizerLiveTranscriptionEngine.recognizer = recognizer
                var finished = false

                fun finish(result: TranscriptionResult) {
                    if (finished) return
                    finished = true
                    recognizer.destroy()
                    this@SpeechRecognizerLiveTranscriptionEngine.recognizer = null
                    continuation.resumeWith(Result.success(result))
                }

                recognizer.setRecognitionListener(object : RecognitionListener {
                    override fun onResults(results: Bundle) {
                        val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val transcript = matches?.firstOrNull()
                        finish(
                            if (transcript.isNullOrBlank()) {
                                TranscriptionResult.NoSpeechDetected
                            } else {
                                TranscriptionResult.Success(transcript)
                            },
                        )
                    }

                    override fun onError(error: Int) {
                        val result = when (error) {
                            SpeechRecognizer.ERROR_NO_MATCH, SpeechRecognizer.ERROR_SPEECH_TIMEOUT ->
                                TranscriptionResult.NoSpeechDetected
                            else -> TranscriptionResult.Failure("SpeechRecognizer error code $error")
                        }
                        finish(result)
                    }

                    override fun onReadyForSpeech(params: Bundle?) {}
                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {}
                    override fun onPartialResults(partialResults: Bundle?) {}
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, language)
                }
                recognizer.startListening(intent)

                continuation.invokeOnCancellation { recognizer.destroy() }
            }
        }

    override fun stopListening() {
        recognizer?.stopListening()
    }
}
