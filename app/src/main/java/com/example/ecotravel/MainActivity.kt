package com.example.ecotravel

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.IntentCompat
import com.example.ecotravel.databinding.ActivityMainBinding
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: SharedPreferences
    private val viewModel: TravelViewModel by viewModels()

    private val historyLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data ?: return@registerForActivityResult
            val updated = IntentCompat.getParcelableArrayListExtra(
                data, "trips", TripEntry::class.java
            ).orEmpty()
            viewModel.setTrips(updated)
            val total = updated.sumOf { it.distanceKm }.toFloat()
            prefs.edit().putFloat("total_distance", total).apply()
            showSummary()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = getSharedPreferences("ecotravel_prefs", MODE_PRIVATE)
        showSummary()
        setupListeners()
    }

    private fun setupListeners() {
        binding.btnCalculate.setOnClickListener { calculate() }
        binding.btnHistory.setOnClickListener { openHistory() }
    }

    private fun calculate() {
        val destination = binding.etDestination.text?.toString()?.trim().orEmpty()
        val distance = binding.etDistance.text?.toString()?.trim()?.toDoubleOrNull()

        binding.tilDestination.error = null
        binding.tilDistance.error = null

        if (destination.isEmpty()) {
            binding.tilDestination.error = "Ingresá un destino"
            return
        }
        if (distance == null || distance <= 0.0) {
            binding.tilDistance.error = "La distancia debe ser mayor a 0"
            return
        }

        val transport = when (binding.rgTransport.checkedRadioButtonId) {
            R.id.rbCar -> "Auto"
            R.id.rbTrain -> "Tren"
            else -> "Avión"
        }

        val trip = viewModel.calculateTrip(destination, distance, transport, binding.cbBusiness.isChecked)
        viewModel.addTrip(trip)
        saveSummary(destination, distance)

        val intent = Intent(this, ResultActivity::class.java)
        intent.putExtra("trip", trip)
        startActivity(intent)
    }

    private fun openHistory() {
        val intent = Intent(this, HistoryActivity::class.java)
        intent.putParcelableArrayListExtra("trips", ArrayList(viewModel.trips.value.orEmpty()))
        historyLauncher.launch(intent)
    }

    private fun saveSummary(destination: String, distance: Double) {
        val total = prefs.getFloat("total_distance", 0f) + distance.toFloat()
        prefs.edit()
            .putFloat("total_distance", total)
            .putString("last_destination", destination)
            .apply()
        showSummary()
    }

    private fun showSummary() {
        val total = prefs.getFloat("total_distance", 0f)
        binding.tvTotalDistance.text = String.format(Locale.getDefault(), "%.1f km", total)
        binding.tvLastDestination.text = prefs.getString("last_destination", null) ?: "—"
    }
}
