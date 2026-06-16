package com.shopmanager

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.shopmanager.data.Item
import com.shopmanager.ui.MainViewModel
import com.shopmanager.utils.ImageUtils
import java.io.File
import java.text.NumberFormat
import java.util.*
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel

    // Views
    private lateinit var screenDashboard: View
    private lateinit var screenItems: View
    private lateinit var screenAddItem: View
    private lateinit var screenItemDetail: View
    private lateinit var toolbar: MaterialToolbar
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var itemsRecyclerView: RecyclerView
    private lateinit var searchInput: TextInputEditText
    private lateinit var searchClearBtn: ImageButton
    private lateinit var itemCountLabel: TextView
    private lateinit var statTotalItems: TextView
    private lateinit var statTypes: TextView
    private lateinit var statAvgPrice: TextView
    private lateinit var statStockValue: TextView
    private lateinit var dashTypeBreakdown: TextView
    private lateinit var dashRecentItems: LinearLayout
    private lateinit var formTitle: TextView
    private lateinit var btnTakePhoto: MaterialButton
    private lateinit var itemPhotoPreview: ImageView
    private lateinit var inputName: TextInputEditText
    private lateinit var inputType: TextInputEditText
    private lateinit var inputDescription: TextInputEditText
    private lateinit var inputPrice: TextInputEditText
    private lateinit var inputLocation: TextInputEditText
    private lateinit var btnSaveItem: MaterialButton
    private lateinit var btnCancelForm: MaterialButton
    private lateinit var detailContainer: LinearLayout
    private lateinit var detailImage: ImageView
    private lateinit var detailName: TextView
    private lateinit var detailType: TextView
    private lateinit var detailDescription: TextView
    private lateinit var detailPrice: TextView
    private lateinit var detailLocation: TextView
    private lateinit var btnEditItem: MaterialButton
    private lateinit var btnDeleteItem: MaterialButton
    private lateinit var btnBackFromDetail: MaterialButton

    private var photoFile: File? = null
    private var capturedPhotoPath: String? = null

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && photoFile != null) {
            val compressed = ImageUtils.compressImage(this, Uri.fromFile(photoFile))
            if (compressed != null) {
                capturedPhotoPath = compressed
                showPhotoPreview(compressed)
            } else {
                Toast.makeText(this, "Failed to process photo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) launchCamera() else
            Toast.makeText(this, "Camera permission needed for photos", Toast.LENGTH_LONG).show()
    }

    // Item Adapter
    private inner class ItemAdapter(
        private val items: MutableList<Item> = mutableListOf()
    ) : RecyclerView.Adapter<ItemAdapter.ViewHolder>() {

        private var onItemClick: ((Item) -> Unit)? = null

        fun setOnItemClickListener(listener: (Item) -> Unit) { onItemClick = listener }

        fun submitList(list: List<Item>) {
            items.clear()
            items.addAll(list)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
            val view = layoutInflater.inflate(R.layout.item_row, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(items[position])
        }

        override fun getItemCount() = items.size

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val name = itemView.findViewById<TextView>(R.id.rowName)
            private val type = itemView.findViewById<TextView>(R.id.rowType)
            private val price = itemView.findViewById<TextView>(R.id.rowPrice)
            private val image = itemView.findViewById<ImageView>(R.id.rowImage)

            fun bind(item: Item) {
                name.text = item.name
                type.text = item.type
                price.text = formatPrice(item.price)
                if (item.imagePath.isNotEmpty()) {
                    Glide.with(this@MainActivity)
                        .load(File(item.imagePath))
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .centerCrop()
                        .into(image)
                } else {
                    image.setImageResource(android.R.drawable.ic_menu_gallery)
                }
                itemView.setOnClickListener { onItemClick?.invoke(item) }
            }
        }
    }

    private val itemAdapter = ItemAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Bind all views
        screenDashboard = findViewById(R.id.screenDashboard)
        screenItems = findViewById(R.id.screenItems)
        screenAddItem = findViewById(R.id.screenAddItem)
        screenItemDetail = findViewById(R.id.screenItemDetail)
        toolbar = findViewById(R.id.toolbar)
        bottomNav = findViewById(R.id.bottomNav)
        itemsRecyclerView = findViewById(R.id.itemsRecyclerView)
        searchInput = findViewById(R.id.searchInput)
        searchClearBtn = findViewById(R.id.searchClearBtn)
        itemCountLabel = findViewById(R.id.itemCountLabel)
        statTotalItems = findViewById(R.id.statTotalItems)
        statTypes = findViewById(R.id.statTypes)
        statAvgPrice = findViewById(R.id.statAvgPrice)
        statStockValue = findViewById(R.id.statStockValue)
        dashTypeBreakdown = findViewById(R.id.dashTypeBreakdown)
        dashRecentItems = findViewById(R.id.dashRecentItems)
        formTitle = findViewById(R.id.formTitle)
        btnTakePhoto = findViewById(R.id.btnTakePhoto)
        itemPhotoPreview = findViewById(R.id.itemPhotoPreview)
        inputName = findViewById(R.id.inputName)
        inputType = findViewById(R.id.inputType)
        inputDescription = findViewById(R.id.inputDescription)
        inputPrice = findViewById(R.id.inputPrice)
        inputLocation = findViewById(R.id.inputLocation)
        btnSaveItem = findViewById(R.id.btnSaveItem)
        btnCancelForm = findViewById(R.id.btnCancelForm)
        detailContainer = findViewById(R.id.detailContainer)
        detailImage = findViewById(R.id.detailImage)
        detailName = findViewById(R.id.detailName)
        detailType = findViewById(R.id.detailType)
        detailDescription = findViewById(R.id.detailDescription)
        detailPrice = findViewById(R.id.detailPrice)
        detailLocation = findViewById(R.id.detailLocation)
        btnEditItem = findViewById(R.id.btnEditItem)
        btnDeleteItem = findViewById(R.id.btnDeleteItem)
        btnBackFromDetail = findViewById(R.id.btnBackFromDetail)

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        // Init RecyclerView
        itemsRecyclerView.layoutManager = LinearLayoutManager(this)
        itemsRecyclerView.adapter = itemAdapter
        itemAdapter.setOnItemClickListener { item ->
            viewModel.loadItem(item.id)
            navigateTo(Screen.ITEM_DETAIL)
        }

        // Bottom Navigation
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> navigateTo(Screen.DASHBOARD)
                R.id.nav_items -> navigateTo(Screen.ITEMS)
                R.id.nav_add -> {
                    viewModel.clearEditing()
                    clearForm()
                    navigateTo(Screen.ADD_ITEM)
                }
            }
            true
        }

        // Search
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                viewModel.setSearchQuery(s?.toString() ?: "")
                searchClearBtn.visibility = if (s?.isNotEmpty() == true) View.VISIBLE else View.GONE
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        searchClearBtn.setOnClickListener {
            searchInput.text?.clear()
        }

        // Form buttons
        btnTakePhoto.setOnClickListener { requestCamera() }
        btnSaveItem.setOnClickListener { saveItem() }
        btnCancelForm.setOnClickListener {
            viewModel.clearEditing()
            clearForm()
            navigateTo(Screen.ITEMS)
        }

        // Detail buttons
        btnEditItem.setOnClickListener {
            viewModel.currentItem.value?.let { item ->
                viewModel.startEditing(item.id)
                populateForm(item)
            }
        }
        btnDeleteItem.setOnClickListener {
            viewModel.currentItem.value?.let { item ->
                confirmDelete(item)
            }
        }
        btnBackFromDetail.setOnClickListener { navigateTo(Screen.ITEMS) }

        // Observe data
        observeData()

        // Start on dashboard
        navigateTo(Screen.DASHBOARD)
    }

    private enum class Screen { DASHBOARD, ITEMS, ADD_ITEM, ITEM_DETAIL }

    private fun navigateTo(screen: Screen) {
        screenDashboard.visibility = if (screen == Screen.DASHBOARD) View.VISIBLE else View.GONE
        screenItems.visibility = if (screen == Screen.ITEMS) View.VISIBLE else View.GONE
        screenAddItem.visibility = if (screen == Screen.ADD_ITEM) View.VISIBLE else View.GONE
        screenItemDetail.visibility = if (screen == Screen.ITEM_DETAIL) View.VISIBLE else View.GONE

        toolbar.title = when (screen) {
            Screen.DASHBOARD -> "Dashboard"
            Screen.ITEMS -> "Items"
            Screen.ADD_ITEM -> if (viewModel.editingItemId.value != null) "Edit Item" else "Add Item"
            Screen.ITEM_DETAIL -> "Item Details"
        }

        when (screen) {
            Screen.DASHBOARD -> {
                bottomNav.selectedItemId = R.id.nav_dashboard
                viewModel.loadTypes()
            }
            Screen.ITEMS -> bottomNav.selectedItemId = R.id.nav_items
            Screen.ADD_ITEM -> bottomNav.selectedItemId = R.id.nav_add
            else -> {}
        }
    }

    private fun observeData() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.searchResults.collect { items ->
                    itemAdapter.submitList(items)
                    itemCountLabel.text = "${items.size} item${if (items.size != 1) "s" else ""}"
                }
            }
        }

        viewModel.totalCount.observe(this) { statTotalItems.text = it.toString() }
        viewModel.averagePrice.observe(this) { statAvgPrice.text = formatPrice(it ?: 0.0) }
        viewModel.totalStockValue.observe(this) { statStockValue.text = formatPrice(it ?: 0.0) }

        viewModel.types.observe(this) { types ->
            val counts = viewModel.typeCounts.value ?: emptyMap()
            statTypes.text = types.size.toString()
            if (types.isEmpty()) {
                dashTypeBreakdown.text = "No items yet"
            } else {
                dashTypeBreakdown.text = types.joinToString("\n") { t ->
                    "  • $t (${counts[t] ?: 0})"
                }
            }
        }

        viewModel.recentItems.observe(this) { items ->
            dashRecentItems.removeAllViews()
            if (items.isEmpty()) {
                val tv = TextView(this).apply {
                    text = "No items yet"
                    setTextColor(ContextCompat.getColor(this@MainActivity, R.color.text_secondary))
                }
                dashRecentItems.addView(tv)
            } else {
                items.forEach { item ->
                    val tv = TextView(this).apply {
                        text = "${item.name}  —  ${formatPrice(item.price)}"
                        setTextColor(ContextCompat.getColor(this@MainActivity, R.color.text_primary))
                        textSize = 14f
                        setPadding(0, 6, 0, 6)
                    }
                    dashRecentItems.addView(tv)
                }
            }
        }

        viewModel.currentItem.observe(this) { item ->
            if (item != null) showItemDetail(item)
        }
    }

    private fun showItemDetail(item: Item) {
        detailName.text = item.name
        detailType.text = item.type.ifEmpty { "—" }
        detailDescription.text = item.description.ifEmpty { "—" }
        detailPrice.text = formatPrice(item.price)
        detailLocation.text = item.location.ifEmpty { "—" }

        if (item.imagePath.isNotEmpty()) {
            detailImage.visibility = View.VISIBLE
            Glide.with(this)
                .load(File(item.imagePath))
                .placeholder(android.R.drawable.ic_menu_gallery)
                .centerCrop()
                .into(detailImage)
        } else {
            detailImage.visibility = View.GONE
        }
    }

    private fun requestCamera() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED -> launchCamera()
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                MaterialAlertDialogBuilder(this)
                    .setTitle("Camera Permission")
                    .setMessage("Camera permission is needed to take photos of your shop items.")
                    .setPositiveButton("OK") { _, _ -> permissionLauncher.launch(Manifest.permission.CAMERA) }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
            else -> permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun launchCamera() {
        photoFile = ImageUtils.createImageFile(this)
        val uri = FileProvider.getUriForFile(
            this, "${packageName}.fileprovider", photoFile!!
        )
        cameraLauncher.launch(uri)
    }

    private fun showPhotoPreview(path: String) {
        itemPhotoPreview.visibility = View.VISIBLE
        Glide.with(this)
            .load(File(path))
            .centerCrop()
            .into(itemPhotoPreview)
    }

    private fun clearForm() {
        capturedPhotoPath = null
        inputName.text?.clear()
        inputType.text?.clear()
        inputDescription.text?.clear()
        inputPrice.text?.clear()
        inputLocation.text?.clear()
        itemPhotoPreview.visibility = View.GONE
        formTitle.text = "Add New Item"
        btnSaveItem.text = "Save Item"
    }

    private fun populateForm(item: Item) {
        formTitle.text = "Edit Item"
        btnSaveItem.text = "Update Item"
        inputName.setText(item.name)
        inputType.setText(item.type)
        inputDescription.setText(item.description)
        inputPrice.setText(if (item.price > 0) item.price.toString() else "")
        inputLocation.setText(item.location)
        capturedPhotoPath = item.imagePath
        if (item.imagePath.isNotEmpty()) showPhotoPreview(item.imagePath)
    }

    private fun saveItem() {
        val name = inputName.text?.toString()?.trim() ?: ""
        if (name.isEmpty()) {
            inputName.error = "Name is required"
            return
        }

        val price = inputPrice.text?.toString()?.toDoubleOrNull() ?: 0.0

        viewModel.saveItem(
            name = name,
            type = inputType.text?.toString()?.trim() ?: "",
            description = inputDescription.text?.toString()?.trim() ?: "",
            price = price,
            imagePath = capturedPhotoPath ?: "",
            location = inputLocation.text?.toString()?.trim() ?: ""
        )

        Toast.makeText(this, "Item saved!", Toast.LENGTH_SHORT).show()
    }

    private fun confirmDelete(item: Item) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete Item")
            .setMessage("Delete \"${item.name}\"? This cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteItem(item)
                Toast.makeText(this, "Item deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun formatPrice(value: Double): String {
        return "₹${String.format("%,.2f", value)}"
    }
}
