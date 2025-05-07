package com.example.adopt_pet_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.example.adopt_pet_app.ui.navigation.AppNavGraph
import com.example.adopt_pet_app.theme.Adopt_pet_appTheme
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Adopt_pet_appTheme {
                val navController = rememberNavController()
                AppNavGraph(navController = navController)
            }
        }
    }
}

