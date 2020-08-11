package com.onit.digest.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.onit.digest.R
import com.onit.digest.model.IngredientWithExtra

class ChildIngredientAdapter :
    ListAdapter<IngredientWithExtra, ChildIngredientAdapter.ViewHolder>(
        DiffCallback()
    ) {

    class DiffCallback : DiffUtil.ItemCallback<IngredientWithExtra>() {
        override fun areItemsTheSame(
            oldItem: IngredientWithExtra,
            newItem: IngredientWithExtra
        ): Boolean = oldItem.ingredient.id == newItem.ingredient.id

        override fun areContentsTheSame(
            oldItem: IngredientWithExtra,
            newItem: IngredientWithExtra
        ): Boolean = oldItem == newItem

    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tv_ingredient)
        val tvUnits: TextView = itemView.findViewById(R.id.tv_units)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.child_ingredient_item, parent, false)
        return ViewHolder(
            itemView
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ingredientWithExtra = getItem(position)
        holder.tvName.text = ingredientWithExtra.ingredient.name
        val unitsStr = ingredientWithExtra.units?.toString()
        if (unitsStr.isNullOrBlank()) {
            holder.tvUnits.visibility = View.GONE
        } else {
            holder.tvUnits.text = unitsStr
            holder.tvUnits.visibility = View.VISIBLE
        }
    }
}