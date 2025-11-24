package com.example.nfcabsensi.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(tableName = "students", indices = [Index(value = ["uid"], unique = true)])
data class Student(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val uid: String,
    val nim: String,
    val fullName: String,
    val studyProgram: String,
    val phoneNumber: String
)
