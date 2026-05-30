package com.eternal.ai.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sender: String,       // "造物主" 或 "永恒"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isDevMode: Boolean = false  // 是否为开发模式对话
)
