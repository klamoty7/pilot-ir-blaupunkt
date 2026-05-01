package com.example.universalirremote

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.universalirremote.data.DeviceDatabase
import com.example.universalirremote.databinding.ActivityDeviceSelectBinding
import com.example.universalirremote.model.IRDevice

/**
 * Activity for selecting a target IR device from the database.
 *
 * Displays a searchable list of all available devices. The user can filter
 * by brand or model name. Selecting a device returns its ID to [MainActivity].
 */
class DeviceSelectActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDeviceSelectBinding
    private lateinit var adapter: DeviceAdapter
    private var currentDeviceId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeviceSelectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentDeviceId = intent.getStringExtra(MainActivity.EXTRA_DEVICE_ID) ?: ""

        setupToolbar()
        setupRecyclerView()
        setupSearch()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.select_device)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = DeviceAdapter(DeviceDatabase.ALL_DEVICES, currentDeviceId) { device ->
            val result = Intent()
            result.putExtra(MainActivity.EXTRA_DEVICE_ID, device.id)
            setResult(RESULT_OK, result)
            finish()
        }
        binding.recyclerDevices.layoutManager = LinearLayoutManager(this)
        binding.recyclerDevices.adapter = adapter
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString() ?: ""
                val filtered = DeviceDatabase.search(query)
                adapter.updateList(filtered)
                binding.tvNoResults.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    // ─────────────────────────────────────────────────────────────────
    // RecyclerView Adapter
    // ─────────────────────────────────────────────────────────────────

    inner class DeviceAdapter(
        private var devices: List<IRDevice>,
        private val selectedId: String,
        private val onSelect: (IRDevice) -> Unit
    ) : RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {

        inner class DeviceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvBrand: TextView = view.findViewById(R.id.tv_brand)
            val tvModel: TextView = view.findViewById(R.id.tv_model)
            val tvType: TextView = view.findViewById(R.id.tv_type)
            val tvCodeCount: TextView = view.findViewById(R.id.tv_code_count)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_device, parent, false)
            return DeviceViewHolder(view)
        }

        override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
            val device = devices[position]
            holder.tvBrand.text = device.brand
            holder.tvModel.text = device.model
            holder.tvType.text = device.type
            holder.tvCodeCount.text = "${device.codes.size} ${holder.itemView.context.getString(R.string.codes)}"

            // Highlight selected device
            holder.itemView.isSelected = device.id == selectedId
            holder.itemView.setBackgroundResource(
                if (device.id == selectedId) R.drawable.bg_device_selected
                else R.drawable.bg_device_normal
            )

            holder.itemView.setOnClickListener { onSelect(device) }
        }

        override fun getItemCount() = devices.size

        fun updateList(newList: List<IRDevice>) {
            devices = newList
            notifyDataSetChanged()
        }
    }
}
