package com.example.nfcabsensi.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.nfcabsensi.data.entity.StudentEvent
import com.example.nfcabsensi.data.entity.Student
import kotlinx.coroutines.flow.Flow

import androidx.room.Embedded

data class StudentWithAttendance(
    @Embedded
    val student: Student,
    @Embedded(prefix = "att_")
    val attendance: StudentEvent
)

@Dao
interface StudentEventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(studentEvent: StudentEvent)

    @Query("SELECT * FROM student_events WHERE eventId = :eventId AND studentId = :studentId LIMIT 1")
    suspend fun getAttendanceForStudent(eventId: Int, studentId: Int): StudentEvent?

    @Query("SELECT * FROM student_events WHERE eventId = :eventId")
    fun getAttendancesForEvent(eventId: Int): Flow<List<StudentEvent>>

    @Transaction
    @Query("""
        SELECT
            students.*,
            student_events.id AS att_id,
            student_events.studentId AS att_studentId,
            student_events.eventId AS att_eventId,
            student_events.billingCode AS att_billingCode,
            student_events.attendanceType AS att_attendanceType,
            student_events.nominal AS att_nominal,
            student_events.timestamp AS att_timestamp
        FROM students
        INNER JOIN student_events ON students.id = student_events.studentId
        WHERE student_events.eventId = :eventId
    """)
    fun getStudentsWithAttendanceForEvent(eventId: Int): Flow<List<StudentWithAttendance>>

    // For Excel Export (Non-Flow version)
    @Transaction
    @Query("""
        SELECT
            students.*,
            student_events.id AS att_id,
            student_events.studentId AS att_studentId,
            student_events.eventId AS att_eventId,
            student_events.billingCode AS att_billingCode,
            student_events.attendanceType AS att_attendanceType,
            student_events.nominal AS att_nominal,
            student_events.timestamp AS att_timestamp
        FROM students
        INNER JOIN student_events ON students.id = student_events.studentId
        WHERE student_events.eventId = :eventId
    """)
    suspend fun getStudentsWithAttendanceForEventSync(eventId: Int): List<StudentWithAttendance>
}
