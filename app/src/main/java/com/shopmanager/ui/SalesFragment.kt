package com.shopmanager.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.shopmanager.databinding.FragmentSalesBinding

class SalesFragment : Fragment() {

    private var _binding: FragmentSalesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var saleAdapter: SaleAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSalesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        saleAdapter = SaleAdapter()
        binding.salesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = saleAdapter
            setHasFixedSize(true)
        }

        viewModel.loadSales()
        observeData()
    }

    private fun observeData() {
        viewModel.sales.observe(viewLifecycleOwner) { sales ->
            saleAdapter.submitList(sales)
            if (sales.isEmpty()) {
                binding.emptyState.visibility = View.VISIBLE
                binding.salesRecyclerView.visibility = View.GONE
            } else {
                binding.emptyState.visibility = View.GONE
                binding.salesRecyclerView.visibility = View.VISIBLE
            }
        }

        viewModel.totalRevenue.observe(viewLifecycleOwner) { revenue ->
            binding.totalRevenue.text = "\u20b9${String.format("%,.2f", revenue)}"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
