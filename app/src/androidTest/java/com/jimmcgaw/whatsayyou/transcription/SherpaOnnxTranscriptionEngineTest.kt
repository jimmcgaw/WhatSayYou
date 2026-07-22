package com.jimmcgaw.whatsayyou.transcription

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SherpaOnnxTranscriptionEngineTest {

    @Test
    fun transcribe_knownAudio_returnsExpectedText() = runBlocking {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val testContext = InstrumentationRegistry.getInstrumentation().context
        val engine = SherpaOnnxTranscriptionEngine(appContext)

        val tempFile = File(appContext.cacheDir, "test_0.wav")
        testContext.assets.open("test_wavs/0.wav").use { input ->
            tempFile.outputStream().use { output -> input.copyTo(output) }
        }

        try {
            val result = engine.transcribe(tempFile, "en")

            assertTrue("Expected Success, got $result", result is TranscriptionResult.Success)
            val text = (result as TranscriptionResult.Success).transcript.uppercase()
            // Ground truth (trans.txt, bundled alongside this test wav in sherpa-onnx's own
            // release): "AFTER EARLY NIGHTFALL THE YELLOW LAMPS WOULD LIGHT UP HERE AND THERE
            // THE SQUALID QUARTER OF THE BROTHELS". Checking a distinctive word rather than an
            // exact match, since punctuation/casing from the real model may differ slightly.
            assertTrue("Expected transcript to mention SQUALID, got: $text", text.contains("SQUALID"))
        } finally {
            tempFile.delete()
        }
    }
}
