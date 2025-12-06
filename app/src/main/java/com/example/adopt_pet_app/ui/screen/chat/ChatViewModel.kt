package com.example.adopt_pet_app.ui.screen.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.adopt_pet_app.data.repository.ChatRepository
import com.example.adopt_pet_app.ui.screen.chat.ChatUser
import com.example.adopt_pet_app.ui.screen.chat.Conversation
import com.example.adopt_pet_app.ui.screen.chat.Message
import com.example.adopt_pet_app.ui.screen.chat.MessageType
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {
    private val repository = ChatRepository()

    companion object {
        private const val TAG = "ChatViewModel"
    }

    // State cho danh sách conversations
    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()

    // State cho tin nhắn trong conversation hiện tại
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    // State cho thông tin user đang chat
    private val _currentChatUser = MutableStateFlow<ChatUser?>(null)
    val currentChatUser: StateFlow<ChatUser?> = _currentChatUser.asStateFlow()

    // State cho typing indicator
    private val _isOtherUserTyping = MutableStateFlow(false)
    val isOtherUserTyping: StateFlow<Boolean> = _isOtherUserTyping.asStateFlow()

    // State cho loading
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // State cho error
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Current conversation ID
    private var currentConversationId: String? = null

    init {
        Log.d(TAG, "ChatViewModel initialized")
        observeConversations()
        updateOnlineStatus(true)
    }

    /**
     * Lắng nghe danh sách conversations
     */
    private fun observeConversations() {
        viewModelScope.launch {
            try {
                repository.observeConversations().collect { conversationList ->
                    Log.d(TAG, "Received ${conversationList.size} conversations")

                    // Lấy thông tin chi tiết cho mỗi conversation
                    val updatedConversations = conversationList.map { conversation ->
                        val userInfo = repository.getUserInfo(conversation.userId)
                        val unreadCount = repository.getUnreadCount(conversation.id)

                        conversation.copy(
                            userName = userInfo?.name ?: "Unknown User",
                            userAvatar = userInfo?.avatar,
                            isOnline = userInfo?.isOnline ?: false,
                            unreadCount = unreadCount
                        )
                    }

                    _conversations.value = updatedConversations
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error observing conversations", e)
                _error.value = e.message
            }
        }
    }

    /**
     * Khởi tạo hoặc mở conversation với user
     */
    fun startConversation(otherUserId: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Starting conversation with user: $otherUserId")
                _isLoading.value = true

                // Tạo hoặc lấy conversation ID
                val conversationId = repository.getOrCreateConversation(otherUserId)
                currentConversationId = conversationId

                Log.d(TAG, "Conversation ID: $conversationId")

                // Lấy thông tin user
                val userInfo = repository.getUserInfo(otherUserId)
                _currentChatUser.value = userInfo

                Log.d(TAG, "User info loaded: ${userInfo?.name}")

                // Lắng nghe tin nhắn
                observeMessages(conversationId)

                // Đánh dấu đã đọc
                repository.markMessagesAsRead(conversationId)

                _isLoading.value = false
            } catch (e: Exception) {
                Log.e(TAG, "Error starting conversation", e)
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }

    /**
     * Lắng nghe tin nhắn trong conversation
     */
    private fun observeMessages(conversationId: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Observing messages for conversation: $conversationId")

                repository.observeMessages(conversationId).collect { messageList ->
                    _messages.value = messageList
                    Log.d(TAG, "Received ${messageList.size} messages")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error observing messages", e)
                _error.value = e.message
            }
        }
    }

    /**
     * Gửi tin nhắn
     */
    fun sendMessage(content: String, messageType: MessageType = MessageType.TEXT) {
        val conversationId = currentConversationId

        if (conversationId == null) {
            Log.w(TAG, "Cannot send message: No active conversation")
            _error.value = "No active conversation"
            return
        }

        if (content.isBlank() && messageType == MessageType.TEXT) {
            Log.w(TAG, "Cannot send empty message")
            return
        }

        viewModelScope.launch {
            try {
                Log.d(TAG, "Sending message to conversation: $conversationId")
                repository.sendMessage(conversationId, content, messageType)
                Log.d(TAG, "Message sent successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error sending message", e)
                _error.value = e.message
            }
        }
    }

    /**
     * Đánh dấu tin nhắn đã đọc
     */
    fun markAsRead() {
        val conversationId = currentConversationId ?: return

        viewModelScope.launch {
            try {
                repository.markMessagesAsRead(conversationId)
                Log.d(TAG, "Messages marked as read")
            } catch (e: Exception) {
                Log.e(TAG, "Error marking messages as read", e)
                _error.value = e.message
            }
        }
    }

    /**
     * Xóa conversation
     */
    fun deleteConversation(conversationId: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Deleting conversation: $conversationId")
                repository.deleteConversation(conversationId)
                Log.d(TAG, "Conversation deleted")
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting conversation", e)
                _error.value = e.message
            }
        }
    }

    /**
     * Cập nhật trạng thái online
     */
    fun updateOnlineStatus(isOnline: Boolean) {
        viewModelScope.launch {
            try {
                repository.updateOnlineStatus(isOnline)
                Log.d(TAG, "Online status updated: $isOnline")
            } catch (e: Exception) {
                Log.e(TAG, "Error updating online status", e)
                _error.value = e.message
            }
        }
    }

    /**
     * Reset state khi rời khỏi conversation
     */
    fun leaveConversation() {
        currentConversationId = null
        _messages.value = emptyList()
        _currentChatUser.value = null
        Log.d(TAG, "Left conversation")
    }

    /**
     * Clear error
     */
    fun clearError() {
        _error.value = null
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "ChatViewModel cleared")
        updateOnlineStatus(false)
    }
}