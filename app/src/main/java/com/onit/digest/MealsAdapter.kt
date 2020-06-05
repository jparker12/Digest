package com.onit.digest

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.onit.digest.model.MealWithIngredients

class MealsAdapter :
    ListAdapter<MealWithIngredients, MealsAdapter.ViewHolder>(DiffCallback()) {

    class DiffCallback: DiffUtil.ItemCallback<MealWithIngredients>() {
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
        val root: View = itemView
        val tvName: TextView = itemView.findViewById(R.id.tv_meal)
        val layoutExpandable: ConstraintLayout = itemView.findViewById(R.id.layout_expandable)
        val rvIngredients: RecyclerView = itemView.findViewById(R.id.rv_ingredients)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.meal_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mealWithIngredients = getItem(position)
        holder.tvName.text = mealWithIngredients.meal.name
        holder.rvIngredients.layoutManager = LinearLayoutManager(holder.rvIngredients.context)
        val adapter = ChildIngredientAdapter()
        holder.rvIngredients.adapter = adapter
        adapter.submitList(mealWithIngredients.ingredients)
        holder.root.setOnClickListener {
            if (holder.layoutExpandable.visibility == View.GONE) {
                holder.layoutExpandable.visibility = View.VISIBLE
            }
        }
    }
}