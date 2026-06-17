package com.shopmanager.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.shopmanager.data.AppDatabase
import com.shopmanager.data.Item
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).itemDao()

    // Search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val searchResults: StateFlow<List<Item>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isBlank()) dao.getAllItemsFlow()
            else flow { emit(dao.searchItems(query)) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dashboard stats
    val totalCount: LiveData<Int> = dao.getTotalCountFlow().asLiveData()
    val averagePrice: LiveData<Double?> = dao.getAveragePriceFlow().asLiveData()
    val totalStockValue: LiveData<Double?> = dao.getTotalStockValueFlow().asLiveData()
    val recentItems: LiveData<List<Item>> = dao.getRecentItemsFlow().asLiveData()

    // Types
    private val _types = MutableLiveData<List<String>>(emptyList())
    val types: LiveData<List<String>> = _types

    private val _typeCounts = MutableLiveData<Map<String, Int>>(emptyMap())
    val typeCounts: LiveData<Map<String, Int>> = _typeCounts

    // Editing
    private val _editingItemId = MutableLiveData<Long?>(null)
    val editingItemId: LiveData<Long?> = _editingItemId

    private val _editingItem = MutableLiveData<Item?>()
    val editingItem: LiveData<Item?> = _editingItem

    fun setSearchQuery(query: String) { _searchQuery.value = query }

    fun loadTypes() {
        viewModelScope.launch {
            val typeList = dao.getTypes()
            _types.value = typeList
            val counts = mutableMapOf<String, Int>()
            typeList.forEach { type ->
                counts[type] = dao.getCountByType(type)
            }
            _typeCounts.value = counts
        }
    }

    fun loadItem(id: Long) {
        viewModelScope.launch {
            val item = dao.getItemById(id)
            _editingItem.value = item
        }
    }

    fun clearEditing() {
        _editingItemId.value = null
        _editingItem.value = null
    }

    fun saveItem(
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
            val existing = _editingItemId.value

            if (existing != null) {
                val item = dao.getItemById(existing) ?: return@launch
                dao.updateItem(
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
                dao.insertItem(
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
            clearEditing()
        }
    }

    fun deleteItem(item: Item) {
        viewModelScope.launch {
            dao.deleteItem(item)
            com.shopmanager.utils.ImageUtils.deleteImage(item.imagePath)
        }
    }
}

fun <T> Flow<T>.asLiveData() = androidx.lifecycle.liveData {
    collect { emit(it) }
}