package com.example.pilotir

import android.content.Context
import android.hardware.ConsumerIrManager
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var irManager: ConsumerIrManager
    private val FREQUENCY = 38000

    // Kody NEC dla Blaupunkt BN40F1132EEB w formacie pattern dla ConsumerIrManager
    private val BLAUPUNKT_CODES = mapOf(
        "POWER" to intArrayOf(9000,4500,560,560,560,1690,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,1690,560,560,560,560,560,1690,560,1690,560,1690,560,560,560,560,560,560,560,1690,560,560,560,560,560,560,560,560,560,560,560,1690,560,1690,560,560,560,560,560,560,560,1690,560,39416),
        "VOL_UP" to intArrayOf(9000,4500,560,560,560,1690,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,1690,560,1690,560,560,560,560,560,560,560,560,560,1690,560,560,560,560,560,560,560,560,560,560,560,560,560,1690,560,1690,560,1690,560,560,560,560,560,560,560,560,560,39416),
        "VOL_DOWN" to intArrayOf(9000,4500,560,560,560,1690,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,1690,560,1690,560,1690,560,1690,560,560,560,560,560,560,560,560,560,560,560,560,560,39416),
        "CH_UP" to intArrayOf(9000,4500,560,560,560,1690,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,1690,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,39416),
        "CH_DOWN" to intArrayOf(9000,4500,560,560,560,1690,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,1690,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,39416),
        "MUTE" to intArrayOf(9000,4500,560,560,560,1690,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,1690,560,1690,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,39416),
        "SOURCE" to intArrayOf(9000,4500,560,560,560,1690,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,39416),
        "MENU" to intArrayOf(9000,4500,560,560,560,1690,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,39416),
        "OK" to intArrayOf(9000,4500,560,560,560,1690,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,560,39416)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        irManager = getSystemService(Context.CONSUMER_IR_SERVICE) as ConsumerIrManager
        if (!irManager.hasIrEmitter()) {
            Toast.makeText(this, "Brak nadajnika IR w telefonie!", Toast.LENGTH_LONG).show()
        }
        findViewById<Button>(R.id.btnPower).setOnClickListener { transmit("POWER") }
        findViewById<Button>(R.id.btnVolUp).setOnClickListener { transmit("VOL_UP") }
        findViewById<Button>(R.id.btnVolDown).setOnClickListener { transmit("VOL_DOWN") }
        findViewById<Button>(R.id.btnChUp).setOnClickListener { transmit("CH_UP") }
        findViewById<Button>(R.id.btnChDown).setOnClickListener { transmit("CH_DOWN") }
        findViewById<Button>(R.id.btnMute).setOnClickListener { transmit("MUTE") }
        findViewById<Button>(R.id.btnSource).setOnClickListener { transmit("SOURCE") }
        findViewById<Button>(R.id.btnMenu).setOnClickListener { transmit("MENU") }
        findViewById<Button>(R.id.btnOk).setOnClickListener { transmit("OK") }
    }

    private fun transmit(command: String) {
        val pattern = BLAUPUNKT_CODES[command]
        if (pattern != null) {
            try {
                irManager.transmit(FREQUENCY, pattern)
                Toast.makeText(this, "Wysłano: $command", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Błąd: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
