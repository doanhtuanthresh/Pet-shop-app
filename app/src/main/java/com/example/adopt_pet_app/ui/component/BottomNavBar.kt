//BottomNavBar.kt
package com.example.adopt_pet_app.ui.component

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.ui.graphics.Color
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
        modifier = Modifier.height(50.dp)
    ) {
        val items = listOf(
            BottomNavItem("Home", painterResource(id = R.drawable.icon_grid)),
            BottomNavItem("Chat", painterResource(id = R.drawable.icon_chat)),
            BottomNavItem("Camera", painterResource(id = R.drawable.camera1)),
            //BottomNavItem("Favorites", painterResource(id = R.drawable.icon_heart)),
            BottomNavItem("Profile", painterResource(id = R.drawable.icon_user))
        )

        items.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        modifier = Modifier.size(36.dp, 31.dp),
                        painter = item.icon,
                        contentDescription = item.label,
                        tint = Color.Unspecified
                    )
                },
                selected = index == selectedIndex,
                onClick = {
                    onItemSelected(index)
                    when (index) {
                        0 -> navController.navigate(Routes.HOME)
                        1 -> navController.navigate(Routes.MESSENGER)
                        2 -> navController.navigate(Routes.CAMERA)
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
