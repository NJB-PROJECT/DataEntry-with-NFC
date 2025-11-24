package com.example.nfcabsensi.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "student_events",
    foreignKeys = [
        ForeignKey(
            entity = Student::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Event::class,
            parentColumns = ["id"],
            childColumns = ["eventId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class StudentEvent(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentId: Int,
    val eventId: Int,
    val billingCode: String,
    val attendanceType: String, // "Offline" or "Online"
    val nominal: Double,
    val timestamp: Long
)
