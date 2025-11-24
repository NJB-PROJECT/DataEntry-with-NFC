package com.example.nfcabsensi.utils

import android.app.Activity
import android.nfc.NfcAdapter
import android.os.Bundle

interface NfcListener {
    fun onTagDetected(uid: String)
}

class NfcHelper(private val activity: Activity, private val listener: NfcListener) : NfcAdapter.ReaderCallback {

    private var nfcAdapter: NfcAdapter? = NfcAdapter.getDefaultAdapter(activity)

    fun enableReaderMode() {
        val options = Bundle()
        // Workaround for some devices: set presence check delay
        options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 250)

        nfcAdapter?.enableReaderMode(
            activity,
            this,
            NfcAdapter.FLAG_READER_NFC_A or
                    NfcAdapter.FLAG_READER_NFC_B or
                    NfcAdapter.FLAG_READER_NFC_F or
                    NfcAdapter.FLAG_READER_NFC_V or
                    NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS,
            options
        )
    }

    fun disableReaderMode() {
        nfcAdapter?.disableReaderMode(activity)
    }

    override fun onTagDiscovered(tag: android.nfc.Tag?) {
        tag?.let {
            // Convert UID bytes to Hex String
            val uid = toHexString(it.id)
            activity.runOnUiThread {
                listener.onTagDetected(uid)
            }
        }
    }

    private fun toHexString(bytes: ByteArray): String {
        val sb = StringBuilder()
        for (b in bytes) {
            sb.append(String.format("%02X:", b))
        }
        if (sb.isNotEmpty()) {
            sb.setLength(sb.length - 1) // Remove last colon
        }
        return sb.toString()
    }
}
