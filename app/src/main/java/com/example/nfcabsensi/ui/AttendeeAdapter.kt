package com.example.nfcabsensi.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nfcabsensi.data.dao.StudentWithAttendance
import com.example.nfcabsensi.databinding.ItemAttendeeBinding

import android.graphics.Color
import com.example.nfcabsensi.data.entity.StudentEvent

class AttendeeAdapter(
    private val expectedOfflinePrice: Double,
    private val expectedOnlinePrice: Double,
    private val onEditClick: (StudentWithAttendance) -> Unit
) : ListAdapter<StudentWithAttendance, AttendeeAdapter.AttendeeViewHolder>(DiffCallback) {

    inner class AttendeeViewHolder(private val binding: ItemAttendeeBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: StudentWithAttendance) {
            binding.tvName.text = item.student.fullName
            binding.tvNim.text = item.student.nim

            val status = "${item.attendance.attendanceType} - ${item.attendance.billingCode}"
            binding.tvStatus.text = status

            val nominal = item.attendance.nominal
            val type = item.attendance.attendanceType
            val expected = if (type.equals("Offline", ignoreCase = true)) expectedOfflinePrice else expectedOnlinePrice

            binding.tvNominal.text = "Rp ${nominal.toInt()}"

            if (nominal < expected) {
                binding.tvNominal.setTextColor(Color.RED)
            } else {
                binding.tvNominal.setTextColor(Color.BLACK)
            }

            binding.root.setOnClickListener {
                onEditClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttendeeViewHolder {
        val binding = ItemAttendeeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AttendeeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AttendeeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<StudentWithAttendance>() {
            override fun areItemsTheSame(oldItem: StudentWithAttendance, newItem: StudentWithAttendance): Boolean {
                return oldItem.attendance.id == newItem.attendance.id
            }

            override fun areContentsTheSame(oldItem: StudentWithAttendance, newItem: StudentWithAttendance): Boolean {
                return oldItem == newItem
            }
        }
    }
}
