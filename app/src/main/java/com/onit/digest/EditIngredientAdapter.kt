package com.onit.digest

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.onit.digest.viewmodel.EditMealViewModel

class EditIngredientAdapter(
    private val editIngredients: MutableList<EditMealViewModel.EditIngredientWithExtra>
) :
    RecyclerView.Adapter<EditIngredientAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val etName: EditText = itemView.findViewById(R.id.et_ingredient_name)
        val etUnits: EditText = itemView.findViewById(R.id.et_ingredient_units)

        init {
            val ibDelete: ImageButton = itemView.findViewById(R.id.ib_delete_ingredient)
            ibDelete.setOnClickListener {
                val position = adapterPosition
                editIngredients.removeAt(position)
                if (etName.hasFocus()) {
                    etName.clearFocus()
                } else if (etUnits.hasFocus()) {
                    etUnits.clearFocus()
                }
                notifyItemRemoved(position)
            }
            etName.addTextChangedListener(onTextChanged = { text, _, _, _ ->
                editIngredients[adapterPosition].ingredientName = text?.toString()?.trim()
            })
            etUnits.addTextChangedListener(onTextChanged = { text, _, _, _ ->
                editIngredients[adapterPosition].quantity = text?.toString()?.trim()?.toIntOrNull()
            })
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.edit_ingredient_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val editIngredient = editIngredients[position]
        holder.etName.setText(editIngredient.ingredientName)
        holder.etUnits.setText(editIngredient.quantity?.toString())
    }

    override fun getItemCount(): Int = editIngredients.size

    fun onAddIngredientClick() {
        editIngredients.add(EditMealViewModel.EditIngredientWithExtra())
        notifyItemInserted(editIngredients.size - 1)
    }

}