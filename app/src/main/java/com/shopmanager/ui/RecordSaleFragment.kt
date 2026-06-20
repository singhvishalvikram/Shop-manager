package com.shopmanager.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.shopmanager.R
import com.shopmanager.databinding.FragmentRecordSaleBinding

class RecordSaleFragment : Fragment() {

    private var _binding: FragmentRecordSaleBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()
    private var itemId: Long = -1L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecordSaleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        itemId = arguments?.getLong("itemId", -1L) ?: -1L
        if (itemId < 0) {
            findNavController().popBackStack()
            return
        }

        viewModel.loadItem(itemId)
        viewModel.currentItem.observe(viewLifecycleOwner) { item ->
            if (item != null && item.id == itemId) {
                binding.saleItemName.text = item.name
                binding.saleItemPrice.text = "Unit price: \u20b9${String.format("%,.2f", item.price)}"
                binding.saleItemStock.text = "In stock: ${item.quantity}"
                // Pre-fill sale price with item price
                if (binding.inputSalePrice.text.isNullOrEmpty()) {
                    binding.inputSalePrice.setText(item.price.toString())
                }
            }
        }

        binding.btnSaveSale.setOnClickListener { saveSale() }
        binding.btnCancelSale.setOnClickListener { findNavController().popBackStack() }
    }

    private fun saveSale() {
        val quantity = binding.inputSaleQuantity.text?.toString()?.toIntOrNull()
        if (quantity == null || quantity <= 0) {
            binding.inputSaleQuantity.error = "Enter a valid quantity"
            return
        }

        val salePrice = binding.inputSalePrice.text?.toString()?.toDoubleOrNull()
        if (salePrice == null || salePrice < 0) {
            binding.inputSalePrice.error = "Enter a valid price"
            return
        }

        val description = binding.inputSaleDescription.text?.toString()?.trim() ?: ""

        viewModel.recordSale(itemId, quantity, salePrice, description)
        Snackbar.make(binding.root, R.string.sale_recorded, Snackbar.LENGTH_SHORT).show()
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
