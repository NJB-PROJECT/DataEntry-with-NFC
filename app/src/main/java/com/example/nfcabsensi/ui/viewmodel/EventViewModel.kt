package com.example.nfcabsensi.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.nfcabsensi.data.dao.StudentWithAttendance
import com.example.nfcabsensi.data.entity.Event
import com.example.nfcabsensi.data.entity.StudentEvent
import com.example.nfcabsensi.data.repository.AppRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EventViewModel(private val repository: AppRepository) : ViewModel() {
    val allEvents: Flow<List<Event>> = repository.allEvents

    private val _messageFlow = MutableSharedFlow<String>()
    val messageFlow = _messageFlow.asSharedFlow()

    private val _currentEvent = MutableStateFlow<Event?>(null)
    val currentEvent = _currentEvent.asStateFlow()

    fun insert(event: Event) = viewModelScope.launch {
        try {
            repository.insertEvent(event)
            _messageFlow.emit("Seminar berhasil dibuat")
        } catch (e: Exception) {
            _messageFlow.emit("Gagal membuat seminar: ${e.message}")
        }
    }

    fun getEventById(id: Int) = viewModelScope.launch {
        _currentEvent.value = repository.getEventById(id)
    }

    fun getStudentsWithAttendance(eventId: Int): Flow<List<StudentWithAttendance>> {
        return repository.getStudentsWithAttendanceForEvent(eventId)
    }

    // NFC Attendance Logic
    fun processNfcScan(eventId: Int, uid: String) = viewModelScope.launch {
        // 1. Check if student exists
        val student = repository.getStudentByUid(uid)
        if (student == null) {
            _messageFlow.emit("UID tidak terdaftar. Silakan daftarkan mahasiswa.")
            return@launch
        }

        // 2. Check if already attended
        val existingAttendance = repository.getAttendanceForStudent(eventId, student.id)
        if (existingAttendance != null) {
            _messageFlow.emit("Mahasiswa ${student.fullName} sudah absen.")
            return@launch
        }

        // 3. Emit success signal to UI so it can prompt for details (Billing, Online/Offline)
        // We need a way to pass the student back to UI
        _scannedStudentFlow.emit(student)
    }

    private val _scannedStudentFlow = MutableSharedFlow<com.example.nfcabsensi.data.entity.Student>()
    val scannedStudentFlow = _scannedStudentFlow.asSharedFlow()

    fun submitAttendance(attendance: StudentEvent) = viewModelScope.launch {
        try {
            repository.insertAttendance(attendance)
            _messageFlow.emit("Absensi berhasil disimpan")
        } catch (e: Exception) {
            _messageFlow.emit("Gagal menyimpan absensi: ${e.message}")
        }
    }

    suspend fun getStudentsWithAttendanceSync(eventId: Int): List<StudentWithAttendance> {
        return repository.getStudentsWithAttendanceForEventSync(eventId)
    }
}

class EventViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EventViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EventViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
