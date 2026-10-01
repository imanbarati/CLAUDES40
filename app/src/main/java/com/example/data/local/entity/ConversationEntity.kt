package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.Conversation

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val lastMessage: String = "",
    val updatedAt: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val messageCount: Int = 0
) {
    fun toDomain(): Conversation = Conversation(
        id = id,
        title = title,
        lastMessage = lastMessage,
        updatedAt = updatedAt,
        isPinned = isPinned,
        messageCount = messageCount
    )

    companion object {
        fun fromDomain(conv: Conversation): ConversationEntity = ConversationEntity(
            id = conv.id,
            title = conv.title,
            lastMessage = conv.lastMessage,
            updatedAt = conv.updatedAt,
            isPinned = conv.isPinned,
            messageCount = conv.messageCount
        )
    }
}
