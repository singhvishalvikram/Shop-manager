package com.shopmanager.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "items")
data class Item(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String = "",
    val type: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val quantity: Int = 0,   // stock quantity
    val imagePath: String = "",    // local file path from camera
    val location: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)