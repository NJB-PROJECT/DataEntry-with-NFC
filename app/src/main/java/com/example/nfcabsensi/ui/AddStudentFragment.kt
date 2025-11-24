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
import com.example.nfcabsensi.MainActivity
import com.example.nfcabsensi.data.database.AppDatabase
import com.example.nfcabsensi.data.entity.Student
import com.example.nfcabsensi.data.repository.AppRepository
import com.example.nfcabsensi.databinding.FragmentAddStudentBinding
import com.example.nfcabsensi.ui.viewmodel.StudentViewModel
import com.example.nfcabsensi.ui.viewmodel.StudentViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import android.graphics.Color
import java.util.UUID

class AddStudentFragment : Fragment() {

    private var _binding: FragmentAddStudentBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: StudentViewModel

    private var isScanning = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddStudentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = AppDatabase.getDatabase(requireContext())
        val repository = AppRepository(db.studentDao(), db.eventDao(), db.studentEventDao())
        val factory = StudentViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[StudentViewModel::class.java]

        setupNfcListener()
        updateScanStatus()

        binding.btnSave.setOnClickListener {
            saveStudent()
        }

        binding.btnToggleScan.setOnClickListener {
            isScanning = !isScanning
            updateScanStatus()
        }

        lifecycleScope.launch {
            viewModel.eventFlow.collectLatest { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                if (message.contains("berhasil")) {
                    findNavController().popBackStack()
                }
            }
        }
    }

    private fun updateScanStatus() {
        if (isScanning) {
            binding.btnToggleScan.text = "Stop Scan NFC"
            binding.tvScanStatus.text = "Tempelkan Kartu..."
            binding.tvScanStatus.setBackgroundColor(Color.YELLOW)
        } else {
            binding.btnToggleScan.text = "Mulai Scan NFC"
            binding.tvScanStatus.text = "Scan Non-Aktif"
            binding.tvScanStatus.setBackgroundColor(Color.LTGRAY)
        }
    }

    private fun setupNfcListener() {
        (activity as? MainActivity)?.onNfcTagDetected = { uid ->
            if (isScanning) {
                activity?.runOnUiThread {
                    binding.etUid.setText(uid)
                    binding.tvScanStatus.text = "SUKSES: $uid"
                    binding.tvScanStatus.setBackgroundColor(Color.GREEN)
                    Toast.makeText(requireContext(), "Kartu Terdeteksi: $uid", Toast.LENGTH_SHORT).show()

                    // Auto-stop scanning after success
                    isScanning = false
                    binding.btnToggleScan.text = "Mulai Scan NFC"
                }
            }
        }
    }

    private fun saveStudent() {
        val uid = binding.etUid.text.toString().trim()
        val name = binding.etName.text.toString().trim()
        val nim = binding.etNim.text.toString().trim()
        val studyProgram = binding.etStudyProgram.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()

        if (uid.isEmpty() || name.isEmpty() || nim.isEmpty()) {
            Toast.makeText(requireContext(), "UID, Nama, dan NIM wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }

        val student = Student(
            uid = uid,
            fullName = name,
            nim = nim,
            studyProgram = studyProgram,
            phoneNumber = phone
        )
        viewModel.insert(student)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as? MainActivity)?.onNfcTagDetected = null
        _binding = null
    }
}
