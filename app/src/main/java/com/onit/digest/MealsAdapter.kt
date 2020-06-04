package com.onit.digest

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.onit.digest.model.MealWithIngredients

class MealsAdapter :
    ListAdapter<MealWithIngredients, MealsAdapter.ViewHolder>(MealsDiffCallback()) {

    class MealsDiffCallback: DiffUtil.ItemCallback<MealWithIngredients>() {
        override fun areItemsTheSame(
            oldItem: MealWithIngredients,
            newItem: MealWithIngredients
        ): Boolean = oldItem.meal.id == newItem.meal.id

        override fun areContentsTheSame(
            oldItem: MealWithIngredients,
            newItem: MealWithIngredients
        ): Boolean = oldItem == newItem
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tv_meal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.meal_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvTitle.text = getItem(position).meal.name
    }
}