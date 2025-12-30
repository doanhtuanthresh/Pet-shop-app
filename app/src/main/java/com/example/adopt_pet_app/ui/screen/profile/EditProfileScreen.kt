package com.example.adopt_pet_app.ui.screen.profile

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun EditProfileScreen(
    navController: NavHostController
) {
    val db = remember { FirebaseFirestore.getInstance() }
    val currentUserId = remember {
        com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
    }

    if (currentUserId == null) {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
        return
    }

    var phoneNumber by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(currentUserId) {
        db.collection("users")
            .document(currentUserId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    phoneNumber = doc.getString("phoneNumber") ?: ""
                    location = doc.getString("location") ?: ""
                }
                isLoading = false
            }
            .addOnFailureListener {
                isLoading = false
            }
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text("Edit Profile", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Phone Number") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("Location") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (isSaving) return@Button
                isSaving = true

                db.collection("users")
                    .document(currentUserId)
                    .update(
                        mapOf(
                            "phoneNumber" to phoneNumber.trim(),
                            "location" to location.trim()
                        )
                    )
                    .addOnSuccessListener {
                        navController.popBackStack()
                    }
                    .addOnFailureListener {
                        isSaving = false
                    }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSaving
        ) {
            Text("Save")
        }
    }
}

