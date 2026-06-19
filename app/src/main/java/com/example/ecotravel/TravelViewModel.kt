package com.example.ecotravel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

// viewModel que centraliza la lógica de cálculo y el estado de la lista de viajes
class TravelViewModel : ViewModel() {

    // lista privada modificable; se expone solo lectura a las Activities
    private val _trips = MutableLiveData<List<TripEntry>>(emptyList())
    val trips: LiveData<List<TripEntry>> get() = _trips

    private var nextId = 0L

    // calcula las emisiones de CO2 según distancia, transporte y si es viaje de negocios
    fun calculateTrip(
        destination: String,
        distanceKm: Double,
        transport: String,
        isBusiness: Boolean
    ): TripEntry {
        // factor de emisión en kg CO2 por kilómetro según el medio de transporte
        val factor = when (transport) {
            "Avión" -> 0.255
            "Auto" -> 0.21
            else -> 0.041 // Tren
        }
        // recargo del 15% para viajes de negocios por logística adicional
        val co2 = distanceKm * factor * if (isBusiness) 1.15 else 1.0
        return TripEntry(nextId++, destination, distanceKm, transport, isBusiness, factor, co2)
    }

    fun addTrip(trip: TripEntry) {
        _trips.value = _trips.value.orEmpty() + trip
    }

    // reemplaza la lista completa, usado al volver del historial con viajes eliminados
    fun setTrips(trips: List<TripEntry>) {
        _trips.value = trips
    }

    // carga los viajes guardados al iniciar y reposiciona el contador de IDs
    fun restoreTrips(trips: List<TripEntry>) {
        _trips.value = trips
        // evita conflictos de ID al agregar viajes nuevos después de cargar los guardados
        nextId = (trips.maxOfOrNull { it.id } ?: -1L) + 1
    }

    fun removeTrip(trip: TripEntry) {
        _trips.value = _trips.value.orEmpty().filter { it.id != trip.id }
    }
}
