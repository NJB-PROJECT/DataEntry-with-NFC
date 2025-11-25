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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nfcabsensi.R
import com.example.nfcabsensi.data.database.AppDatabase
import com.example.nfcabsensi.data.repository.AppRepository
import com.example.nfcabsensi.databinding.FragmentStudentListBinding
import com.example.nfcabsensi.ui.viewmodel.StudentViewModel
import com.example.nfcabsensi.ui.viewmodel.StudentViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class StudentListFragment : Fragment() {

    private var _binding: FragmentStudentListBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: StudentViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentStudentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = AppDatabase.getDatabase(requireContext())
        val repository = AppRepository(db.studentDao(), db.eventDao(), db.studentEventDao())
        val factory = StudentViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[StudentViewModel::class.java]

        val adapter = StudentAdapter { student ->
            // On Item Click (Optional: Edit/Delete Dialog)
            showEditDeleteDialog(student)
        }
        binding.rvStudents.layoutManager = LinearLayoutManager(requireContext())
        binding.rvStudents.adapter = adapter

        lifecycleScope.launch {
            viewModel.allStudents.collectLatest { students ->
                adapter.submitList(students)
            }
        }

        lifecycleScope.launch {
            viewModel.eventFlow.collectLatest { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnAddStudent.setOnClickListener {
            findNavController().navigate(R.id.action_studentListFragment_to_addStudentFragment)
        }
    }

    private fun showEditDeleteDialog(student: com.example.nfcabsensi.data.entity.Student) {
        val options = arrayOf("Hapus Mahasiswa")
        android.app.AlertDialog.Builder(requireContext())
            .setTitle(student.fullName)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> confirmDelete(student)
                }
            }
            .show()
    }

    private fun confirmDelete(student: com.example.nfcabsensi.data.entity.Student) {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Hapus ${student.fullName}?")
            .setMessage("Data absensi terkait juga akan terhapus.")
            .setPositiveButton("Hapus") { _, _ ->
                viewModel.delete(student)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
