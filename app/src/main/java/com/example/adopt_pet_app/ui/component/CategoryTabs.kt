package com.example.adopt_pet_app.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight

@Composable
fun CategoryTabs(
    categories: List<String> = listOf("All", "Dogs", "Cats", "Rabbits"),
    onCategorySelected: (String) -> Unit
) {
    var selectedCategory by remember { mutableStateOf(categories[0]) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(50.dp)
    ) {
        categories.forEach { category ->
            Text(
                text = category,
                fontSize = 18.sp,
                fontWeight = if (selectedCategory == category) FontWeight.Bold else FontWeight.Normal,
                color = if (selectedCategory == category) Color(0xFFFF6600) else Color.Black,
                modifier = Modifier
                    .clickable {
                        selectedCategory = category
                        onCategorySelected(category)
                    }
            )
        }
    }
}
