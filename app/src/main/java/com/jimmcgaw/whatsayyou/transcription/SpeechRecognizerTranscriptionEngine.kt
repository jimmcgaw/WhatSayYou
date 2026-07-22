package com.jimmcgaw.whatsayyou.transcription

import android.content.Context
import android.content.Intent
import android.media.AudioFormat
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.content.FileProvider
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

/**
 * Wraps Android's SpeechRecognizer, which is built for live mic input rather than a
 * pre-recorded file. This uses RecognizerIntent.EXTRA_AUDIO_SOURCE (an undocumented-in-practice
 * but public API) to point it at our WAV file via a FileProvider Uri instead.
 *
 * KNOWN LIMITATION: verified on a Pixel emulator (Google Play image) that the bound
 * RecognitionService rejects our FileProvider Uri outright ("IntentParsingUtil: No valid
 * audio source passed to EXTRA_AUDIO_SOURCE; ignoring audio source" in logcat) and silently
 * falls back to live mic input instead of reading the file. The Worker/Repository/List
 * pipeline around this engine is fully verified working; only this engine's core file-feeding
 * mechanism doesn't hold up on this device/service combination. TranscriptionEngine is an
 * interface specifically so this can be swapped for a real file-based engine (e.g. whisper.cpp,
 * sherpa-onnx) without touching any other layer.
 */
class SpeechRecognizerTranscriptionEngine(private val context: Context) : TranscriptionEngine {

    override suspend fun transcribe(audioFile: File, language: String): TranscriptionResult =
        withContext(Dispatchers.Main) {
            suspendCancellableCoroutine { continuation ->
                if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                    continuation.resumeWith(Result.success(TranscriptionResult.Failure("No recognition service available on this device")))
                    return@suspendCancellableCoroutine
                }

                val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
                var finished = false

                fun finish(result: TranscriptionResult) {
                    if (finished) return
                    finished = true
                    recognizer.destroy()
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

                val audioUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", audioFile)
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, language)
                    putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE, audioUri)
                    putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE_ENCODING, AudioFormat.ENCODING_PCM_16BIT)
                    putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE_SAMPLING_RATE, SAMPLE_RATE_HZ)
                    putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE_CHANNEL_COUNT, CHANNEL_COUNT)
                }
                recognizer.startListening(intent)

                continuation.invokeOnCancellation { recognizer.destroy() }
            }
        }

    companion object {
        private const val SAMPLE_RATE_HZ = 16_000
        private const val CHANNEL_COUNT = 1
    }
}
