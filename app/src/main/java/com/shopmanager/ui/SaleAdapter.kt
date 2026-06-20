package com.shopmanager.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.shopmanager.databinding.SaleRowBinding
import java.text.SimpleDateFormat
import java.util.*

class SaleAdapter : ListAdapter<SaleWithItemName, SaleAdapter.SaleViewHolder>(SaleDiffCallback()) {

    private val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SaleViewHolder {
        val binding = SaleRowBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SaleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SaleViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SaleViewHolder(
        private val binding: SaleRowBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(saleWithName: SaleWithItemName) {
            val sale = saleWithName.sale
            binding.saleItemName.text = saleWithName.itemName
            binding.saleDetails.text = "${sale.quantitySold} unit(s) @ \u20b9${String.format("%,.2f", sale.salePrice)}"
            binding.saleDate.text = dateFormat.format(Date(sale.saleDate))
            binding.saleAmount.text = "\u20b9${String.format("%,.2f", sale.quantitySold * sale.salePrice)}"
        }
    }
}

class SaleDiffCallback : DiffUtil.ItemCallback<SaleWithItemName>() {
    override fun areItemsTheSame(oldItem: SaleWithItemName, newItem: SaleWithItemName): Boolean {
        return oldItem.sale.id == newItem.sale.id
    }

    override fun areContentsTheSame(oldItem: SaleWithItemName, newItem: SaleWithItemName): Boolean {
        return oldItem == newItem
    }
}
