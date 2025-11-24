package com.example.nfcabsensi.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.nfcabsensi.data.database.AppDatabase
import com.example.nfcabsensi.data.entity.Event
import com.example.nfcabsensi.data.repository.AppRepository
import com.example.nfcabsensi.databinding.FragmentAddEventBinding
import com.example.nfcabsensi.ui.viewmodel.EventViewModel
import com.example.nfcabsensi.ui.viewmodel.EventViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Date

import android.app.DatePickerDialog
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale

class AddEventFragment : Fragment() {

    private var _binding: FragmentAddEventBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: EventViewModel
    private var selectedDate: Long = Date().time

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddEventBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = AppDatabase.getDatabase(requireContext())
        val repository = AppRepository(db.studentDao(), db.eventDao(), db.studentEventDao())
        val factory = EventViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[EventViewModel::class.java]

        setupDatePicker()

        binding.btnSave.setOnClickListener {
            saveEvent()
        }

        lifecycleScope.launch {
            viewModel.messageFlow.collectLatest { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                if (message.contains("berhasil")) {
                    findNavController().popBackStack()
                }
            }
        }
    }

    private fun setupDatePicker() {
        // Set initial date text
        updateDateLabel()

        binding.etDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = selectedDate

            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                val newCalendar = Calendar.getInstance()
                newCalendar.set(selectedYear, selectedMonth, selectedDay)
                selectedDate = newCalendar.timeInMillis
                updateDateLabel()
            }, year, month, day).show()
        }
    }

    private fun updateDateLabel() {
        val format = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        binding.etDate.setText(format.format(Date(selectedDate)))
    }

    private fun saveEvent() {
        val title = binding.etTitle.text.toString().trim()
        val lecturer = binding.etLecturer.text.toString().trim()
        val leader = binding.etLeader.text.toString().trim()
        val leaderPhone = binding.etLeaderPhone.text.toString().trim()
        val studyProgram = binding.etStudyProgram.text.toString().trim()
        val classCode = binding.etClassCode.text.toString().trim()
        val priceOffline = binding.etPriceOffline.text.toString().toDoubleOrNull() ?: 0.0
        val priceOnline = binding.etPriceOnline.text.toString().toDoubleOrNull() ?: 0.0

        if (title.isEmpty()) {
            Toast.makeText(requireContext(), "Judul seminar wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }

        val event = Event(
            title = title,
            date = selectedDate,
            lecturerName = lecturer,
            classLeader = leader,
            classLeaderPhone = leaderPhone,
            studyProgram = studyProgram,
            classCode = classCode,
            priceOffline = priceOffline,
            priceOnline = priceOnline
        )
        viewModel.insert(event)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
