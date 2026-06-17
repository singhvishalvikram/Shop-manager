package com.shopmanager.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {

    @Query("""
        SELECT * FROM items
        WHERE name LIKE '%' || :query || '%'
           OR type LIKE '%' || :query || '%'
           OR description LIKE '%' || :query || '%'
        ORDER BY updatedAt DESC
    """)
    suspend fun searchItems(query: String): List<Item>

    @Query("SELECT * FROM items ORDER BY updatedAt DESC")
    suspend fun getAllItems(): List<Item>

    @Query("SELECT * FROM items ORDER BY updatedAt DESC")
    fun getAllItemsFlow(): Flow<List<Item>>

    @Query("SELECT * FROM items WHERE id = :id")
    suspend fun getItemById(id: Long): Item?

    @Insert
    suspend fun insertItem(item: Item): Long

    @Update
    suspend fun updateItem(item: Item)

    @Delete
    suspend fun deleteItem(item: Item)

    @Query("DELETE FROM items WHERE id = :id")
    suspend fun deleteItemById(id: Long)

    @Query("SELECT COUNT(*) FROM items")
    suspend fun getTotalCount(): Int

    @Query("SELECT COUNT(*) FROM items")
    fun getTotalCountFlow(): Flow<Int>

    @Query("SELECT AVG(price) FROM items WHERE price > 0")
    suspend fun getAveragePrice(): Double?

    @Query("SELECT AVG(price) FROM items WHERE price > 0")
    fun getAveragePriceFlow(): Flow<Double?>

    @Query("SELECT SUM(price * quantity) FROM items")
    suspend fun getTotalStockValue(): Double?

    @Query("SELECT SUM(price * quantity) FROM items")
    fun getTotalStockValueFlow(): Flow<Double?>

    @Query("SELECT * FROM items ORDER BY updatedAt DESC LIMIT 10")
    suspend fun getRecentItems(): List<Item>

    @Query("SELECT * FROM items ORDER BY updatedAt DESC LIMIT 10")
    fun getRecentItemsFlow(): Flow<List<Item>>

    @Query("SELECT DISTINCT type FROM items WHERE type != '' ORDER BY type")
    suspend fun getTypes(): List<String>

    @Query("SELECT COUNT(*) FROM items WHERE type = :type")
    suspend fun getCountByType(type: String): Int
}