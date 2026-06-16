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

    // Public State
    val allItems: Flow<List<Item>> = dao.getAllItemsFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val searchResults: StateFlow<List<Item>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isBlank()) dao.getAllItemsFlow()
            else flow { emit(dao.searchItems(query)) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalCount: LiveData<Int> = dao.getTotalCountFlow().asLiveData()
    val averagePrice: LiveData<Double> = dao.getAveragePriceFlow().asLiveData()
    val totalStockValue: LiveData<Double> = dao.getTotalStockValueFlow().asLiveData()
    val recentItems: LiveData<List<Item>> = dao.getRecentItemsFlow().asLiveData()

    private val _types = MutableLiveData<List<String>>(emptyList())
    val types: LiveData<List<String>> = _types

    private val _typeCounts = MutableLiveData<Map<String, Int>>(emptyMap())
    val typeCounts: LiveData<Map<String, Int>> = _typeCounts

    private val _currentItem = MutableLiveData<Item?>()
    val currentItem: LiveData<Item?> = _currentItem

    // Navigation
    private val _currentScreen = MutableLiveData(Screen.DASHBOARD)
    val currentScreen: LiveData<Screen> = _currentScreen

    private val _editingItemId = MutableLiveData<Long?>(null)
    val editingItemId: LiveData<Long?> = _editingItemId

    enum class Screen { DASHBOARD, ITEMS, ADD_ITEM, ITEM_DETAIL }

    fun navigateTo(screen: Screen) { _currentScreen.value = screen }

    // Actions
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
            _currentItem.value = dao.getItemById(id)
        }
    }

    fun startEditing(id: Long) {
        _editingItemId.value = id
        viewModelScope.launch {
            _currentItem.value = dao.getItemById(id)
        }
        navigateTo(Screen.ADD_ITEM)
    }

    fun clearEditing() {
        _editingItemId.value = null
        _currentItem.value = null
    }

    fun saveItem(
        name: String,
        type: String,
        description: String,
        price: Double,
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
                        imagePath = imagePath,
                        location = location,
                        createdAt = now,
                        updatedAt = now
                    )
                )
            }
            clearEditing()
            navigateTo(Screen.ITEMS)
        }
    }

    fun deleteItem(item: Item) {
        viewModelScope.launch {
            dao.deleteItem(item)
            com.shopmanager.utils.ImageUtils.deleteImage(item.imagePath)
            navigateTo(Screen.ITEMS)
        }
    }

    fun deleteItemById(id: Long) {
        viewModelScope.launch {
            val item = dao.getItemById(id) ?: return@launch
            dao.deleteItemById(id)
            com.shopmanager.utils.ImageUtils.deleteImage(item.imagePath)
        }
    }
}

fun <T> Flow<T>.asLiveData() = androidx.lifecycle.liveData {
    collect { emit(it) }
}
