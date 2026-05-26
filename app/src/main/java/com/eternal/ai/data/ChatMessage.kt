package com.eternal.ai.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sender: String, // "造物主" or "永恒"
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)
