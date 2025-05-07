//BottomNavBar.kt
package com.example.adopt_pet_app.ui.component

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.painter.Painter
import com.example.adopt_pet_app.R

@Composable
fun BottomNavBar(
    selectedIndex: Int = 0,
    onItemSelected: (Int) -> Unit = {}
) {
    NavigationBar(
        tonalElevation = 4.dp,
        modifier = Modifier.height(75.dp)
    ) {
        val items = listOf(
            BottomNavItem("Home", painterResource(id = R.drawable.icon_grid)),
            BottomNavItem("Chat", painterResource(id = R.drawable.icon_chat)),
            BottomNavItem("Favorites", painterResource(id = R.drawable.icon_heart)),
            BottomNavItem("Profile", painterResource(id = R.drawable.icon_user))
        )

        items.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        modifier = Modifier.size(36.dp,31.dp),
                        painter = item.icon,
                        contentDescription = item.label
                    )
                },
                selected = index == selectedIndex,
                onClick = { onItemSelected(index) }
            )
        }
    }
}

data class BottomNavItem(
    val label: String,
    val icon: Painter
)
