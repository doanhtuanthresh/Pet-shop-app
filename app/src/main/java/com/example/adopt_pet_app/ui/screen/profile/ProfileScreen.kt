//ProfileScreen.kt
package com.example.adopt_pet_app.ui.screen.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.adopt_pet_app.R
import com.example.adopt_pet_app.ui.component.BottomNavBar
import com.example.adopt_pet_app.ui.component.PostItem
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

const val PROFILE = "profile/{userId}"
fun getProfileRoute(userId: String) = "profile/$userId"

@Composable
fun ProfileScreen(userId: String, navController: NavHostController) {
    val viewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory(userId))
    val tabs = listOf("Post", "About")
    var selectedTab by remember { mutableStateOf(0) }
    val userPosts = viewModel.userPosts
    val username = viewModel.username
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    val currentBackStackEntry = navController.currentBackStackEntryAsState().value
    val shouldReload = currentBackStackEntry?.savedStateHandle?.get<Boolean>("shouldReload") == true

    LaunchedEffect(shouldReload) {
        if (shouldReload) {
            viewModel.fetchUserData()     // Gọi lại hàm để lấy dữ liệu mới
            currentBackStackEntry?.savedStateHandle?.set("shouldReload", false)  // Đặt lại flag
        }
    }


    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(24.dp))
                Text("Menu", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium)

                Divider()

                NavigationDrawerItem(
                    label = { Text("Edit Profile") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("editProfile/$userId")
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    label = { Text("Sign Out") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        FirebaseAuth.getInstance().signOut()
                        navController.navigate("login") {
                            popUpTo(0) // Clear backstack
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(R.drawable.petshop_logo),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(48.dp)
                    .padding(top = 16.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Profile", style = MaterialTheme.typography.titleLarge)
                IconButton(onClick = {
                    scope.launch { drawerState.open() }
                }) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu")
                }
            }

            // Remainder UI (avatar, tab row, content, bottom nav) không thay đổi...
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(R.drawable.avatar1),
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color.Gray)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(username, style = MaterialTheme.typography.titleMedium)
                Text("Vung Tau, Vie", color = Color.Gray)
            }

            TabRow(selectedTabIndex = selectedTab, modifier = Modifier.fillMaxWidth()) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            if (selectedTab == 0) {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(userPosts) { post ->
                        PostItem(
                            post = post,
                            onUserClick = { username -> navController.navigate("profile/$username") },
                            onPostClick = { postId -> navController.navigate("postDetail/$postId") }
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No content in '${tabs[selectedTab]}' tab yet.")
                }
            }

            BottomNavBar(navController = navController, userId = currentUserId, selectedIndex = 3)
        }
    }
}
