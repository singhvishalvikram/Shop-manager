package com.shopmanager.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.shopmanager.R
import com.shopmanager.data.Item
import com.shopmanager.databinding.ItemRowBinding
import java.io.File

class ItemAdapter(
    private val onItemClick: (Item) -> Unit
) : ListAdapter<Item, ItemAdapter.ItemViewHolder>(ItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ItemRowBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ItemViewHolder(
        private val binding: ItemRowBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Item) {
            binding.rowName.text = item.name
            binding.rowType.text = item.type.ifEmpty { "Uncategorized" }
            binding.rowPrice.text = formatPrice(item.price)

            if (item.imagePath.isNotEmpty()) {
                Glide.with(binding.root.context)
                    .load(File(item.imagePath))
                    .placeholder(R.drawable.ic_placeholder_image)
                    .error(R.drawable.ic_placeholder_image)
                    .centerCrop()
                    .into(binding.rowImage)
            } else {
                binding.rowImage.setImageResource(R.drawable.ic_placeholder_image)
            }

            binding.root.setOnClickListener { onItemClick(item) }
        }
    }

    private fun formatPrice(value: Double): String {
        return "\u20b9${String.format("%,.2f", value)}"
    }
}

class ItemDiffCallback : DiffUtil.ItemCallback<Item>() {
    override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean {
        return oldItem == newItem
    }
}
