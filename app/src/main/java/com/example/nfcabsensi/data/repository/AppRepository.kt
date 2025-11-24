package com.example.nfcabsensi.data.repository

import com.example.nfcabsensi.data.dao.EventDao
import com.example.nfcabsensi.data.dao.StudentDao
import com.example.nfcabsensi.data.dao.StudentEventDao
import com.example.nfcabsensi.data.dao.StudentWithAttendance
import com.example.nfcabsensi.data.entity.Event
import com.example.nfcabsensi.data.entity.Student
import com.example.nfcabsensi.data.entity.StudentEvent
import kotlinx.coroutines.flow.Flow

class AppRepository(
    private val studentDao: StudentDao,
    private val eventDao: EventDao,
    private val studentEventDao: StudentEventDao
) {
    // Student
    val allStudents: Flow<List<Student>> = studentDao.getAllStudents()

    suspend fun insertStudent(student: Student) {
        studentDao.insertStudent(student)
    }

    suspend fun getStudentByUid(uid: String): Student? {
        return studentDao.getStudentByUid(uid)
    }

    // Event
    val allEvents: Flow<List<Event>> = eventDao.getAllEvents()

    suspend fun insertEvent(event: Event) {
        eventDao.insertEvent(event)
    }

    suspend fun getEventById(id: Int): Event? {
        return eventDao.getEventById(id)
    }

    suspend fun updateEvent(event: Event) {
        eventDao.updateEvent(event)
    }

    // Attendance
    suspend fun insertAttendance(attendance: StudentEvent) {
        studentEventDao.insertAttendance(attendance)
    }

    suspend fun getAttendanceForStudent(eventId: Int, studentId: Int): StudentEvent? {
        return studentEventDao.getAttendanceForStudent(eventId, studentId)
    }

    fun getStudentsWithAttendanceForEvent(eventId: Int): Flow<List<StudentWithAttendance>> {
        return studentEventDao.getStudentsWithAttendanceForEvent(eventId)
    }

    suspend fun getStudentsWithAttendanceForEventSync(eventId: Int): List<StudentWithAttendance> {
        return studentEventDao.getStudentsWithAttendanceForEventSync(eventId)
    }
}
