package com.example.stepquest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.stepquest.data.AppDatabase
import com.example.stepquest.data.Item
import com.example.stepquest.data.SessionManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch

class InventoryBottomSheet(
    private val onItemUsed: (Item) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: InventoryAdapter
    private lateinit var db: AppDatabase
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.inventory_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        recyclerView = view.findViewById(R.id.rv_inventory)
        db = AppDatabase.getDatabase(requireContext())
        sessionManager = SessionManager(requireContext())
        val userId = sessionManager.getUserId()

        recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        adapter = InventoryAdapter(emptyList()) { item ->
            useItem(item)
        }
        recyclerView.adapter = adapter

        loadItems(userId)
    }

    private fun loadItems(userId: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            val items = db.userDao().getUserItems(userId)
            adapter.updateItems(items)
        }
    }

    private fun useItem(item: Item) {
        viewLifecycleOwner.lifecycleScope.launch {
            val novaQuantidade = item.quantidade - 1
            if (novaQuantidade >= 0) {
                db.userDao().updateItemQuantity(item.id, novaQuantidade)
                onItemUsed(item)
                dismiss()
            }
        }
    }
}