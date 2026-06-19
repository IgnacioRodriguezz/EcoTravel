package com.example.ecotravel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TravelViewModel : ViewModel() {

    private val _trips = MutableLiveData<List<TripEntry>>(emptyList())
    val trips: LiveData<List<TripEntry>> get() = _trips

    private var nextId = 0L

    fun calculateTrip(
        destination: String,
        distanceKm: Double,
        transport: String,
        isBusiness: Boolean
    ): TripEntry {
        val factor = when (transport) {
            "Avión" -> 0.255
            "Auto" -> 0.21
            else -> 0.041
        }
        val co2 = distanceKm * factor * if (isBusiness) 1.15 else 1.0
        return TripEntry(nextId++, destination, distanceKm, transport, isBusiness, factor, co2)
    }

    fun addTrip(trip: TripEntry) {
        _trips.value = _trips.value.orEmpty() + trip
    }

    fun setTrips(trips: List<TripEntry>) {
        _trips.value = trips
    }

    fun removeTrip(trip: TripEntry) {
        _trips.value = _trips.value.orEmpty().filter { it.id != trip.id }
    }
}
