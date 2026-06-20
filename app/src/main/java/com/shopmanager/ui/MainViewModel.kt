package com.shopmanager.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.shopmanager.data.AppDatabase
import com.shopmanager.data.Item
import com.shopmanager.data.Sale
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val itemDao = db.itemDao()
    private val saleDao = db.saleDao()

    // ── Search ──
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val searchResults: StateFlow<List<Item>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isBlank()) itemDao.getAllItemsFlow()
            else flow { emit(itemDao.searchItems(query)) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ── Dashboard Stats ──
    val totalCount: LiveData<Int> = itemDao.getTotalCountFlow().asLiveData()
    val averagePrice: LiveData<Double?> = itemDao.getAveragePriceFlow().asLiveData()
    val totalStockValue: LiveData<Double?> = itemDao.getTotalStockValueFlow().asLiveData()
    val recentItems: LiveData<List<Item>> = itemDao.getRecentItemsFlow().asLiveData()

    private val _types = MutableLiveData<List<String>>(emptyList())
    val types: LiveData<List<String>> = _types

    private val _typeCounts = MutableLiveData<Map<String, Int>>(emptyMap())
    val typeCounts: LiveData<Map<String, Int>> = _typeCounts

    // ── Current Item (for detail view) ──
    private val _currentItem = MutableLiveData<Item?>()
    val currentItem: LiveData<Item?> = _currentItem

    // ── Sales ──
    private val _sales = MutableLiveData<List<SaleWithItemName>>(emptyList())
    val sales: LiveData<List<SaleWithItemName>> = _sales

    private val _totalRevenue = MutableLiveData<Double>(0.0)
    val totalRevenue: LiveData<Double> = _totalRevenue

    // ── Actions ──

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun loadTypes() {
        viewModelScope.launch {
            val typeList = itemDao.getTypes()
            _types.value = typeList
            val counts = mutableMapOf<String, Int>()
            typeList.forEach { type ->
                counts[type] = itemDao.getCountByType(type)
            }
            _typeCounts.value = counts
        }
    }

    fun loadItem(id: Long) {
        viewModelScope.launch {
            _currentItem.value = itemDao.getItemById(id)
        }
    }

    fun saveItem(
        editingId: Long?,
        name: String,
        type: String,
        description: String,
        price: Double,
        quantity: Int,
        imagePath: String,
        location: String
    ) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            if (editingId != null && editingId > 0) {
                val item = itemDao.getItemById(editingId) ?: return@launch
                itemDao.updateItem(
                    item.copy(
                        name = name,
                        type = type,
                        description = description,
                        price = price,
                        quantity = quantity,
                        imagePath = imagePath.ifBlank { item.imagePath },
                        location = location,
                        updatedAt = now
                    )
                )
            } else {
                itemDao.insertItem(
                    Item(
                        name = name,
                        type = type,
                        description = description,
                        price = price,
                        quantity = quantity,
                        imagePath = imagePath,
                        location = location,
                        createdAt = now,
                        updatedAt = now
                    )
                )
            }
        }
    }

    fun deleteItem(item: Item) {
        viewModelScope.launch {
            itemDao.deleteItem(item)
            com.shopmanager.utils.ImageUtils.deleteImage(item.imagePath)
        }
    }

    // ── Sales ──

    fun loadSales() {
        viewModelScope.launch {
            val allSales = saleDao.getAllSales()
            val salesWithNames = allSales.map { sale ->
                val item = itemDao.getItemById(sale.itemId)
                SaleWithItemName(
                    sale = sale,
                    itemName = item?.name ?: "Deleted Item"
                )
            }
            _sales.value = salesWithNames
            _totalRevenue.value = saleDao.getTotalRevenue() ?: 0.0
        }
    }

    fun recordSale(itemId: Long, quantity: Int, salePrice: Double, description: String) {
        viewModelScope.launch {
            saleDao.insertSale(
                Sale(
                    itemId = itemId,
                    quantitySold = quantity,
                    salePrice = salePrice,
                    description = description
                )
            )
            itemDao.decreaseQuantity(itemId, quantity)
        }
    }

    fun getExistingTypes(): LiveData<List<String>> {
        val result = MutableLiveData<List<String>>()
        viewModelScope.launch {
            result.postValue(itemDao.getTypes())
        }
        return result
    }
}

data class SaleWithItemName(
    val sale: Sale,
    val itemName: String
)

fun <T> Flow<T>.asLiveData(): LiveData<T> = androidx.lifecycle.liveData {
    collect { emit(it) }
}
