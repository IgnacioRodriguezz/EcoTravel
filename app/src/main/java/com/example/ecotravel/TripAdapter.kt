package com.example.ecotravel

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ecotravel.databinding.ItemTripBinding
import java.util.Locale

class TripAdapter(
    private val onLongClick: (TripEntry) -> Unit
) : ListAdapter<TripEntry, TripAdapter.TripViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val binding = ItemTripBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TripViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        holder.bind(getItem(position), onLongClick)
    }

    class TripViewHolder(private val binding: ItemTripBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(trip: TripEntry, onLongClick: (TripEntry) -> Unit) {
            val color = transportColor(trip.transport)
            binding.tvEmoji.text = transportEmoji(trip.transport)
            binding.tvEmoji.backgroundTintList = ColorStateList.valueOf(color)
            binding.tvDestination.text = trip.destination
            binding.tvDetails.text = String.format(Locale.getDefault(), "%.0f km · %s", trip.distanceKm, trip.transport)
            binding.tvCo2.text = String.format(Locale.getDefault(), "%.2f kg", trip.co2)
            binding.tvCo2.setTextColor(color)
            binding.root.setOnLongClickListener {
                onLongClick(trip)
                true
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<TripEntry>() {
        override fun areItemsTheSame(oldItem: TripEntry, newItem: TripEntry) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: TripEntry, newItem: TripEntry) = oldItem == newItem
    }
}
