package com.example.adopt_pet_app.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun InfoBadge(
    text: String,
    modifier: Modifier = Modifier  // thêm dòng này
) {
    Box(
        modifier = modifier
            .background(Color(0xFFFFA000), shape = RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text = text, color = Color.White)
    }
}
