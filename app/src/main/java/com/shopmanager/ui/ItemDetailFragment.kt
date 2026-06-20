package com.shopmanager.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.shopmanager.R
import com.shopmanager.databinding.FragmentItemDetailBinding
import java.io.File

class ItemDetailFragment : Fragment() {

    private var _binding: FragmentItemDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()
    private var itemId: Long = -1L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentItemDetailBinding.inflate(inflater, container, false)
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
        observeItem()
        setupButtons()
    }

    private fun observeItem() {
        viewModel.currentItem.observe(viewLifecycleOwner) { item ->
            if (item == null || item.id != itemId) return@observe

            binding.detailName.text = item.name
            binding.detailPrice.text = "\u20b9${String.format("%,.2f", item.price)}"
            binding.detailType.text = item.type.ifEmpty { getString(R.string.label_no_value) }
            binding.detailQuantity.text = item.quantity.toString()
            binding.detailDescription.text = item.description.ifEmpty { getString(R.string.label_no_value) }
            binding.detailLocation.text = item.location.ifEmpty { getString(R.string.label_no_value) }

            if (item.imagePath.isNotEmpty() && File(item.imagePath).exists()) {
                binding.imageCard.visibility = View.VISIBLE
                Glide.with(this)
                    .load(File(item.imagePath))
                    .centerCrop()
                    .into(binding.detailImage)
            } else {
                binding.imageCard.visibility = View.GONE
            }
        }
    }

    private fun setupButtons() {
        binding.btnEditItem.setOnClickListener {
            val bundle = Bundle().apply { putLong("itemId", itemId) }
            findNavController().navigate(R.id.addEditFragment, bundle)
        }

        binding.btnDeleteItem.setOnClickListener {
            val item = viewModel.currentItem.value ?: return@setOnClickListener
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.delete_item_title)
                .setMessage(getString(R.string.delete_item_message, item.name))
                .setPositiveButton(R.string.delete) { _, _ ->
                    viewModel.deleteItem(item)
                    Snackbar.make(binding.root, R.string.item_deleted, Snackbar.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }

        binding.btnRecordSale.setOnClickListener {
            val bundle = Bundle().apply { putLong("itemId", itemId) }
            findNavController().navigate(R.id.recordSaleFragment, bundle)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
