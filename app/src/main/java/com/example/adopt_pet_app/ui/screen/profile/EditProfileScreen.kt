package com.example.adopt_pet_app.ui.screen.profile

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun EditProfileScreen(userId: String, navController: NavHostController) {
    val location = remember { mutableStateOf("") }
    val phoneNumber = remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "Edit Profile", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = phoneNumber.value,
            onValueChange = { phoneNumber.value = it },
            label = { Text("Phone Number") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = location.value,
            onValueChange = { location.value = it },
            label = { Text("Location") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val db = FirebaseFirestore.getInstance()
                db.collection("users").document(userId)
                    .update(
                        mapOf(
                            "phoneNumber" to phoneNumber.value,
                            "location" to location.value
                        )
                    )
                    .addOnSuccessListener {
                        Log.d("EditProfile", "Successfully updated")

                        // Truyền cờ reload về màn trước
                        navController.previousBackStackEntry?.savedStateHandle?.set("shouldReload", true)

                        navController.popBackStack()
                    }
                    .addOnFailureListener { e ->
                        Log.e("EditProfile", "Error updating profile", e)
                    }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save")
        }

    }
}
