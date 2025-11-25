package com.example.nfcabsensi.utils

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.nfcabsensi.data.dao.StudentWithAttendance
import com.example.nfcabsensi.data.entity.Event
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExcelExporter(private val context: Context) {

    fun exportEventData(event: Event, attendees: List<StudentWithAttendance>): String? {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Absensi")

        // Create Header Style
        val headerFont = workbook.createFont().apply {
            bold = true
        }
        val headerStyle = workbook.createCellStyle().apply {
            setFont(headerFont)
        }

        // --- Event Info Header ---
        var rowIndex = 0
        val infoRows = listOf(
            "Judul Seminar" to event.title,
            "Tanggal" to formatDate(event.date),
            "Dosen" to event.lecturerName,
            "Ketua Kelas" to "${event.classLeader} (${event.classLeaderPhone})",
            "Kode Kelas" to event.classCode
        )

        for ((label, value) in infoRows) {
            val row = sheet.createRow(rowIndex++)
            row.createCell(0).setCellValue(label)
            row.createCell(1).setCellValue(value)
        }

        // Spacer
        rowIndex++

        // --- Table Header ---
        val headers = listOf(
            "No", "NIM", "Nama Lengkap", "Kode Event",
            "Kode Tagihan", "Offline/Online", "Nominal",
            "Program Studi", "Nomor HP"
        )

        val headerRow = sheet.createRow(rowIndex++)
        headers.forEachIndexed { index, title ->
            val cell = headerRow.createCell(index)
            cell.setCellValue(title)
            cell.cellStyle = headerStyle
        }

        // --- Data Rows ---
        attendees.forEachIndexed { index, item ->
            val row = sheet.createRow(rowIndex++)
            val s = item.student
            val a = item.attendance

            row.createCell(0).setCellValue((index + 1).toDouble())
            row.createCell(1).setCellValue(s.nim)
            row.createCell(2).setCellValue(s.fullName)
            row.createCell(3).setCellValue(event.classCode)
            row.createCell(4).setCellValue(a.billingCode)
            row.createCell(5).setCellValue(a.attendanceType)
            row.createCell(6).setCellValue(a.nominal)
            row.createCell(7).setCellValue(s.studyProgram)
            row.createCell(8).setCellValue(s.phoneNumber)
        }

        // Auto size columns causes crash on Android due to missing AWT fonts
        // Set manual width (approximate characters width * 256)
        sheet.setColumnWidth(0, 5 * 256) // No
        sheet.setColumnWidth(1, 15 * 256) // NIM
        sheet.setColumnWidth(2, 25 * 256) // Nama
        sheet.setColumnWidth(3, 15 * 256) // Kode Event
        sheet.setColumnWidth(4, 15 * 256) // Kode Tagihan
        sheet.setColumnWidth(5, 12 * 256) // Offline/Online
        sheet.setColumnWidth(6, 15 * 256) // Nominal
        sheet.setColumnWidth(7, 20 * 256) // Prodi
        sheet.setColumnWidth(8, 15 * 256) // HP

        // Save File
        return saveFile(workbook, event.title)
    }

    private fun formatDate(timestamp: Long): String {
        return SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date(timestamp))
    }

    private fun saveFile(workbook: XSSFWorkbook, eventTitle: String): String? {
        // Sanitize filename
        val safeTitle = eventTitle.replace("[^a-zA-Z0-9.-]".toRegex(), "_")
        val fileName = "Absensi_${safeTitle}_${System.currentTimeMillis()}.xlsx"

        var outputStream: OutputStream? = null
        var resultPath: String? = null

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Use MediaStore for Android 10+
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    outputStream = resolver.openOutputStream(uri)
                    resultPath = "Downloads/$fileName"
                }
            } else {
                // Legacy way for Android 9 and below
                val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!path.exists()) path.mkdirs()
                val file = File(path, fileName)
                outputStream = FileOutputStream(file)
                resultPath = file.absolutePath
            }

            if (outputStream != null) {
                workbook.write(outputStream)
                outputStream.close()
                workbook.close()
                return resultPath
            } else {
                return null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}
