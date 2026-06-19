package com.example.ecotravel

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.IntentCompat
import com.example.ecotravel.databinding.ActivityHistoryBinding

// Activity que muestra el historial de viajes y permite eliminar entradas con clic largo
class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private val viewModel: TravelViewModel by viewModels()
    private lateinit var adapter: TripAdapter
    private val removedIds = mutableListOf<Long>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Carga la lista recibida desde MainActivity solo en la primera creación
        if (savedInstanceState == null) {
            val trips = IntentCompat.getParcelableArrayListExtra(
                intent, "trips", TripEntry::class.java
            ).orEmpty()
            viewModel.setTrips(trips)
        }

        binding.btnBack.setOnClickListener { returnResult() }

        // Intercepta el botón físico/gesto de volver para enviar el mismo resultado
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { returnResult() }
        })

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        adapter = TripAdapter { trip -> confirmDelete(trip) }
        binding.rvTrips.adapter = adapter

        viewModel.trips.observe(this) { trips ->
            adapter.submitList(trips)
            binding.tvEmpty.visibility = if (trips.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    // Devuelve solo los IDs eliminados; MainActivity aplica los cambios sobre su propia lista
    private fun returnResult() {
        val data = Intent().putExtra("removed_ids", removedIds.toLongArray())
        setResult(RESULT_OK, data)
        finish()
    }

    // Muestra un diálogo de confirmación antes de eliminar para evitar borrados accidentales
    private fun confirmDelete(trip: TripEntry) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar viaje")
            .setMessage("¿Querés eliminar el viaje a ${trip.destination}?")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.removeTrip(trip)
                removedIds.add(trip.id)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
