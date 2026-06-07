package com.eternal.ai.data; import androidx.room.Entity; import androidx.room.PrimaryKey
@Entity(tableName = "messages") data class ChatMessage(@PrimaryKey(autoGenerate = true) val id: Long = 0, val sender: String, val content: String, val timestamp: Long = System.currentTimeMillis(), val isDevMode: Boolean = false)
