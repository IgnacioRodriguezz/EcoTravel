package com.example.ecotravel

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.ecotravel.databinding.ActivityMainBinding
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

// activity principal: permite configurar un nuevo viaje y muestra el resumen acumulado
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: SharedPreferences
    private val viewModel: TravelViewModel by viewModels()

    // launcher que recibe los IDs eliminados en el historial y los aplica sobre el ViewModel local
    private val historyLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val removedIds = result.data?.getLongArrayExtra("removed_ids") ?: return@registerForActivityResult
            removedIds.forEach { id ->
                viewModel.trips.value?.find { it.id == id }?.let { viewModel.removeTrip(it) }
            }
            saveTrips(viewModel.trips.value.orEmpty())
            showSummary()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = getSharedPreferences("ecotravel_prefs", MODE_PRIVATE)
        loadTrips()
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

        // validación: ambos campos son obligatorios y la distancia debe ser positiva
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
        saveTrips(viewModel.trips.value.orEmpty())
        saveSummary(destination)

        val intent = Intent(this, ResultActivity::class.java)
        intent.putExtra("trip", trip)
        startActivity(intent)
    }

    private fun openHistory() {
        val intent = Intent(this, HistoryActivity::class.java)
        intent.putParcelableArrayListExtra("trips", ArrayList(viewModel.trips.value.orEmpty()))
        historyLauncher.launch(intent)
    }

    // guarda el último destino en SharedPreferences (el total se recalcula desde la lista)
    private fun saveSummary(destination: String) {
        prefs.edit().putString("last_destination", destination).apply()
        showSummary()
    }

    // muestra el total acumulado calculado desde la lista actual y el último destino
    private fun showSummary() {
        val total = viewModel.trips.value.orEmpty().sumOf { it.distanceKm }
        binding.tvTotalDistance.text = String.format(Locale.getDefault(), "%.1f km", total)
        binding.tvLastDestination.text = prefs.getString("last_destination", null) ?: "—"
    }

    // persiste la lista de viajes como JSON en SharedPreferences
    private fun saveTrips(trips: List<TripEntry>) {
        val arr = JSONArray()
        trips.forEach { trip ->
            val obj = JSONObject()
            obj.put("id", trip.id)
            obj.put("destination", trip.destination)
            obj.put("distanceKm", trip.distanceKm)
            obj.put("transport", trip.transport)
            obj.put("isBusiness", trip.isBusiness)
            obj.put("factor", trip.factor)
            obj.put("co2", trip.co2)
            arr.put(obj)
        }
        prefs.edit().putString("trips_json", arr.toString()).apply()
    }

    // carga los viajes guardados desde SharedPreferences al iniciar la app
    private fun loadTrips() {
        val json = prefs.getString("trips_json", null) ?: return
        val arr = JSONArray(json)
        val trips = (0 until arr.length()).map { i ->
            val obj = arr.getJSONObject(i)
            TripEntry(
                id = obj.getLong("id"),
                destination = obj.getString("destination"),
                distanceKm = obj.getDouble("distanceKm"),
                transport = obj.getString("transport"),
                isBusiness = obj.getBoolean("isBusiness"),
                factor = obj.getDouble("factor"),
                co2 = obj.getDouble("co2")
            )
        }
        viewModel.restoreTrips(trips)
    }
}
