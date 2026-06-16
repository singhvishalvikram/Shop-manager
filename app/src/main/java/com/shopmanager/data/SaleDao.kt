package com.shopmanager.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {

    @Query("SELECT * FROM sales ORDER BY saleDate DESC")
    suspend fun getAllSales(): List<Sale>

    @Query("SELECT * FROM sales WHERE itemId = :itemId ORDER BY saleDate DESC")
    suspend fun getSalesForItem(itemId: Long): List<Sale>

    @Insert
    suspend fun insertSale(sale: Sale): Long

    @Update
    suspend fun updateSale(sale: Sale)

    @Delete
    suspend fun deleteSale(sale: Sale)

    @Query("SELECT SUM(quantitySold) FROM sales WHERE itemId = :itemId")
    suspend fun getTotalSoldForItem(itemId: Long): Int?

    @Query("SELECT SUM(quantitySold * salePrice) FROM sales")
    suspend fun getTotalRevenue(): Double?
}
