package com.example.adopt_pet_app.data.repository

import android.util.Log
import com.example.adopt_pet_app.ui.screen.chat.ChatUser
import com.example.adopt_pet_app.ui.screen.chat.Conversation
import com.example.adopt_pet_app.ui.screen.chat.Message
import com.example.adopt_pet_app.ui.screen.chat.MessageType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ChatRepository {
    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val conversationsRef = database.getReference("conversations")
    private val messagesRef = database.getReference("messages")
    private val usersRef = database.getReference("users")

    companion object {
        private const val TAG = "ChatRepository"
    }


    suspend fun getOrCreateConversation(otherUserId: String): String {
        val currentUserId = auth.currentUser?.uid ?: throw Exception("User not logged in")

        // Tạo conversation ID theo thứ tự alphabet để đảm bảo duy nhất
        val conversationId = if (currentUserId < otherUserId) {
            "${currentUserId}_${otherUserId}"
        } else {
            "${otherUserId}_${currentUserId}"
        }

        Log.d(TAG, "Getting or creating conversation: $conversationId")

        // Kiểm tra xem conversation đã tồn tại chưa
        val snapshot = conversationsRef.child(conversationId).get().await()

        if (!snapshot.exists()) {
            // Tạo mới conversation với ServerValue.TIMESTAMP
            val conversationData = mapOf(
                "id" to conversationId,
                "participants" to listOf(currentUserId, otherUserId),
                "createdAt" to ServerValue.TIMESTAMP,
                "lastMessage" to "",
                "lastMessageTime" to ServerValue.TIMESTAMP,
                "lastMessageSenderId" to ""
            )
            conversationsRef.child(conversationId).setValue(conversationData).await()
            Log.d(TAG, "Created new conversation: $conversationId")
        }

        return conversationId
    }


    suspend fun sendMessage(
        conversationId: String,
        content: String,
        messageType: MessageType = MessageType.TEXT
    ): String {
        val currentUserId = auth.currentUser?.uid ?: throw Exception("User not logged in")
        val messageId = UUID.randomUUID().toString()

        val message = mapOf(
            "id" to messageId,
            "conversationId" to conversationId,
            "senderId" to currentUserId,
            "content" to content,
            "messageType" to messageType.name,
            "timestamp" to ServerValue.TIMESTAMP, // Sử dụng server timestamp
            "isRead" to false
        )

        // Lưu tin nhắn
        messagesRef.child(conversationId).child(messageId).setValue(message).await()

        // Cập nhật last message trong conversation
        val conversationUpdate = mapOf(
            "lastMessage" to content,
            "lastMessageTime" to ServerValue.TIMESTAMP, // Sử dụng server timestamp
            "lastMessageSenderId" to currentUserId
        )
        conversationsRef.child(conversationId).updateChildren(conversationUpdate).await()

        Log.d(TAG, "Message sent: $messageId")
        return messageId
    }


    fun observeMessages(conversationId: String): Flow<List<Message>> = callbackFlow {
        Log.d(TAG, "Observing messages for conversation: $conversationId")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = mutableListOf<Message>()

                snapshot.children.forEach { messageSnapshot ->
                    try {
                        // Parse timestamp - xử lý cả Long và ServerValue.TIMESTAMP
                        val timestampValue = messageSnapshot.child("timestamp").value
                        val timestamp = when (timestampValue) {
                            is Long -> timestampValue
                            is Map<*, *> -> {
                                // ServerValue.TIMESTAMP chưa được chuyển đổi, dùng thời gian hiện tại tạm thời
                                System.currentTimeMillis()
                            }
                            else -> {
                                Log.w(TAG, "Unexpected timestamp type: ${timestampValue?.javaClass}")
                                System.currentTimeMillis()
                            }
                        }

                        val message = Message(
                            id = messageSnapshot.child("id").getValue(String::class.java) ?: "",
                            conversationId = conversationId,
                            content = messageSnapshot.child("content").getValue(String::class.java) ?: "",
                            senderId = messageSnapshot.child("senderId").getValue(String::class.java) ?: "",
                            timestamp = timestamp,
                            isRead = messageSnapshot.child("isRead").getValue(Boolean::class.java) ?: false,
                            messageType = try {
                                MessageType.valueOf(
                                    messageSnapshot.child("messageType").getValue(String::class.java) ?: "TEXT"
                                )
                            } catch (e: Exception) {
                                MessageType.TEXT
                            },
                            senderName = null // Sẽ được cập nhật sau
                        )
                        messages.add(message)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing message", e)
                    }
                }

                // Sắp xếp theo thời gian tăng dần (cũ -> mới)
                val sortedMessages = messages.sortedBy { it.timestamp }

                // Debug log để kiểm tra thứ tự
                if (sortedMessages.isNotEmpty()) {
                    Log.d(TAG, "Messages sorted: ${sortedMessages.map { it.timestamp }}")
                }

                trySend(sortedMessages)
                Log.d(TAG, "Received ${sortedMessages.size} messages")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error observing messages", error.toException())
                close(error.toException())
            }
        }

        messagesRef.child(conversationId).orderByChild("timestamp").addValueEventListener(listener)

        awaitClose {
            messagesRef.child(conversationId).removeEventListener(listener)
            Log.d(TAG, "Stopped observing messages")
        }
    }


    fun observeConversations(): Flow<List<Conversation>> = callbackFlow {
        val currentUserId = auth.currentUser?.uid ?: throw Exception("User not logged in")

        Log.d(TAG, "Observing conversations for user: $currentUserId")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val conversations = mutableListOf<Conversation>()

                snapshot.children.forEach { conversationSnapshot ->
                    try {
                        val participants = conversationSnapshot.child("participants")
                            .getValue(object : GenericTypeIndicator<List<String>>() {})

                        if (participants?.contains(currentUserId) == true) {
                            val otherUserId = participants.firstOrNull { it != currentUserId } ?: ""

                            // Parse timestamp từ conversation
                            val timestampValue = conversationSnapshot.child("lastMessageTime").value
                            val timestamp = when (timestampValue) {
                                is Long -> timestampValue
                                is Map<*, *> -> System.currentTimeMillis()
                                else -> 0L
                            }

                            val conversation = Conversation(
                                id = conversationSnapshot.child("id").getValue(String::class.java) ?: "",
                                userId = otherUserId,
                                userName = "Loading...",
                                userAvatar = null,
                                lastMessage = conversationSnapshot.child("lastMessage").getValue(String::class.java) ?: "",
                                timestamp = timestamp,
                                unreadCount = 0,
                                isOnline = false,
                                isPinned = false
                            )
                            conversations.add(conversation)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing conversation", e)
                    }
                }

                // Sắp xếp theo thời gian giảm dần (mới nhất đầu tiên)
                val sortedConversations = conversations.sortedByDescending { it.timestamp }
                trySend(sortedConversations)
                Log.d(TAG, "Received ${sortedConversations.size} conversations")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error observing conversations", error.toException())
                close(error.toException())
            }
        }

        conversationsRef.orderByChild("lastMessageTime").addValueEventListener(listener)

        awaitClose {
            conversationsRef.removeEventListener(listener)
            Log.d(TAG, "Stopped observing conversations")
        }
    }


    suspend fun markMessagesAsRead(conversationId: String) {
        val currentUserId = auth.currentUser?.uid ?: return

        try {
            val snapshot = messagesRef.child(conversationId).get().await()

            val updates = mutableMapOf<String, Any>()
            snapshot.children.forEach { messageSnapshot ->
                val senderId = messageSnapshot.child("senderId").getValue(String::class.java)
                val isRead = messageSnapshot.child("isRead").getValue(Boolean::class.java) ?: false

                if (senderId != currentUserId && !isRead) {
                    val messageId = messageSnapshot.key ?: return@forEach
                    updates["$messageId/isRead"] = true
                }
            }

            if (updates.isNotEmpty()) {
                messagesRef.child(conversationId).updateChildren(updates).await()
                Log.d(TAG, "Marked ${updates.size} messages as read")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error marking messages as read", e)
        }
    }


    suspend fun getUnreadCount(conversationId: String): Int {
        val currentUserId = auth.currentUser?.uid ?: return 0

        return try {
            val snapshot = messagesRef.child(conversationId).get().await()
            var count = 0

            snapshot.children.forEach { messageSnapshot ->
                val senderId = messageSnapshot.child("senderId").getValue(String::class.java)
                val isRead = messageSnapshot.child("isRead").getValue(Boolean::class.java) ?: false

                if (senderId != currentUserId && !isRead) {
                    count++
                }
            }

            count
        } catch (e: Exception) {
            Log.e(TAG, "Error getting unread count", e)
            0
        }
    }

    suspend fun getUserInfo(userId: String): ChatUser? {
        return try {
            val snapshot = usersRef.child(userId).get().await()

            if (snapshot.exists()) {
                ChatUser(
                    id = userId,
                    name = snapshot.child("name").getValue(String::class.java) ?: "Unknown User",
                    avatar = snapshot.child("avatar").getValue(String::class.java),
                    isOnline = snapshot.child("isOnline").getValue(Boolean::class.java) ?: false,
                    lastSeen = snapshot.child("lastSeen").getValue(Long::class.java) ?: 0L
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user info", e)
            null
        }
    }


    fun observeUserInfo(userId: String): Flow<ChatUser?> = callbackFlow {
        Log.d(TAG, "Observing user info: $userId")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    if (snapshot.exists()) {
                        val user = ChatUser(
                            id = userId,
                            name = snapshot.child("name").getValue(String::class.java) ?: "Unknown User",
                            avatar = snapshot.child("avatar").getValue(String::class.java),
                            isOnline = snapshot.child("isOnline").getValue(Boolean::class.java) ?: false,
                            lastSeen = snapshot.child("lastSeen").getValue(Long::class.java) ?: 0L
                        )
                        trySend(user)
                    } else {
                        trySend(null)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing user info", e)
                    trySend(null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error observing user info", error.toException())
                close(error.toException())
            }
        }

        usersRef.child(userId).addValueEventListener(listener)

        awaitClose {
            usersRef.child(userId).removeEventListener(listener)
        }
    }


    suspend fun updateOnlineStatus(isOnline: Boolean) {
        val currentUserId = auth.currentUser?.uid ?: return

        try {
            val updates = mapOf(
                "isOnline" to isOnline,
                "lastSeen" to ServerValue.TIMESTAMP
            )

            usersRef.child(currentUserId).updateChildren(updates).await()
            Log.d(TAG, "Updated online status: $isOnline")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating online status", e)
        }
    }


    suspend fun deleteConversation(conversationId: String) {
        try {
            conversationsRef.child(conversationId).removeValue().await()
            messagesRef.child(conversationId).removeValue().await()
            Log.d(TAG, "Deleted conversation: $conversationId")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting conversation", e)
        }
    }


    suspend fun deleteMessage(conversationId: String, messageId: String) {
        try {
            messagesRef.child(conversationId).child(messageId).removeValue().await()

            // Cập nhật last message nếu cần
            val lastMessageSnapshot = messagesRef.child(conversationId)
                .orderByChild("timestamp")
                .limitToLast(1)
                .get()
                .await()

            if (lastMessageSnapshot.exists()) {
                lastMessageSnapshot.children.firstOrNull()?.let { lastMsg ->
                    val lastMessageContent = lastMsg.child("content").getValue(String::class.java) ?: ""
                    val lastMessageTime = lastMsg.child("timestamp").getValue(Long::class.java) ?: 0L
                    val lastMessageSenderId = lastMsg.child("senderId").getValue(String::class.java) ?: ""

                    val conversationUpdate = mapOf(
                        "lastMessage" to lastMessageContent,
                        "lastMessageTime" to lastMessageTime,
                        "lastMessageSenderId" to lastMessageSenderId
                    )
                    conversationsRef.child(conversationId).updateChildren(conversationUpdate).await()
                }
            } else {
                // Không còn tin nhắn nào
                val conversationUpdate = mapOf(
                    "lastMessage" to "",
                    "lastMessageTime" to 0L,
                    "lastMessageSenderId" to ""
                )
                conversationsRef.child(conversationId).updateChildren(conversationUpdate).await()
            }

            Log.d(TAG, "Deleted message: $messageId")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting message", e)
        }
    }


    fun setupPresence(userId: String) {
        val userStatusRef = usersRef.child(userId)

        // Khi user disconnect
        userStatusRef.onDisconnect().updateChildren(
            mapOf(
                "isOnline" to false,
                "lastSeen" to ServerValue.TIMESTAMP
            )
        )

        // Set online
        userStatusRef.updateChildren(
            mapOf(
                "isOnline" to true,
                "lastSeen" to ServerValue.TIMESTAMP
            )
        )

        Log.d(TAG, "Setup presence for user: $userId")
    }
}