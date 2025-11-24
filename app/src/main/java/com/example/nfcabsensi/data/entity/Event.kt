package com.example.nfcabsensi.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class Event(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val date: Long,
    val lecturerName: String,
    val classLeader: String,
    val classLeaderPhone: String,
    val studyProgram: String,
    val classCode: String,
    val priceOffline: Double,
    val priceOnline: Double
)
