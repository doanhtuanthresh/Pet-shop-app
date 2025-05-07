//Homescreen.kt
package com.example.adopt_pet_app.ui.screen.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.adopt_pet_app.R
import com.example.adopt_pet_app.ui.component.BottomNavBar
import com.example.adopt_pet_app.ui.component.CategoryTabs
import com.example.adopt_pet_app.ui.component.PostItem
import com.example.adopt_pet_app.ui.component.SearchBar
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.adopt_pet_app.ui.navigation.Routes
import com.example.adopt_pet_app.ui.screen.post.PostViewModel



@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(navController: NavHostController) {
    val listState = rememberLazyListState()
    val previousOffset = remember { mutableStateOf(0) }
    val postViewModel: PostViewModel = viewModel()
    val posts = postViewModel.userPosts

    var selectedCategory by remember { mutableStateOf("All") }

    val filteredPosts = posts.filter {
        selectedCategory == "All" || it.type.equals(selectedCategory, ignoreCase = true)
    }

    LaunchedEffect(Unit) {
        postViewModel.loadAllPosts()
    }

    // Kiểm tra chiều cuộn
    val isScrollingUp by remember {
        derivedStateOf {
            listState.firstVisibleItemScrollOffset < previousOffset.value || listState.firstVisibleItemIndex == 0
        }
    }

    // Cập nhật vị trí cuộn
    LaunchedEffect(listState.firstVisibleItemScrollOffset) {
        previousOffset.value = listState.firstVisibleItemScrollOffset
    }

    Scaffold(
        bottomBar = { BottomNavBar() }
    ) { padding ->
        LazyColumn(
            state = listState,
            contentPadding = padding,
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                AnimatedVisibility(
                    visible = isScrollingUp,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),  // Từ trên xuống
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { -it })) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Image(
                            painter = painterResource(R.drawable.petshop_logo),
                            contentDescription = "Logo",
                            modifier = Modifier
                                .size(100.dp)
                                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        SearchBar(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        CategoryTabs(
                            categories = listOf("All", "Dog", "Cat", "Rabbit"),
                            onCategorySelected = { category ->
                                selectedCategory = category
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            stickyHeader {
                Column(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    Text(
                        text = "What's New",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp)
                    )
                    NewPostSection(navController = navController)
                }
            }

            items(filteredPosts) { post ->
                PostItem(
                    post = post,
                    onUserClick = { username ->
                        navController.navigate("profile/$username")
                    },
                    onPostClick = { postId ->
                        navController.navigate("postDetail/$postId")
                    }
                )
            }
        }
    }
}

@Composable
fun NewPostSection(navController: NavHostController) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.avatar1),
            contentDescription = "User Avatar",
            modifier = Modifier
                .size(40.dp)
                .background(Color.Gray, CircleShape)
        )

        Spacer(modifier = Modifier.width(8.dp))

        OutlinedTextField(
            value = "",
            onValueChange = {},
            placeholder = { Text("New post...") },
            modifier = Modifier
                .weight(1f)
                .clickable { navController.navigate(Routes.POST_CREATE) },
            shape = CircleShape,
            maxLines = 1,
            enabled = false
        )

        /*IconButton(onClick = { /* Đăng bài */ }) {
            Icon(
                painter = painterResource(id = R.drawable.sendbutton),
                contentDescription = "Send",
                tint = Color.Unspecified,
                modifier = Modifier.size(24.dp)
            )
        }*/
    }
}
