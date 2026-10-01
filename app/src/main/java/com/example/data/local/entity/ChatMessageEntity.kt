package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.model.ChatMessage

@Entity(
    tableName = "chat_messages",
    indices = [Index(value = ["conversationId"]), Index(value = ["timestamp"])]
)
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val role: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val sources: List<String> = emptyList(),
    val isPinned: Boolean = false,
    val isSaved: Boolean = false,
    val agentName: String = "Claude 3.5 Sonnet"
) {
    fun toDomain(): ChatMessage = ChatMessage(
        id = id,
        conversationId = conversationId,
        role = role,
        content = content,
        timestamp = timestamp,
        sources = sources,
        isPinned = isPinned,
        isSaved = isSaved,
        agentName = agentName
    )

    companion object {
        fun fromDomain(msg: ChatMessage): ChatMessageEntity = ChatMessageEntity(
            id = msg.id,
            conversationId = msg.conversationId,
            role = msg.role,
            content = msg.content,
            timestamp = msg.timestamp,
            sources = msg.sources,
            isPinned = msg.isPinned,
            isSaved = msg.isSaved,
            agentName = msg.agentName
        )
    }
}
