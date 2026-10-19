package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_memories")
data class UserMemoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: String, // "Interest", "Fact", "Goal", "Favorite"
    val fact: String,
    val timestamp: Long = System.currentTimeMillis()
)
