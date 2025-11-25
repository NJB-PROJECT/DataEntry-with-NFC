package com.example.nfcabsensi

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.nfcabsensi.databinding.ActivityMainBinding
import com.example.nfcabsensi.utils.NfcHelper
import com.example.nfcabsensi.utils.NfcListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity(), NfcListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private var nfcHelper: NfcHelper? = null

    // Helper to pass NFC events to fragments
    var onNfcTagDetected: ((String) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check for updates before setting content
        checkAppVersion()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        setupActionBarWithNavController(navController)

        nfcHelper = NfcHelper(this, this)
    }

    private fun checkAppVersion() {
        try {
            // Explicitly use the database URL for stability, although google-services.json should handle it.
            // Using the URL provided by the user: https://apk-seminar-default-rtdb.asia-southeast1.firebasedatabase.app/
            val database = FirebaseDatabase.getInstance("https://apk-seminar-default-rtdb.asia-southeast1.firebasedatabase.app/")
            val myRef = database.getReference("latest_version_code")

            myRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    try {
                        // Handle both Long and Int types safely
                        val latestVersionCode = dataSnapshot.getValue(Long::class.java)?.toInt() ?: 0
                        val currentVersionCode = packageManager.getPackageInfo(packageName, 0).versionCode

                        Log.d("ForceUpdate", "Remote: $latestVersionCode, Local: $currentVersionCode")

                        if (latestVersionCode > currentVersionCode) {
                            // Force Update
                            val intent = Intent(this@MainActivity, ForceUpdateActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        }
                    } catch (e: Exception) {
                        Log.e("ForceUpdate", "Error parsing version: ${e.message}")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w("MainActivity", "Failed to read version value.", error.toException())
                    Toast.makeText(this@MainActivity, "Gagal cek update: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } catch (e: Exception) {
            Log.e("MainActivity", "Firebase init failed: ${e.message}")
            Toast.makeText(this, "Error Firebase: ${e.message}", Toast.LENGTH_LONG).show()
        }
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
