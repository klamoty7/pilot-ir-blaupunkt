package com.example.universalirremote.ir

import android.content.Context
import android.hardware.ConsumerIrManager
import android.os.Build
import android.util.Log
import com.example.universalirremote.model.IRCode

/**
 * Manages IR transmission using Android's [ConsumerIrManager] API.
 *
 * This class wraps the Android ConsumerIrManager to provide a simple interface
 * for transmitting IR signals. It handles:
 * - Hardware availability detection
 * - Pronto hex format parsing and conversion to raw timing arrays
 * - NEC protocol encoding as fallback
 * - Carrier frequency selection
 *
 * Supported devices (phones with built-in IR blaster):
 * - Xiaomi (most models)
 * - Huawei (Mate/P series)
 * - Honor
 * - Samsung Galaxy S4/S5 (older models)
 * - HTC One M7/M8
 * - LG G5
 */
class IRManager(context: Context) {

    private val TAG = "IRManager"
    private val irManager: ConsumerIrManager? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            context.getSystemService(Context.CONSUMER_IR_SERVICE) as? ConsumerIrManager
        } else null

    /**
     * Returns true if the device has a functional IR blaster.
     */
    fun hasIREmitter(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            irManager?.hasIrEmitter() == true
        } else false
    }

    /**
     * Transmits an IR code using the Pronto hex format.
     *
     * @param code The [IRCode] containing Pronto hex string and frequency
     * @return true if transmission was initiated successfully
     */
    fun transmit(code: IRCode): Boolean {
        if (!hasIREmitter()) {
            Log.w(TAG, "No IR emitter available on this device")
            return false
        }

        return try {
            val pattern = parseProntoHex(code.pronto)
            if (pattern != null && pattern.isNotEmpty()) {
                val frequency = extractFrequencyFromPronto(code.pronto) ?: code.frequency
                Log.d(TAG, "Transmitting IR: freq=${frequency}Hz, pattern_length=${pattern.size}")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    irManager?.transmit(frequency, pattern)
                }
                true
            } else {
                // Fallback: try NEC encoding
                val necPattern = encodeNEC(code.nec, code.frequency)
                if (necPattern != null) {
                    Log.d(TAG, "Transmitting NEC fallback: ${code.nec}")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        irManager?.transmit(code.frequency, necPattern)
                    }
                    true
                } else {
                    Log.e(TAG, "Failed to parse IR code: ${code.nec}")
                    false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "IR transmission error: ${e.message}", e)
            false
        }
    }

    /**
     * Returns the list of carrier frequencies supported by the IR hardware.
     */
    fun getSupportedFrequencies(): Array<ConsumerIrManager.CarrierFrequencyRange>? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            irManager?.carrierFrequencies
        } else null
    }

    // ─────────────────────────────────────────────────────────────────
    // Pronto Hex Parser
    // ─────────────────────────────────────────────────────────────────

    /**
     * Parses a Pronto hex string into an Android-compatible raw timing array (microseconds).
     *
     * Pronto format: "0000 006D 0022 0002 ..."
     * - Word 0: 0000 (learned code)
     * - Word 1: carrier frequency code (1000000 / (word * 0.241246) Hz)
     * - Word 2: number of burst pairs in first sequence
     * - Word 3: number of burst pairs in second sequence
     * - Remaining: alternating mark/space durations in Pronto clock units
     *
     * Android ConsumerIrManager expects: int[] of alternating mark/space in microseconds
     */
    private fun parseProntoHex(pronto: String): IntArray? {
        return try {
            val words = pronto.trim().split("\\s+".toRegex())
                .map { it.toInt(16) }

            if (words.size < 4) return null

            // Word 1: frequency code → carrier period in Pronto clock units (0.241246 µs)
            val freqCode = words[1]
            val prontoClockMicros = if (freqCode > 0) {
                1_000_000.0 / (freqCode * 0.241246)
            } else {
                26.3 // default ~38 kHz
            }

            val seq1Count = words[2] // pairs in sequence 1
            val seq2Count = words[3] // pairs in sequence 2
            val dataStart = 4

            val totalPairs = seq1Count + seq2Count
            val result = mutableListOf<Int>()

            for (i in 0 until totalPairs) {
                val idx = dataStart + i * 2
                if (idx + 1 >= words.size) break
                val markUnits = words[idx]
                val spaceUnits = words[idx + 1]
                result.add((markUnits * prontoClockMicros).toInt())
                result.add((spaceUnits * prontoClockMicros).toInt())
            }

            if (result.isEmpty()) null else result.toIntArray()
        } catch (e: Exception) {
            Log.e(TAG, "Pronto parse error: ${e.message}")
            null
        }
    }

    /**
     * Extracts the carrier frequency (Hz) from a Pronto hex string.
     */
    private fun extractFrequencyFromPronto(pronto: String): Int? {
        return try {
            val words = pronto.trim().split("\\s+".toRegex())
            if (words.size < 2) return null
            val freqCode = words[1].toInt(16)
            if (freqCode == 0) return null
            (1_000_000.0 / (freqCode * 0.241246)).toInt()
        } catch (e: Exception) {
            null
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // NEC Protocol Encoder (fallback)
    // ─────────────────────────────────────────────────────────────────

    /**
     * Encodes a 32-bit NEC hex code into a raw timing array (microseconds).
     *
     * NEC protocol timings:
     * - Leader: 9000µs mark + 4500µs space
     * - Logical 0: 562µs mark + 562µs space
     * - Logical 1: 562µs mark + 1687µs space
     * - Stop bit: 562µs mark
     */
    private fun encodeNEC(necHex: String, frequency: Int = 38000): IntArray? {
        return try {
            val hex = necHex.removePrefix("0x").removePrefix("0X")
            val value = hex.toLong(16)

            val pattern = mutableListOf<Int>()

            // Leader pulse
            pattern.add(9000)
            pattern.add(4500)

            // 32 bits, LSB first
            for (i in 0 until 32) {
                val bit = (value shr i) and 1L
                pattern.add(562)
                pattern.add(if (bit == 1L) 1687 else 562)
            }

            // Stop bit
            pattern.add(562)

            pattern.toIntArray()
        } catch (e: Exception) {
            Log.e(TAG, "NEC encode error: ${e.message}")
            null
        }
    }
}
