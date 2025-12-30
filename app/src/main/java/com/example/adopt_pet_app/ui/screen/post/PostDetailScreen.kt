package com.example.adopt_pet_app.ui.screen.post

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.adopt_pet_app.data.model.Post
import com.example.adopt_pet_app.ui.component.FullScreenImageViewer
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalPagerApi::class)
@Composable
fun PostDetailScreen(
    postId: String,
    postViewModel: PostViewModel = viewModel(),
    onNavigateToEdit: (String) -> Unit = {},
    onNavigateToChat: (String) -> Unit = {}, // Chỉ nhận userId để navigate đến chat
    onNavigateBack: () -> Unit = {}
) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    var post by remember { mutableStateOf<Post?>(null) }
    var showFullScreen by remember { mutableStateOf(false) }
    var startPage by remember { mutableStateOf(0) }
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }

    // Reload post data
    fun reloadPost() {
        postViewModel.getPostById(postId) {
            post = it
        }
    }

    LaunchedEffect(postId) {
        reloadPost()
    }

    post?.let { postData ->
        val pagerState = rememberPagerState()
        val isOwner = postData.userId == currentUserId
        val isAdopted = postData.adopted

        // Hàm xử lý toggle adopted status
        fun handleToggleAdopted() {
            isProcessing = true
            if (isAdopted) {
                // Bỏ đánh dấu adopted
                postViewModel.unmarkAsAdopted(
                    postId = postId,
                    onSuccess = {
                        isProcessing = false
                        reloadPost()
                    },
                    onError = {
                        isProcessing = false
                    }
                )
            } else {
                // Đánh dấu adopted
                currentUserId?.let { userId ->
                    postViewModel.markAsAdopted(
                        postId = postId,
                        adoptedBy = userId,
                        onSuccess = {
                            isProcessing = false
                            reloadPost()
                        },
                        onError = {
                            isProcessing = false
                        }
                    )
                }
            }
        }

        Column(modifier = Modifier.fillMaxSize()) {
            // Image gallery with menu button
            if (postData.imageUrls.isNotEmpty()) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    HorizontalPager(
                        count = postData.imageUrls.size,
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    ) { page ->
                        Box {
                            Image(
                                painter = rememberAsyncImagePainter(postData.imageUrls[page]),
                                contentDescription = "Pet image ${page + 1}",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable {
                                        startPage = page
                                        showFullScreen = true
                                    },
                                contentScale = ContentScale.Crop
                            )

                            // Overlay nếu đã được nhận nuôi
                            if (isAdopted) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.4f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = Color(0xFF4CAF50),
                                        modifier = Modifier.padding(16.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Adopted",
                                                tint = Color.White
                                            )
                                            Text(
                                                "ADOPTED",
                                                color = Color.White,
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Dots indicator
                    if (postData.imageUrls.size > 1) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            repeat(postData.imageUrls.size) { index ->
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (pagerState.currentPage == index) Color.White
                                            else Color.White.copy(alpha = 0.5f)
                                        )
                                )
                            }
                        }
                    }

                    // Menu button cho chủ bài đăng
                    if (isOwner) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(40.dp)
                        ) {
                            IconButton(
                                onClick = { showMenu = true },
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        color = Color.Black.copy(alpha = 0.5f),
                                        shape = CircleShape
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Menu",
                                    tint = Color.White
                                )
                            }

                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit"
                                            )
                                            Text("Edit")
                                        }
                                    },
                                    onClick = {
                                        showMenu = false
                                        onNavigateToEdit(postId)
                                    }
                                )

                                // Tùy chọn đánh dấu/bỏ đánh dấu adopted
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isAdopted) Icons.Default.Close else Icons.Default.Check,
                                                contentDescription = if (isAdopted) "Mark as Available" else "Mark as Adopted",
                                                tint = if (isAdopted) Color(0xFFF57C00) else Color(0xFF4CAF50)
                                            )
                                            Text(
                                                if (isAdopted) "Mark as Available" else "Mark as Adopted",
                                                color = if (isAdopted) Color(0xFFF57C00) else Color(0xFF4CAF50)
                                            )
                                        }
                                    },
                                    onClick = {
                                        showMenu = false
                                        handleToggleAdopted()
                                    }
                                )

                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                tint = Color.Red
                                            )
                                            Text("Delete", color = Color.Red)
                                        }
                                    },
                                    onClick = {
                                        showMenu = false
                                        showDeleteDialog = true
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Post info
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(postData.petName, fontSize = 34.sp, fontWeight = FontWeight.Bold)
                Text(postData.type, color = Color.Gray)

                Spacer(modifier = Modifier.height(16.dp))

                Row {
                    InfoBadge(text = "♂ ${postData.gender}")
                    Spacer(modifier = Modifier.width(10.dp))
                    InfoBadge(text = postData.age)
                    if (isAdopted) {
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF4CAF50))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(text = "Adopted", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("Location", fontWeight = FontWeight.SemiBold)
                Text(postData.location)

                Spacer(modifier = Modifier.height(12.dp))

                Text("Description", fontWeight = FontWeight.SemiBold)
                Text(postData.description)

                Spacer(modifier = Modifier.height(24.dp))

                // CHỈ HIỂN THỊ NÚT CHO NGƯỜI DÙNG THƯỜNG
                if (!isOwner) {
                    Button(
                        onClick = {
                            if (!isAdopted) {
                                // Chuyển thẳng đến chat với chủ bài đăng
                                onNavigateToChat(postData.userId)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isAdopted,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isAdopted) Color.Gray else  MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isAdopted) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null
                                )
                                Text("Adopted")
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Message,
                                    contentDescription = null
                                )
                                Text("Adopt Now")
                            }
                        }
                    }
                }
            }
        }

        // Full screen image viewer
        if (showFullScreen) {
            FullScreenImageViewer(
                imageUris = postData.imageUrls.map { Uri.parse(it) },
                startPage = startPage,
                onClose = { showFullScreen = false }
            )
        }

        // Delete confirmation dialog
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Post") },
                text = { Text("Are you sure you want to delete this post?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                            postViewModel.deletePost(
                                postId = postId,
                                onSuccess = { onNavigateBack() },
                                onError = {}
                            )
                        }
                    ) {
                        Text("Delete", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    } ?: run {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun InfoBadge(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF9A825))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text = text, color = Color.White, fontSize = 12.sp)
    }
}