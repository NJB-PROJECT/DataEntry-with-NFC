package com.example.nfcabsensi.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nfcabsensi.MainActivity
import com.example.nfcabsensi.R
import com.example.nfcabsensi.data.database.AppDatabase
import com.example.nfcabsensi.data.entity.Student
import com.example.nfcabsensi.data.entity.StudentEvent
import com.example.nfcabsensi.data.repository.AppRepository
import com.example.nfcabsensi.databinding.FragmentEventDetailBinding
import com.example.nfcabsensi.ui.viewmodel.EventViewModel
import com.example.nfcabsensi.ui.viewmodel.EventViewModelFactory
import com.example.nfcabsensi.utils.ExcelExporter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID

class EventDetailFragment : Fragment() {

    private var _binding: FragmentEventDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: EventViewModel
    private var eventId: Int = 0

    // Flag to check if scanning is active (user clicked "Start Scan")
    private var isScanning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            eventId = it.getInt("eventId")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEventDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = AppDatabase.getDatabase(requireContext())
        val repository = AppRepository(db.studentDao(), db.eventDao(), db.studentEventDao())
        val factory = EventViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[EventViewModel::class.java]

        // Load Event Data
        viewModel.getEventById(eventId)

        // Setup List
        // Initialize adapter with dummy values initially
        val adapter = AttendeeAdapter(0.0, 0.0) { item ->
            // Edit Attendance Click
            showEditAttendanceDialog(item)
        }
        binding.rvAttendees.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAttendees.adapter = adapter

        lifecycleScope.launch {
            viewModel.currentEvent.collectLatest { event ->
                event?.let {
                    binding.tvDetailTitle.text = it.title
                    binding.tvDetailInfo.text = "Dosen: ${it.lecturerName}\nKetua: ${it.classLeader} (${it.classLeaderPhone})"

                    // Re-create adapter with correct prices when event is loaded
                    val newAdapter = AttendeeAdapter(it.priceOffline, it.priceOnline) { item ->
                         showEditAttendanceDialog(item)
                    }
                    binding.rvAttendees.adapter = newAdapter
                    // Re-submit current list if any
                    val currentList = adapter.currentList
                    if (currentList.isNotEmpty()) {
                        newAdapter.submitList(currentList)
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.getStudentsWithAttendance(eventId).collectLatest { list ->
                (binding.rvAttendees.adapter as? AttendeeAdapter)?.submitList(list)
                calculateSummary(list)
            }
        }

        lifecycleScope.launch {
            viewModel.messageFlow.collectLatest { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }

        lifecycleScope.launch {
            viewModel.scannedStudentFlow.collectLatest { student ->
                showAttendanceDialog(student)
            }
        }

        binding.btnScanNfc.setOnClickListener {
            toggleScanning()
        }

        binding.btnExportExcel.setOnClickListener {
            exportToExcel()
        }

        // Setup NFC listening
        (activity as? MainActivity)?.onNfcTagDetected = { uid ->
            if (isScanning) {
                viewModel.processNfcScan(eventId, uid)
            }
        }

        // Setup Long Click on Scan button to simulate NFC (for testing without device)
        binding.btnScanNfc.setOnLongClickListener {
             if (isScanning) {
                 // Simulate scanning a student. Since we don't know a valid UID,
                 // this might fail if we don't pick one from DB.
                 // For testing, let's just use a random UID and see if it hits the "Not found" error,
                 // or you can hardcode a UID you added in Student Management.
                 // Better yet, ask user for UID in a dialog for simulation.
                 showSimulationDialog()
             }
            true
        }
    }

    private fun toggleScanning() {
        isScanning = !isScanning
        if (isScanning) {
            binding.btnScanNfc.text = "Stop Scan NFC"
            binding.btnScanNfc.setBackgroundColor(requireContext().getColor(R.color.teal_700))
            Toast.makeText(requireContext(), "NFC Scan Aktif. Tempelkan Kartu.", Toast.LENGTH_SHORT).show()
        } else {
            binding.btnScanNfc.text = "Scan Absen (NFC)"
            binding.btnScanNfc.setBackgroundColor(requireContext().getColor(R.color.purple_500)) // Reset color roughly
        }
    }

    private fun showSimulationDialog() {
        val input = EditText(requireContext())
        input.hint = "Enter UID manually"
        AlertDialog.Builder(requireContext())
            .setTitle("Simulate NFC")
            .setView(input)
            .setPositiveButton("Scan") { _, _ ->
                viewModel.processNfcScan(eventId, input.text.toString())
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun calculateSummary(list: List<com.example.nfcabsensi.data.dao.StudentWithAttendance>) {
        val currentEvent = viewModel.currentEvent.value ?: return
        var totalCollected = 0.0
        var totalExpected = 0.0

        for (item in list) {
            totalCollected += item.attendance.nominal
            val expected = if (item.attendance.attendanceType.equals("Offline", true))
                currentEvent.priceOffline else currentEvent.priceOnline
            totalExpected += expected
        }

        val debt = totalExpected - totalCollected

        binding.tvTotalSummary.text = "Terkumpul: Rp ${totalCollected.toInt()} / Target: Rp ${totalExpected.toInt()}"
        binding.tvDebtSummary.text = "Kekurangan: Rp ${debt.toInt()}"
    }

    private fun showAttendanceDialog(student: Student) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_attendance_input, null)
        val etBilling = dialogView.findViewById<EditText>(R.id.et_billing_code)
        val rgType = dialogView.findViewById<RadioGroup>(R.id.rg_attendance_type)
        val tvStudentName = dialogView.findViewById<TextView>(R.id.tv_student_name_dialog)
        val etNominal = dialogView.findViewById<EditText>(R.id.et_nominal_paid)

        tvStudentName.text = student.fullName

        // Default check Offline
        rgType.check(R.id.rb_offline)

        // Update nominal hint when type changes
        val currentEvent = viewModel.currentEvent.value
        if (currentEvent != null) {
            etNominal.setText(currentEvent.priceOffline.toInt().toString())
        }

        rgType.setOnCheckedChangeListener { _, checkedId ->
             val price = if (checkedId == R.id.rb_offline) currentEvent?.priceOffline else currentEvent?.priceOnline
             etNominal.setText(price?.toInt().toString())
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Input Data Absensi")
            .setView(dialogView)
            .setPositiveButton("Simpan") { _, _ ->
                val billingCode = etBilling.text.toString().trim()
                val isOffline = rgType.checkedRadioButtonId == R.id.rb_offline
                val type = if (isOffline) "Offline" else "Online"
                val nominalInput = etNominal.text.toString().toDoubleOrNull()

                // Get price from current event
                val defaultNominal = if (isOffline) currentEvent?.priceOffline else currentEvent?.priceOnline
                val finalNominal = nominalInput ?: defaultNominal ?: 0.0

                val attendance = StudentEvent(
                    studentId = student.id,
                    eventId = eventId,
                    billingCode = billingCode,
                    attendanceType = type,
                    nominal = finalNominal,
                    timestamp = System.currentTimeMillis()
                )

                viewModel.submitAttendance(attendance)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showEditAttendanceDialog(item: com.example.nfcabsensi.data.dao.StudentWithAttendance) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_attendance_input, null)
        val etBilling = dialogView.findViewById<EditText>(R.id.et_billing_code)
        val rgType = dialogView.findViewById<RadioGroup>(R.id.rg_attendance_type)
        val tvStudentName = dialogView.findViewById<TextView>(R.id.tv_student_name_dialog)
        val etNominal = dialogView.findViewById<EditText>(R.id.et_nominal_paid)

        tvStudentName.text = "Edit: ${item.student.fullName}"
        etBilling.setText(item.attendance.billingCode)
        etNominal.setText(item.attendance.nominal.toInt().toString())

        if (item.attendance.attendanceType.equals("Online", true)) {
            rgType.check(R.id.rb_online)
        } else {
            rgType.check(R.id.rb_offline)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Absensi")
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                val billingCode = etBilling.text.toString().trim()
                val isOffline = rgType.checkedRadioButtonId == R.id.rb_offline
                val type = if (isOffline) "Offline" else "Online"
                val nominalInput = etNominal.text.toString().toDoubleOrNull() ?: 0.0

                val updatedAttendance = item.attendance.copy(
                    billingCode = billingCode,
                    attendanceType = type,
                    nominal = nominalInput
                )

                viewModel.submitAttendance(updatedAttendance)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun exportToExcel() {
        lifecycleScope.launch {
            val event = viewModel.currentEvent.value ?: return@launch
            val attendees = viewModel.getStudentsWithAttendanceSync(eventId)

            if (attendees.isEmpty()) {
                Toast.makeText(context, "Data peserta kosong", Toast.LENGTH_SHORT).show()
                return@launch
            }

            try {
                val exporter = ExcelExporter(requireContext())
                val filePath = exporter.exportEventData(event, attendees)

                if (filePath != null) {
                    Toast.makeText(context, "Excel tersimpan di: $filePath", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Gagal membuat file Excel", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as? MainActivity)?.onNfcTagDetected = null
        _binding = null
    }
}
