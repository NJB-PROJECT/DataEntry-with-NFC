package com.example.nfcabsensi.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.nfcabsensi.data.dao.EventDao
import com.example.nfcabsensi.data.dao.StudentDao
import com.example.nfcabsensi.data.dao.StudentEventDao
import com.example.nfcabsensi.data.entity.Event
import com.example.nfcabsensi.data.entity.Student
import com.example.nfcabsensi.data.entity.StudentEvent

@Database(entities = [Student::class, Event::class, StudentEvent::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao
    abstract fun eventDao(): EventDao
    abstract fun studentEventDao(): StudentEventDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nfc_absensi_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
