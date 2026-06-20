package com.shopmanager.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.shopmanager.R
import com.shopmanager.databinding.FragmentItemsBinding
import kotlinx.coroutines.launch

class ItemsFragment : Fragment() {

    private var _binding: FragmentItemsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var itemAdapter: ItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentItemsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearch()
        setupSort()
        setupFab()
        observeData()
    }

    private fun setupRecyclerView() {
        itemAdapter = ItemAdapter { item ->
            val bundle = Bundle().apply { putLong("itemId", item.id) }
            findNavController().navigate(R.id.itemDetailFragment, bundle)
        }
        binding.itemsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = itemAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupSearch() {
        binding.searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                viewModel.setSearchQuery(s?.toString() ?: "")
                binding.searchClearBtn.visibility =
                    if (s?.isNotEmpty() == true) View.VISIBLE else View.GONE
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        binding.searchClearBtn.setOnClickListener {
            binding.searchInput.text?.clear()
        }
    }

    private fun setupSort() {
        binding.btnSort.setOnClickListener { view ->
            PopupMenu(requireContext(), view).apply {
                menu.add(0, 0, 0, "Name (A-Z)")
                menu.add(0, 1, 1, "Name (Z-A)")
                menu.add(0, 2, 2, "Price (Low-High)")
                menu.add(0, 3, 3, "Price (High-Low)")
                menu.add(0, 4, 4, "Newest First")
                setOnMenuItemClickListener { menuItem ->
                    val currentList = itemAdapter.currentList.toMutableList()
                    val sorted = when (menuItem.itemId) {
                        0 -> currentList.sortedBy { it.name.lowercase() }
                        1 -> currentList.sortedByDescending { it.name.lowercase() }
                        2 -> currentList.sortedBy { it.price }
                        3 -> currentList.sortedByDescending { it.price }
                        4 -> currentList.sortedByDescending { it.updatedAt }
                        else -> currentList
                    }
                    itemAdapter.submitList(sorted)
                    true
                }
                show()
            }
        }
    }

    private fun setupFab() {
        binding.fabAddItem.setOnClickListener {
            val bundle = Bundle().apply { putLong("itemId", -1L) }
            findNavController().navigate(R.id.addEditFragment, bundle)
        }
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.searchResults.collect { items ->
                    itemAdapter.submitList(items)
                    val count = items.size
                    binding.itemCountLabel.text = "$count item${if (count != 1) "s" else ""}"

                    val isSearching = viewModel.searchQuery.value.isNotBlank()
                    if (items.isEmpty()) {
                        binding.emptyState.visibility = View.VISIBLE
                        binding.itemsRecyclerView.visibility = View.GONE
                        if (isSearching) {
                            binding.emptyTitle.text = getString(R.string.empty_search_title)
                            binding.emptySubtitle.text = getString(R.string.empty_search_subtitle)
                        } else {
                            binding.emptyTitle.text = getString(R.string.empty_inventory_title)
                            binding.emptySubtitle.text = getString(R.string.empty_inventory_subtitle)
                        }
                    } else {
                        binding.emptyState.visibility = View.GONE
                        binding.itemsRecyclerView.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
