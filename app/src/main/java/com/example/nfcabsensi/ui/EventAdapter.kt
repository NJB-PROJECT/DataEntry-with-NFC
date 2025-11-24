package com.example.nfcabsensi.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nfcabsensi.data.entity.Event
import com.example.nfcabsensi.databinding.ItemEventBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EventAdapter(private val onClick: (Event) -> Unit) : ListAdapter<Event, EventAdapter.EventViewHolder>(DiffCallback) {

    class EventViewHolder(private val binding: ItemEventBinding, val onClick: (Event) -> Unit) : RecyclerView.ViewHolder(binding.root) {
        fun bind(event: Event) {
            binding.tvTitle.text = event.title
            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            binding.tvDate.text = dateFormat.format(Date(event.date))
            binding.tvLecturer.text = "Dosen: ${event.lecturerName}"

            binding.root.setOnClickListener {
                onClick(event)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = ItemEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventViewHolder(binding, onClick)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Event>() {
            override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
                return oldItem == newItem
            }
        }
    }
}
