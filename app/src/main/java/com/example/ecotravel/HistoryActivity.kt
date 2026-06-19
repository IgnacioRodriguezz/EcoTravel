package com.example.ecotravel

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.IntentCompat
import com.example.ecotravel.databinding.ActivityHistoryBinding

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private val viewModel: TravelViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            val trips = IntentCompat.getParcelableArrayListExtra(
                intent, "trips", TripEntry::class.java
            ).orEmpty()
            viewModel.setTrips(trips)
        }

        binding.btnBack.setOnClickListener { finish() }

        val adapter = TripAdapter { trip -> confirmDelete(trip) }
        binding.rvTrips.adapter = adapter

        viewModel.trips.observe(this) { trips ->
            adapter.submitList(trips)
            binding.tvEmpty.visibility = if (trips.isEmpty()) View.VISIBLE else View.GONE
            setResult(RESULT_OK, Intent().putParcelableArrayListExtra("trips", ArrayList(trips)))
        }
    }

    private fun confirmDelete(trip: TripEntry) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar viaje")
            .setMessage("¿Querés eliminar el viaje a ${trip.destination}?")
            .setPositiveButton("Eliminar") { _, _ -> viewModel.removeTrip(trip) }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
