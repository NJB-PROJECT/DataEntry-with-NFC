package com.example.nfcabsensi.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.nfcabsensi.data.entity.Student
import com.example.nfcabsensi.data.repository.AppRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class StudentViewModel(private val repository: AppRepository) : ViewModel() {
    val allStudents: Flow<List<Student>> = repository.allStudents

    // For signaling results (like "Student Added" or "Error")
    private val _eventFlow = MutableSharedFlow<String>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun insert(student: Student) = viewModelScope.launch {
        try {
            repository.insertStudent(student)
            _eventFlow.emit("Mahasiswa berhasil ditambahkan")
        } catch (e: Exception) {
            _eventFlow.emit("Gagal menambahkan mahasiswa: ${e.message}")
        }
    }
}

class StudentViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StudentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StudentViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
