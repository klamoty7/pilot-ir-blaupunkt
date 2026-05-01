package com.example.universalirremote

import android.content.Intent
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.universalirremote.data.DeviceDatabase
import com.example.universalirremote.databinding.ActivityMainBinding
import com.example.universalirremote.ir.IRManager
import com.example.universalirremote.model.IRDevice

/**
 * Main activity displaying the remote control UI.
 *
 * This activity provides:
 * - A full TV remote layout with all standard buttons
 * - Real IR transmission via [IRManager] using ConsumerIrManager API
 * - Visual LED blink feedback on button press
 * - Haptic feedback on button press
 * - Display of last transmitted NEC and Pronto codes
 * - Device selection via [DeviceSelectActivity]
 */
class MainActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_DEVICE_ID = "extra_device_id"
        const val REQUEST_DEVICE_SELECT = 1001
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var irManager: IRManager
    private var currentDevice: IRDevice = DeviceDatabase.ALL_DEVICES.first()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        irManager = IRManager(this)

        setupDeviceHeader()
        setupRemoteButtons()
        checkIRSupport()
    }

    // ─────────────────────────────────────────────────────────────────
    // Setup
    // ─────────────────────────────────────────────────────────────────

    private fun setupDeviceHeader() {
        updateDeviceDisplay()
        binding.btnSelectDevice.setOnClickListener {
            val intent = Intent(this, DeviceSelectActivity::class.java)
            intent.putExtra(EXTRA_DEVICE_ID, currentDevice.id)
            startActivityForResult(intent, REQUEST_DEVICE_SELECT)
        }
    }

    private fun updateDeviceDisplay() {
        binding.tvDeviceName.text = "${currentDevice.brand} ${currentDevice.model}"
        binding.tvDeviceType.text = currentDevice.type
    }

    private fun checkIRSupport() {
        if (!irManager.hasIREmitter()) {
            binding.tvIrStatus.text = getString(R.string.ir_not_supported)
            binding.tvIrStatus.setTextColor(getColor(R.color.error_red))
            binding.tvIrWarning.visibility = View.VISIBLE
        } else {
            binding.tvIrStatus.text = getString(R.string.ir_ready)
            binding.tvIrStatus.setTextColor(getColor(R.color.accent_blue))
            binding.tvIrWarning.visibility = View.GONE
        }
    }

    private fun setupRemoteButtons() {
        // Power & Source
        binding.btnPower.setOnClickListener { sendIR("POWER", it) }
        binding.btnSource.setOnClickListener { sendIR("SOURCE", it) }

        // Volume
        binding.btnVolUp.setOnClickListener { sendIR("VOL+", it) }
        binding.btnVolDown.setOnClickListener { sendIR("VOL-", it) }
        binding.btnMute.setOnClickListener { sendIR("MUTE", it) }

        // Channel
        binding.btnChUp.setOnClickListener { sendIR("CH+", it) }
        binding.btnChDown.setOnClickListener { sendIR("CH-", it) }

        // Navigation
        binding.btnUp.setOnClickListener { sendIR("UP", it) }
        binding.btnDown.setOnClickListener { sendIR("DOWN", it) }
        binding.btnLeft.setOnClickListener { sendIR("LEFT", it) }
        binding.btnRight.setOnClickListener { sendIR("RIGHT", it) }
        binding.btnOk.setOnClickListener { sendIR("OK", it) }

        // Menu / Back / Exit
        binding.btnMenu.setOnClickListener { sendIR("MENU", it) }
        binding.btnBack.setOnClickListener { sendIR("BACK", it) }
        binding.btnExit.setOnClickListener { sendIR("EXIT", it) }

        // Number pad
        binding.btn0.setOnClickListener { sendIR("0", it) }
        binding.btn1.setOnClickListener { sendIR("1", it) }
        binding.btn2.setOnClickListener { sendIR("2", it) }
        binding.btn3.setOnClickListener { sendIR("3", it) }
        binding.btn4.setOnClickListener { sendIR("4", it) }
        binding.btn5.setOnClickListener { sendIR("5", it) }
        binding.btn6.setOnClickListener { sendIR("6", it) }
        binding.btn7.setOnClickListener { sendIR("7", it) }
        binding.btn8.setOnClickListener { sendIR("8", it) }
        binding.btn9.setOnClickListener { sendIR("9", it) }
    }

    // ─────────────────────────────────────────────────────────────────
    // IR Transmission
    // ─────────────────────────────────────────────────────────────────

    private fun sendIR(key: String, view: View) {
        // Haptic feedback
        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)

        // LED blink animation
        blinkLed()

        val code = currentDevice.codes[key]
        if (code == null) {
            showToast(getString(R.string.no_code_for_button, key))
            updateCodeDisplay("-", getString(R.string.no_code_in_database))
            return
        }

        // Transmit IR signal
        val success = irManager.transmit(code)

        // Update code display
        updateCodeDisplay(code.nec, code.pronto)

        if (!success && !irManager.hasIREmitter()) {
            // Demo mode - show code but can't transmit
            showToast(getString(R.string.demo_mode_toast, key))
        }
    }

    private fun blinkLed() {
        binding.ledIndicator.setBackgroundResource(R.drawable.led_active)
        binding.ledIndicator.postDelayed({
            binding.ledIndicator.setBackgroundResource(R.drawable.led_inactive)
        }, 120)
    }

    private fun updateCodeDisplay(nec: String, pronto: String) {
        binding.tvNecCode.text = "NEC: $nec"
        binding.tvProntoCode.text = if (pronto.length > 40) {
            "Pronto: ${pronto.take(40)}..."
        } else {
            "Pronto: $pronto"
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // ─────────────────────────────────────────────────────────────────
    // Activity Result
    // ─────────────────────────────────────────────────────────────────

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_DEVICE_SELECT && resultCode == RESULT_OK) {
            val deviceId = data?.getStringExtra(EXTRA_DEVICE_ID)
            if (deviceId != null) {
                val device = DeviceDatabase.findById(deviceId)
                if (device != null) {
                    currentDevice = device
                    updateDeviceDisplay()
                    showToast(getString(R.string.device_selected, device.brand, device.model))
                }
            }
        }
    }
}
