//BottomNavBar.kt
package com.example.adopt_pet_app.ui.component

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.painter.Painter
import androidx.navigation.NavHostController
import com.example.adopt_pet_app.R
import com.example.adopt_pet_app.ui.navigation.Routes

@Composable
fun BottomNavBar(
    navController: NavHostController,
    selectedIndex: Int = 0,
    userId: String,
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
                        modifier = Modifier.size(36.dp, 31.dp),
                        painter = item.icon,
                        contentDescription = item.label
                    )
                },
                selected = index == selectedIndex,
                onClick = {
                    onItemSelected(index)
                    when (index) {
                        0 -> navController.navigate(Routes.HOME)
                        3 -> navController.navigate(Routes.getProfileRoute(userId))
                    }
                }
            )
        }
    }
}


data class BottomNavItem(
    val label: String,
    val icon: Painter
)
