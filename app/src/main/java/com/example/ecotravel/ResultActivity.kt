package com.example.ecotravel

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.IntentCompat
import com.example.ecotravel.databinding.ActivityResultBinding
import java.util.Locale

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val trip = IntentCompat.getParcelableExtra(intent, "trip", TripEntry::class.java)
        if (trip == null) {
            finish()
            return
        }

        val color = transportColor(trip.transport)
        binding.headerLayout.setBackgroundColor(color)
        binding.tvEmoji.text = transportEmoji(trip.transport)
        binding.tvTransport.text = trip.transport

        binding.tvDestination.text = trip.destination
        binding.tvFactor.text = String.format(Locale.getDefault(), "%.3f kg CO₂/km", trip.factor)
        binding.tvBusiness.text = if (trip.isBusiness) "Sí (+15%)" else "No"
        binding.tvTotalCo2.text = String.format(Locale.getDefault(), "%.2f kg CO₂", trip.co2)

        binding.btnBackArrow.setOnClickListener { finish() }
        binding.btnBack.setOnClickListener { finish() }
    }
}
