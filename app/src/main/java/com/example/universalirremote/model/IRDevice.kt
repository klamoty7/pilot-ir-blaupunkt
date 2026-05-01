package com.example.universalirremote.model

import java.io.Serializable

/**
 * Represents a single IR code for a specific button/function.
 *
 * @property nec  NEC hex code (e.g. "0x4FB40BF4")
 * @property pronto Pronto hex string (space-separated hex words)
 * @property frequency Carrier frequency in Hz (default 38000 Hz for NEC)
 */
data class IRCode(
    val nec: String,
    val pronto: String,
    val frequency: Int = 38000
) : Serializable

/**
 * Represents a remote-controllable device with a map of button keys to IR codes.
 */
data class IRDevice(
    val id: String,
    val brand: String,
    val model: String,
    val type: String,
    val codes: Map<String, IRCode>
) : Serializable
