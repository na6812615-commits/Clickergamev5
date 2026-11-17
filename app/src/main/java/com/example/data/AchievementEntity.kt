package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String,
    val isUnlocked: Boolean = false,
    val isClaimed: Boolean = false,
    val progressValue: Double = 0.0,
    val targetValue: Double = 1.0,
    val unlockedAt: Long = 0L,
    val claimedAt: Long = 0L
)
