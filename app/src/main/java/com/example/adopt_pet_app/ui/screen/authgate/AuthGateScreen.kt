package com.example.adopt_pet_app.ui.screen.authgate

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.example.adopt_pet_app.ui.navigation.Routes

@Composable
fun AuthGateScreen(navController: NavHostController) {

    LaunchedEffect(Unit) {
        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            navController.navigate(Routes.HOME) {
                popUpTo(Routes.AUTH_GATE) { inclusive = true }
            }
        } else {
            navController.navigate(Routes.ONBOARDING) {
                popUpTo(Routes.AUTH_GATE) { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
