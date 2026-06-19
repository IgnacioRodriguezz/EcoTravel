package com.example.ecotravel

import android.os.Parcel
import android.os.Parcelable

// data class que representa un viaje calculado; implementa Parcelable para pasarla entre Activities
data class TripEntry(
    val id: Long,
    val destination: String,
    val distanceKm: Double,
    val transport: String,
    val isBusiness: Boolean,
    val factor: Double,
    val co2: Double
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString().orEmpty(),
        parcel.readDouble(),
        parcel.readString().orEmpty(),
        parcel.readByte() != 0.toByte(),
        parcel.readDouble(),
        parcel.readDouble()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(destination)
        parcel.writeDouble(distanceKm)
        parcel.writeString(transport)
        parcel.writeByte(if (isBusiness) 1 else 0)
        parcel.writeDouble(factor)
        parcel.writeDouble(co2)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<TripEntry> {
        override fun createFromParcel(parcel: Parcel): TripEntry = TripEntry(parcel)
        override fun newArray(size: Int): Array<TripEntry?> = arrayOfNulls(size)
    }
}

// devuelve el color asociado a cada medio de transporte para usarlo en UI
fun transportColor(transport: String): Int = when (transport) {
    "Avión" -> 0xFF1565C0.toInt()
    "Auto" -> 0xFFEF6C00.toInt()
    else -> 0xFF2E7D32.toInt()
}

fun transportEmoji(transport: String): String = when (transport) {
    "Avión" -> "✈️"
    "Auto" -> "🚗"
    else -> "🚆"
}
