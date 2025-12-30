package com.example.adopt_pet_app.ui.screen.chat

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    userId: String,
    userName: String = "User",
    userAvatar: String? = null,
    navController: NavController,
    chatViewModel: ChatViewModel = viewModel()
) {
    val context = LocalContext.current
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    // Collect states from ViewModel - CHỈ ĐỊNH KIỂU RÕ RÀNG
    val messages: List<Message> by chatViewModel.messages.collectAsState()
    val currentChatUser: ChatUser? by chatViewModel.currentChatUser.collectAsState()
    val isOtherUserTyping: Boolean by chatViewModel.isOtherUserTyping.collectAsState()
    val isLoading: Boolean by chatViewModel.isLoading.collectAsState()
    val error: String? by chatViewModel.error.collectAsState()

    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    var showAttachmentMenu by remember { mutableStateOf(false) }

    // Khởi tạo conversation khi vào màn hình
    LaunchedEffect(userId) {
        chatViewModel.startConversation(userId)
    }

    // Auto scroll to bottom khi có tin nhắn mới
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Đánh dấu đã đọc khi vào màn hình
    LaunchedEffect(Unit) {
        chatViewModel.markAsRead()
    }

    // Cleanup khi rời khỏi màn hình
    DisposableEffect(Unit) {
        onDispose {
            chatViewModel.leaveConversation()
        }
    }

    // Show error nếu có
    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            chatViewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            ChatTopBar(
                userName = currentChatUser?.name ?: userName,
                userAvatar = currentChatUser?.avatar ?: userAvatar,
                isOnline = currentChatUser?.isOnline ?: false,
                isTyping = isOtherUserTyping,
                onBackClick = { navController.navigateUp() },
                onVideoCallClick = {
                    Toast.makeText(context, "Video call coming soon", Toast.LENGTH_SHORT).show()
                },
                onVoiceCallClick = {
                    Toast.makeText(context, "Voice call coming soon", Toast.LENGTH_SHORT).show()
                }
            )
        },
        bottomBar = {
            MessageInputBar(
                message = messageText,
                onMessageChange = { messageText = it },
                onSendClick = {
                    if (messageText.isNotBlank()) {
                        chatViewModel.sendMessage(messageText)
                        messageText = ""
                    }
                },
                onAttachmentClick = {
                    showAttachmentMenu = true
                    Toast.makeText(context, "Attachment coming soon", Toast.LENGTH_SHORT).show()
                },
                onVoiceMessageClick = {
                    Toast.makeText(context, "Voice message coming soon", Toast.LENGTH_SHORT).show()
                }
            )
        },
        containerColor = Color(0xFFF0F2F5)
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (isLoading && messages.isEmpty()) {
                // Show loading indicator
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Loading conversation...",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            } else if (messages.isEmpty() && !isLoading) {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No messages yet",
                        color = Color.Gray,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Start a conversation!",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    reverseLayout = false
                ) {
                    // Date separator (tùy chọn, có thể comment nếu chưa cần)
                    // item {
                    //     DateSeparator()
                    // }

                    // Messages
                    items(messages, key = { it.id }) { message ->
                        MessageBubble(
                            message = message,
                            isCurrentUser = message.senderId == currentUserId,
                            userName = currentChatUser?.name ?: userName,
                            userAvatar = currentChatUser?.avatar ?: userAvatar
                        )
                    }
                    // Bottom spacer
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}