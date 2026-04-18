package com.example.stepquest

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.stepquest.data.Item

class InventoryAdapter(
    private var items: List<Item>,
    private val onItemClick: (Item) -> Unit
) : RecyclerView.Adapter<InventoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivIcon: ImageView = view.findViewById(R.id.iv_item_icon)
        val tvQuantity: TextView = view.findViewById(R.id.tv_item_quantity)
        val tvName: TextView = view.findViewById(R.id.tv_item_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_inventory, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvName.text = item.nome
        holder.tvQuantity.text = "x${item.quantidade}"
        
        val context = holder.itemView.context
        val resId = context.resources.getIdentifier(item.imagemRes, "drawable", context.packageName)
        if (resId != 0) {
            holder.ivIcon.setImageResource(resId)
        } else {
            // Fallback para não ficar vazio
            holder.ivIcon.setImageResource(android.R.drawable.ic_menu_help)
        }

        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount() = items.size

    fun updateItems(newItems: List<Item>) {
        items = newItems
        notifyDataSetChanged()
    }
}