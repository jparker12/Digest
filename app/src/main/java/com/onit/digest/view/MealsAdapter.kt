package com.onit.digest.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.onit.digest.R
import com.onit.digest.model.MealWithIngredients

/**
 * Adapter class for displaying a list of user's meals.
 * Meals are displayed in [CardView] that can be expanded to reveal ingredients and an 'EDIT' button
 */
class MealsAdapter(
    private val onMealExpandToggle: (mealWithIngredients: MealWithIngredients) -> Unit,
    private val onEditMealClick: (mealWithIngredients: MealWithIngredients, cvMeal: CardView) -> Unit
) : ListAdapter<MealWithIngredients, MealsAdapter.ViewHolder>(DiffCallback()) {

    private var expandedMealIds = emptySet<Int>()

    // Keep track of the last item that was expanded/collapsed so it can be animated when
    // setExpandedMealIds() is called
    private var lastExpandedPosition: Int? = null

    /**
     * Supply a set of MealIds that have been expanded to show ingredients in the recycler view
     */
    fun setExpandedMealIds(expandedMealIds: Set<Int>) {
        if (this.expandedMealIds != expandedMealIds) {
            this.expandedMealIds = expandedMealIds
            val lastExpandedPosition = this.lastExpandedPosition
            if (lastExpandedPosition != null) {
                notifyItemChanged(lastExpandedPosition)
            } else {
                notifyDataSetChanged()
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<MealWithIngredients>() {
        override fun areItemsTheSame(
            oldItem: MealWithIngredients,
            newItem: MealWithIngredients
        ): Boolean = oldItem.meal.id == newItem.meal.id

        override fun areContentsTheSame(
            oldItem: MealWithIngredients,
            newItem: MealWithIngredients
        ): Boolean = oldItem == newItem
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cvMeal: CardView = itemView.findViewById(R.id.cv_meal)
        val tvName: TextView = itemView.findViewById(R.id.tv_meal)
        val layoutExpandable: ConstraintLayout = itemView.findViewById(R.id.layout_expandable)
        val rvIngredients: RecyclerView = itemView.findViewById(R.id.rv_ingredients)

        init {
            tvName.setOnClickListener {
                val position = adapterPosition
                lastExpandedPosition = position
                onMealExpandToggle(getItem(position))
            }
            val bnEditMeal: Button = itemView.findViewById(R.id.bn_edit_meal)
            bnEditMeal.setOnClickListener {
                onEditMealClick(getItem(adapterPosition), cvMeal)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.meal_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mealWithIngredients = getItem(position)
        holder.cvMeal.transitionName = "transition.card.meal.$position"
        holder.tvName.text = mealWithIngredients.meal.name
        holder.rvIngredients.layoutManager = LinearLayoutManager(holder.rvIngredients.context)
        val adapter = ChildIngredientAdapter()
        holder.rvIngredients.adapter = adapter
        adapter.submitList(mealWithIngredients.ingredients)
        if (expandedMealIds.contains(mealWithIngredients.meal.id)) {
            holder.layoutExpandable.visibility = View.VISIBLE
            holder.tvName.setCompoundDrawablesRelativeWithIntrinsicBounds(
                0,
                0,
                R.drawable.ic_arrow_up,
                0
            )
        } else {
            holder.layoutExpandable.visibility = View.GONE
            holder.tvName.setCompoundDrawablesRelativeWithIntrinsicBounds(
                0,
                0,
                R.drawable.ic_arrow_down,
                0
            )
        }
    }

    fun getMealWithIngredients(position: Int): MealWithIngredients = getItem(position)
}