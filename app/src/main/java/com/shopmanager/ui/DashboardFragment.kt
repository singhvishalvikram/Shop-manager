package com.shopmanager.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.shopmanager.R
import com.shopmanager.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.loadTypes()
        observeData()
    }

    private fun observeData() {
        viewModel.totalCount.observe(viewLifecycleOwner) {
            binding.statTotalItems.text = it.toString()
        }

        viewModel.averagePrice.observe(viewLifecycleOwner) {
            binding.statAvgPrice.text = formatPrice(it ?: 0.0)
        }

        viewModel.totalStockValue.observe(viewLifecycleOwner) {
            binding.statStockValue.text = formatPrice(it ?: 0.0)
        }

        viewModel.types.observe(viewLifecycleOwner) { types ->
            val counts = viewModel.typeCounts.value ?: emptyMap()
            binding.statTypes.text = types.size.toString()
            if (types.isEmpty()) {
                binding.dashTypeBreakdown.text = getString(R.string.no_items)
            } else {
                binding.dashTypeBreakdown.text = types.joinToString("\n") { t ->
                    "  • $t (${counts[t] ?: 0})"
                }
            }
        }

        viewModel.recentItems.observe(viewLifecycleOwner) { items ->
            binding.dashRecentItems.removeAllViews()
            if (items.isEmpty()) {
                val tv = TextView(requireContext()).apply {
                    text = getString(R.string.no_items)
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
                }
                binding.dashRecentItems.addView(tv)
            } else {
                items.forEach { item ->
                    val tv = TextView(requireContext()).apply {
                        text = "${item.name}  —  ${formatPrice(item.price)}"
                        setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
                        textSize = 14f
                        setPadding(0, 6, 0, 6)
                        setOnClickListener {
                            val bundle = Bundle().apply { putLong("itemId", item.id) }
                            findNavController().navigate(R.id.itemDetailFragment, bundle)
                        }
                    }
                    binding.dashRecentItems.addView(tv)
                }
            }
        }
    }

    private fun formatPrice(value: Double): String {
        return "\u20b9${String.format("%,.2f", value)}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
