package com.shopmanager.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sales",
    indices = [Index("itemId"), Index("saleDate")],
    foreignKeys = [ForeignKey(
        entity = Item::class,
        parentColumns = ["id"],
        childColumns = ["itemId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Sale(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val itemId: Long,
    val quantitySold: Int,
    val salePrice: Double,    // price at which it was sold (can differ from item price)
    val saleDate: Long = System.currentTimeMillis(),
    val description: String = ""
)