package com.jimmcgaw.whatsayyou.audio

import java.nio.ByteBuffer
import java.nio.ByteOrder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class WavHeaderTest {

    @Test
    fun build_producesCorrectRiffWaveHeader() {
        val header = WavHeader.build(dataSizeBytes = 3_200, sampleRateHz = 16_000, channelCount = 1, bitsPerSample = 16)
        val buffer = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN)

        assertEquals(44, header.size)
        assertEquals("RIFF", ascii(header, 0, 4))
        assertEquals(36 + 3_200, buffer.getInt(4))
        assertEquals("WAVE", ascii(header, 8, 4))
        assertEquals("fmt ", ascii(header, 12, 4))
        assertEquals(16, buffer.getInt(16))
        assertEquals(1, buffer.getShort(20).toInt()) // PCM
        assertEquals(1, buffer.getShort(22).toInt()) // channel count
        assertEquals(16_000, buffer.getInt(24)) // sample rate
        assertEquals(32_000, buffer.getInt(28)) // byte rate = 16000 * 1 * 2
        assertEquals(2, buffer.getShort(32).toInt()) // block align
        assertEquals(16, buffer.getShort(34).toInt()) // bits per sample
        assertEquals("data", ascii(header, 36, 4))
        assertEquals(3_200, buffer.getInt(40))
    }

    @Test
    fun build_negativeDataSize_throws() {
        assertThrows(IllegalArgumentException::class.java) {
            WavHeader.build(dataSizeBytes = -1, sampleRateHz = 16_000, channelCount = 1, bitsPerSample = 16)
        }
    }

    private fun ascii(bytes: ByteArray, offset: Int, length: Int): String =
        String(bytes, offset, length, Charsets.US_ASCII)
}
