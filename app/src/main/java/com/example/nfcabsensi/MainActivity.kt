package com.example.nfcabsensi

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.nfcabsensi.databinding.ActivityMainBinding
import com.example.nfcabsensi.utils.NfcHelper
import com.example.nfcabsensi.utils.NfcListener

class MainActivity : AppCompatActivity(), NfcListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private var nfcHelper: NfcHelper? = null

    // Helper to pass NFC events to fragments
    var onNfcTagDetected: ((String) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        setupActionBarWithNavController(navController)

        nfcHelper = NfcHelper(this, this)
    }

    override fun onResume() {
        super.onResume()
        nfcHelper?.enableReaderMode()
    }

    override fun onPause() {
        super.onPause()
        nfcHelper?.disableReaderMode()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onTagDetected(uid: String) {
        // Pass the UID to the active fragment if it's listening
        if (onNfcTagDetected != null) {
            onNfcTagDetected?.invoke(uid)
        } else {
            // Optional: Show toast if no one is listening
            // runOnUiThread { Toast.makeText(this, "NFC Detected: $uid", Toast.LENGTH_SHORT).show() }
        }
    }
}
