package com.example.adopt_pet_app.ui.screen.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.adopt_pet_app.data.repository.ChatRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.joinAll
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

    // State cho loading
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // State cho error
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // State cho current conversation ID
    private val _currentConversationId = MutableStateFlow<String?>(null)
    val currentConversationId: StateFlow<String?> = _currentConversationId.asStateFlow()

    private val _isOtherUserTyping = MutableStateFlow(false)
    val isOtherUserTyping: StateFlow<Boolean> = _isOtherUserTyping.asStateFlow()

    // Thêm hàm để update typing status
    fun setIsOtherUserTyping(isTyping: Boolean) {
        _isOtherUserTyping.value = isTyping
    }

    init {
        Log.d(TAG, "ChatViewModel initialized")
        observeConversations()
        updateOnlineStatus(true)
    }


    private fun observeConversations() {
        viewModelScope.launch {
            try {
                repository.observeConversations().collect { conversationList ->
                    Log.d(TAG, "Received ${conversationList.size} conversations")

                    // Lấy thông tin chi tiết cho mỗi conversation
                    val updatedConversations = conversationList.map { conversation ->
                        viewModelScope.launch {
                            val userInfo = repository.getUserInfo(conversation.userId)
                            val unreadCount = repository.getUnreadCount(conversation.id)

                            // Cập nhật conversation trong list
                            val updatedConversation = conversation.copy(
                                userName = userInfo?.name ?: "Unknown User",
                                userAvatar = userInfo?.avatar,
                                isOnline = userInfo?.isOnline ?: false,
                                unreadCount = unreadCount
                            )

                            // Cập nhật conversation list
                            val currentList = _conversations.value.toMutableList()
                            val index = currentList.indexOfFirst { it.id == conversation.id }
                            if (index != -1) {
                                currentList[index] = updatedConversation
                            } else {
                                currentList.add(updatedConversation)
                            }

                            // Sắp xếp lại theo thời gian
                            val sortedList = currentList.sortedByDescending { it.timestamp }
                            _conversations.value = sortedList
                        }
                    }

                    updatedConversations.joinAll()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error observing conversations", e)
                _error.value = "Lỗi khi tải cuộc trò chuyện: ${e.message}"
            }
        }
    }


    fun startConversation(otherUserId: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Starting conversation with user: $otherUserId")
                _isLoading.value = true

                // Tạo hoặc lấy conversation ID
                val conversationId = repository.getOrCreateConversation(otherUserId)
                _currentConversationId.value = conversationId

                Log.d(TAG, "Conversation ID: $conversationId")

                // Lấy và lắng nghe thông tin user
                loadAndObserveUserInfo(otherUserId)

                // Lắng nghe tin nhắn
                observeMessages(conversationId)

                // Đánh dấu tin nhắn đã đọc
                repository.markMessagesAsRead(conversationId)

                _isLoading.value = false
            } catch (e: Exception) {
                Log.e(TAG, "Error starting conversation", e)
                _error.value = "Lỗi khi bắt đầu cuộc trò chuyện: ${e.message}"
                _isLoading.value = false
            }
        }
    }


    private fun loadAndObserveUserInfo(userId: String) {
        viewModelScope.launch {
            try {
                // Lấy thông tin user ban đầu
                val userInfo = repository.getUserInfo(userId)
                _currentChatUser.value = userInfo

                // Lắng nghe thay đổi real-time
                repository.observeUserInfo(userId).collect { updatedUser ->
                    _currentChatUser.value = updatedUser
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading user info", e)
            }
        }
    }


    private fun observeMessages(conversationId: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Observing messages for conversation: $conversationId")

                repository.observeMessages(conversationId).collect { messageList ->
                    // Cập nhật messages với sender name
                    val updatedMessages = messageList.map { message ->
                        if (message.senderName == null) {
                            // Lấy sender name cho message
                            val userInfo = repository.getUserInfo(message.senderId)
                            message.copy(senderName = userInfo?.name)
                        } else {
                            message
                        }
                    }

                    _messages.value = updatedMessages

                    // Debug log để kiểm tra thứ tự
                    if (updatedMessages.isNotEmpty()) {
                        Log.d(TAG, "Messages displayed: ${updatedMessages.size}")
                        Log.d(TAG, "First message: ${updatedMessages.first().timestamp} - ${updatedMessages.first().content}")
                        Log.d(TAG, "Last message: ${updatedMessages.last().timestamp} - ${updatedMessages.last().content}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error observing messages", e)
                _error.value = "Lỗi khi tải tin nhắn: ${e.message}"
            }
        }
    }


    fun sendMessage(content: String, messageType: MessageType = MessageType.TEXT) {
        val conversationId = _currentConversationId.value

        if (conversationId == null) {
            Log.w(TAG, "Cannot send message: No active conversation")
            _error.value = "Không có cuộc trò chuyện đang hoạt động"
            return
        }

        if (content.isBlank() && messageType == MessageType.TEXT) {
            Log.w(TAG, "Cannot send empty message")
            _error.value = "Tin nhắn không được để trống"
            return
        }

        viewModelScope.launch {
            try {
                Log.d(TAG, "Sending message to conversation: $conversationId")
                repository.sendMessage(conversationId, content, messageType)
                Log.d(TAG, "Message sent successfully")

                // Đánh dấu đã đọc
                repository.markMessagesAsRead(conversationId)
            } catch (e: Exception) {
                Log.e(TAG, "Error sending message", e)
                _error.value = "Lỗi khi gửi tin nhắn: ${e.message}"
            }
        }
    }


    fun markAsRead() {
        val conversationId = _currentConversationId.value ?: return

        viewModelScope.launch {
            try {
                repository.markMessagesAsRead(conversationId)
                Log.d(TAG, "Messages marked as read")
            } catch (e: Exception) {
                Log.e(TAG, "Error marking messages as read", e)
                _error.value = "Lỗi khi đánh dấu tin nhắn: ${e.message}"
            }
        }
    }


    fun deleteConversation(conversationId: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Deleting conversation: $conversationId")
                repository.deleteConversation(conversationId)
                Log.d(TAG, "Conversation deleted")

                // Nếu đang xem conversation này, reset state
                if (_currentConversationId.value == conversationId) {
                    leaveConversation()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting conversation", e)
                _error.value = "Lỗi khi xóa cuộc trò chuyện: ${e.message}"
            }
        }
    }


    fun deleteMessage(messageId: String) {
        val conversationId = _currentConversationId.value ?: return

        viewModelScope.launch {
            try {
                Log.d(TAG, "Deleting message: $messageId")
                repository.deleteMessage(conversationId, messageId)
                Log.d(TAG, "Message deleted")
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting message", e)
                _error.value = "Lỗi khi xóa tin nhắn: ${e.message}"
            }
        }
    }


    fun updateOnlineStatus(isOnline: Boolean) {
        viewModelScope.launch {
            try {
                repository.updateOnlineStatus(isOnline)
                Log.d(TAG, "Online status updated: $isOnline")
            } catch (e: Exception) {
                Log.e(TAG, "Error updating online status", e)
                _error.value = "Lỗi khi cập nhật trạng thái: ${e.message}"
            }
        }
    }


    fun leaveConversation() {
        _currentConversationId.value = null
        _messages.value = emptyList()
        _currentChatUser.value = null
        Log.d(TAG, "Left conversation")
    }


    fun getConversationById(conversationId: String): Conversation? {
        return _conversations.value.firstOrNull { it.id == conversationId }
    }


    fun clearError() {
        _error.value = null
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "ChatViewModel cleared")
        updateOnlineStatus(false)
    }
}