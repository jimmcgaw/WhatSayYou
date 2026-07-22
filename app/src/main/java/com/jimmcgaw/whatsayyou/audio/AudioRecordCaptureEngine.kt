package com.jimmcgaw.whatsayyou.audio

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.RandomAccessFile
import java.util.concurrent.atomic.AtomicBoolean

class AudioRecordCaptureEngine(private val outputDirectory: File) : AudioCaptureEngine {

    private val isRecording = AtomicBoolean(false)

    // RECORD_AUDIO is checked/requested by the caller (HomeScreen) before startRecording() is ever invoked.
    @Suppress("MissingPermission")
    override suspend fun startRecording(): File = withContext(Dispatchers.IO) {
        val outputFile = File(outputDirectory, "recording_${System.currentTimeMillis()}.wav")
        val minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE_HZ, CHANNEL_CONFIG, ENCODING)
        val audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE_HZ,
            CHANNEL_CONFIG,
            ENCODING,
            minBufferSize * 2,
        )
        val randomAccessFile = RandomAccessFile(outputFile, "rw")

        isRecording.set(true)
        var dataSizeBytes = 0
        try {
            randomAccessFile.write(WavHeader.build(0, SAMPLE_RATE_HZ, CHANNEL_COUNT, BITS_PER_SAMPLE))
            audioRecord.startRecording()

            val buffer = ByteArray(minBufferSize)
            while (isRecording.get()) {
                val bytesRead = audioRecord.read(buffer, 0, buffer.size)
                if (bytesRead > 0) {
                    randomAccessFile.write(buffer, 0, bytesRead)
                    dataSizeBytes += bytesRead
                }
            }
        } finally {
            audioRecord.stop()
            audioRecord.release()
            randomAccessFile.seek(0)
            randomAccessFile.write(WavHeader.build(dataSizeBytes, SAMPLE_RATE_HZ, CHANNEL_COUNT, BITS_PER_SAMPLE))
            randomAccessFile.close()
        }

        outputFile
    }

    override fun stopRecording() {
        isRecording.set(false)
    }

    companion object {
        private const val SAMPLE_RATE_HZ = 16_000
        private const val CHANNEL_COUNT = 1
        private const val BITS_PER_SAMPLE = 16
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val ENCODING = AudioFormat.ENCODING_PCM_16BIT
    }
}
