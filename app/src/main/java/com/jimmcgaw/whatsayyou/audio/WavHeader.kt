package com.jimmcgaw.whatsayyou.audio

import java.nio.ByteBuffer
import java.nio.ByteOrder

object WavHeader {
    const val HEADER_SIZE_BYTES = 44

    fun build(dataSizeBytes: Int, sampleRateHz: Int, channelCount: Int, bitsPerSample: Int): ByteArray {
        require(dataSizeBytes >= 0) { "dataSizeBytes must be >= 0, was $dataSizeBytes" }

        val blockAlign = channelCount * (bitsPerSample / 8)
        val byteRate = sampleRateHz * blockAlign

        val buffer = ByteBuffer.allocate(HEADER_SIZE_BYTES).order(ByteOrder.LITTLE_ENDIAN)
        buffer.put("RIFF".toByteArray(Charsets.US_ASCII))
        buffer.putInt(36 + dataSizeBytes)
        buffer.put("WAVE".toByteArray(Charsets.US_ASCII))
        buffer.put("fmt ".toByteArray(Charsets.US_ASCII))
        buffer.putInt(16)
        buffer.putShort(1) // PCM
        buffer.putShort(channelCount.toShort())
        buffer.putInt(sampleRateHz)
        buffer.putInt(byteRate)
        buffer.putShort(blockAlign.toShort())
        buffer.putShort(bitsPerSample.toShort())
        buffer.put("data".toByteArray(Charsets.US_ASCII))
        buffer.putInt(dataSizeBytes)

        return buffer.array()
    }
}
