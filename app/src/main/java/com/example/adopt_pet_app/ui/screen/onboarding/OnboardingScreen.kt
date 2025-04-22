package com.example.adopt_pet_app.ui.screen.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.adopt_pet_app.R


@Composable
fun OnboardingScreen(
    onGetStartedClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally

    ) {
        // Ảnh logo
        Image(
            painter = painterResource(id = R.drawable.petshop_logo),
            contentDescription = "Pet Shop Logo",
            modifier = Modifier
                .size(200.dp)
                .padding(bottom = 16.dp)
        )

        // Tiêu đề
        Text(
            text = "PET SHOP",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.Black
        )

        // Mô tả
        Text(
            text = "Are you ready to get a new lovely friend?",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier.padding(vertical = 8.dp),
            textAlign = TextAlign.Center
        )

        // Nút Get Started
        Button(
            onClick = onGetStartedClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF914D) // Màu cam
            ),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .padding(top = 16.dp)
                .height(48.dp)
        ) {
            Text(
                text = "Get Started",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
