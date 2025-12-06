package com.example.adopt_pet_app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.adopt_pet_app.data.repository.ChatRepository
import com.example.adopt_pet_app.data.repository.UserRepository
import com.example.adopt_pet_app.ui.navigation.AppNavGraph
import com.example.adopt_pet_app.theme.Adopt_pet_appTheme
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val userRepository = UserRepository()
    private val chatRepository = ChatRepository()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!arePermissionsGranted()) {
            ActivityCompat.requestPermissions(this, CAMERA_PERMISSIONS, REQUEST_CAMERA_PERMISSION)
        }

        setupUserPresence()

        setContent {
            Adopt_pet_appTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    AppNavGraph(navController = navController)
                }
            }
        }
    }


    private fun setupUserPresence() {
        auth.currentUser?.let { user ->
            lifecycleScope.launch {
                try {
                    userRepository.syncUserFromFirestore(user.uid)

                    chatRepository.setupPresence(user.uid)

                    userRepository.updateOnlineStatus(user.uid, true)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }


    fun arePermissionsGranted(): Boolean {
        return CAMERA_PERMISSIONS.all { permission ->
            ContextCompat.checkSelfPermission(
                this,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onResume() {
        super.onResume()
        auth.currentUser?.uid?.let { userId ->
            lifecycleScope.launch {
                try {
                    userRepository.updateOnlineStatus(userId, true)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        auth.currentUser?.uid?.let { userId ->
            lifecycleScope.launch {
                try {
                    userRepository.clearPresence(userId)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    companion object {
        private const val REQUEST_CAMERA_PERMISSION = 100

        private val CAMERA_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
    }
}