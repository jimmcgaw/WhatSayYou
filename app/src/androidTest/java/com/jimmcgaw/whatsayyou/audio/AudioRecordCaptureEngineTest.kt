package com.jimmcgaw.whatsayyou.audio

import android.Manifest
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AudioRecordCaptureEngineTest {

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.RECORD_AUDIO)

    @Test
    fun startRecording_writesValidWavFile() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val engine = AudioRecordCaptureEngine(context.filesDir)

        val recordingResult = async { engine.startRecording() }
        delay(1_000)
        engine.stopRecording()
        val file = recordingResult.await()

        try {
            assertTrue(file.exists())
            assertTrue(file.length() > WavHeader.HEADER_SIZE_BYTES)

            val header = file.readBytes().copyOfRange(0, WavHeader.HEADER_SIZE_BYTES)
            assertEquals("RIFF", String(header, 0, 4, Charsets.US_ASCII))
            assertEquals("WAVE", String(header, 8, 4, Charsets.US_ASCII))
            assertEquals("data", String(header, 36, 4, Charsets.US_ASCII))
        } finally {
            file.delete()
        }
    }
}
