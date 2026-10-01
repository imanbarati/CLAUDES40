package com.example.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.dao.ChatMessageDao
import com.example.data.local.dao.ConversationDao
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.ConversationEntity
import com.example.model.ChatMessage
import com.example.model.Conversation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MessageRepository(
    private val conversationDao: ConversationDao,
    private val chatMessageDao: ChatMessageDao
) {
    constructor(context: Context) : this(
        AppDatabase.getInstance(context).conversationDao(),
        AppDatabase.getInstance(context).chatMessageDao()
    )

    init {
        CoroutineScope(Dispatchers.IO).launch {
            seedInitialDataIfEmpty()
        }
    }

    val conversationsFlow: Flow<List<Conversation>> = conversationDao.getAllConversations().map { list ->
        list.map { it.toDomain() }
    }

    fun getMessagesFlow(conversationId: String): Flow<List<ChatMessage>> {
        return chatMessageDao.getMessagesByConversation(conversationId).map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun getConversationsSync(): List<Conversation> = withContext(Dispatchers.IO) {
        conversationDao.getAllConversationsSync().map { it.toDomain() }
    }

    suspend fun getMessagesSync(conversationId: String): List<ChatMessage> = withContext(Dispatchers.IO) {
        val list = chatMessageDao.getMessagesByConversationSync(conversationId)
        if (list.isEmpty() && conversationId == "default-chat") {
            seedDefaultChatMessages()
            chatMessageDao.getMessagesByConversationSync(conversationId).map { it.toDomain() }
        } else {
            list.map { it.toDomain() }
        }
    }

    suspend fun saveMessage(message: ChatMessage): List<ChatMessage> = withContext(Dispatchers.IO) {
        chatMessageDao.insertMessage(ChatMessageEntity.fromDomain(message))
        // Update parent conversation
        conversationDao.updateLastMessage(
            id = message.conversationId,
            lastMessage = if (message.content.length > 60) message.content.take(57) + "..." else message.content,
            updatedAt = message.timestamp
        )
        chatMessageDao.getMessagesByConversationSync(message.conversationId).map { it.toDomain() }
    }

    suspend fun updateMessage(message: ChatMessage) = withContext(Dispatchers.IO) {
        chatMessageDao.updateMessage(ChatMessageEntity.fromDomain(message))
    }

    suspend fun deleteMessage(id: String) = withContext(Dispatchers.IO) {
        chatMessageDao.deleteMessageById(id)
    }

    suspend fun saveConversation(conversation: Conversation) = withContext(Dispatchers.IO) {
        conversationDao.insertConversation(ConversationEntity.fromDomain(conversation))
    }

    suspend fun deleteConversation(id: String) = withContext(Dispatchers.IO) {
        chatMessageDao.deleteMessagesByConversation(id)
        conversationDao.deleteConversationById(id)
    }

    suspend fun togglePinConversation(id: String) = withContext(Dispatchers.IO) {
        conversationDao.togglePin(id)
    }

    private suspend fun seedInitialDataIfEmpty() = withContext(Dispatchers.IO) {
        val count = conversationDao.getConversationCount()
        if (count == 0) {
            val defaultConv = Conversation(
                id = "default-chat",
                title = "Chat with Claude",
                lastMessage = "Welcome to Claude for Samsung Galaxy! ✨",
                updatedAt = System.currentTimeMillis(),
                isPinned = true,
                messageCount = 1
            )
            conversationDao.insertConversation(ConversationEntity.fromDomain(defaultConv))
            seedDefaultChatMessages()
        }
    }

    private suspend fun seedDefaultChatMessages() = withContext(Dispatchers.IO) {
        val welcomeMsg = ChatMessage(
            id = "msg-welcome-1",
            conversationId = "default-chat",
            role = "assistant",
            content = "Welcome to Claude for Samsung Galaxy! ✨\n\nDesigned specifically for your Galaxy A53 with a modern Samsung One UI experience. You can chat with Claude, run live web searches, summarize long documents, translate into multiple languages, and dictate messages with the new voice recognition microphone.\n\nType your message below or pick one of the quick action chips to get started!",
            timestamp = System.currentTimeMillis() - 60000,
            agentName = "Claude 3.5 Sonnet"
        )
        chatMessageDao.insertMessage(ChatMessageEntity.fromDomain(welcomeMsg))
    }
}
