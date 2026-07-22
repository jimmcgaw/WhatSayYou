package com.jimmcgaw.whatsayyou.transcription

import android.content.Context
import com.k2fsa.sherpa.onnx.OfflineModelConfig
import com.k2fsa.sherpa.onnx.OfflineRecognizer
import com.k2fsa.sherpa.onnx.OfflineRecognizerConfig
import com.k2fsa.sherpa.onnx.OfflineWhisperModelConfig
import com.k2fsa.sherpa.onnx.WaveReader
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Wraps sherpa-onnx's offline (file-based) Whisper tiny.en recognizer, bundled as assets under
 * assets/sherpa-onnx-whisper-tiny.en/. Unlike SpeechRecognizer, this genuinely reads a finished
 * WAV file rather than requiring a live mic session, so it needs no concurrent capture and can't
 * corrupt AudioCaptureEngine's recording the way the SpeechRecognizer approach did.
 */
class SherpaOnnxTranscriptionEngine(private val context: Context) : TranscriptionEngine {

    private val recognizer: OfflineRecognizer by lazy {
        val config = OfflineRecognizerConfig(
            modelConfig = OfflineModelConfig(
                whisper = OfflineWhisperModelConfig(
                    encoder = "$MODEL_DIR/tiny.en-encoder.int8.onnx",
                    decoder = "$MODEL_DIR/tiny.en-decoder.int8.onnx",
                ),
                tokens = "$MODEL_DIR/tiny.en-tokens.txt",
                modelType = "whisper",
            ),
        )
        OfflineRecognizer(context.assets, config)
    }

    override suspend fun transcribe(audioFile: File, language: String): TranscriptionResult =
        withContext(Dispatchers.IO) {
            val wave = WaveReader.readWave(audioFile.absolutePath)
            val stream = recognizer.createStream()
            try {
                stream.acceptWaveform(wave.samples, wave.sampleRate)
                recognizer.decode(stream)
                val text = recognizer.getResult(stream).text
                if (text.isBlank()) TranscriptionResult.NoSpeechDetected else TranscriptionResult.Success(text)
            } finally {
                stream.release()
            }
        }

    companion object {
        private const val MODEL_DIR = "sherpa-onnx-whisper-tiny.en"
    }
}
