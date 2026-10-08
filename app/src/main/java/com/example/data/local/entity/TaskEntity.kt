package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.S40Task

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val details: String = "",
    val dateStr: String = "",
    val isCompleted: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toDomain(): S40Task = S40Task(
        id = id,
        title = title,
        details = details,
        dateStr = dateStr,
        isCompleted = isCompleted,
        timestamp = timestamp
    )

    companion object {
        fun fromDomain(task: S40Task): TaskEntity = TaskEntity(
            id = task.id,
            title = task.title,
            details = task.details,
            dateStr = task.dateStr,
            isCompleted = task.isCompleted,
            timestamp = task.timestamp
        )
    }
}
