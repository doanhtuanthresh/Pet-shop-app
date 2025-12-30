package com.example.adopt_pet_app.ui.screen.chat

enum class MessageType {
    TEXT, IMAGE, VIDEO, AUDIO, DOCUMENT, LOCATION
}

data class Message(
    val id: String,
    val conversationId: String = "", // Thêm field này
    val content: String,
    val senderId: String,
    val timestamp: Long,
    val isRead: Boolean,
    val messageType: MessageType = MessageType.TEXT,
    val senderName: String? = null
)

data class Conversation(
    val id: String,
    val userId: String,
    val userName: String,
    val userAvatar: String?,
    val lastMessage: String,
    val timestamp: Long,
    val unreadCount: Int = 0,
    val isOnline: Boolean = false,
    val isPinned: Boolean = false
)

data class ChatUser(
    val id: String,
    val name: String,
    val avatar: String?,
    val isOnline: Boolean,
    val lastSeen: Long
)