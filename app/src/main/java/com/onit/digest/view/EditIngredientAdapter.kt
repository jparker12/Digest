package com.onit.digest.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.ImageButton
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.onit.digest.R
import com.onit.digest.model.MealRepository

/**
 * Adapter for displaying a list of editable ingredients for a meal.
 */
class EditIngredientAdapter(
    /**
     * Current ingredients of the meal
     */
    private val editIngredients: MutableList<MealRepository.EditIngredientWithExtra>,
    /**
     * Names of all ingredients in storage (used for auto-complete)
     */
    private val allIngredients: Array<String>
) :
    RecyclerView.Adapter<EditIngredientAdapter.ViewHolder>() {

    /**
     * True if a new ingredient has recently been added
     */
    private var isIngredientAdded = false

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val actvName: AutoCompleteTextView = itemView.findViewById(R.id.actv_ingredient_name)
        val etUnits: EditText = itemView.findViewById(R.id.et_ingredient_units)

        init {
            val ibDelete: ImageButton = itemView.findViewById(R.id.ib_delete_ingredient)
            ibDelete.setOnClickListener {
                val position = adapterPosition
                editIngredients.removeAt(position)
                // Hide keyboard if this item has focus
                if (actvName.hasFocus()) {
                    toggleKeyboard(actvName, false)
                } else if (etUnits.hasFocus()) {
                    toggleKeyboard(etUnits, false)
                }
                notifyItemRemoved(position)
            }
            // Auto-complete values
            actvName.setAdapter(
                ArrayAdapter(
                    actvName.context,
                    android.R.layout.simple_dropdown_item_1line,
                    allIngredients
                )
            )
            // Listen for changes to ingredient name and units and update model
            actvName.addTextChangedListener(onTextChanged = { text, _, _, _ ->
                editIngredients[adapterPosition].ingredientName = text?.toString() ?: ""
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
        holder.actvName.setText(editIngredient.ingredientName)
        holder.etUnits.setText(editIngredient.quantity?.toString())
        if (isIngredientAdded && position == itemCount - 1) {
            // Open keyboard for this newly added ingredient
            isIngredientAdded = false
            toggleKeyboard(holder.actvName, true)
        }
    }

    override fun getItemCount(): Int = editIngredients.size

    /**
     * Adds a new empty ingredient to the list
     */
    fun onAddIngredientClick() {
        editIngredients.add(MealRepository.EditIngredientWithExtra())
        isIngredientAdded = true
        notifyItemInserted(editIngredients.size - 1)
    }

    /**
     * Open/close the soft keyboard
     */
    private fun toggleKeyboard(editText: EditText, open: Boolean) {
        val imm =
            editText.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (open) {
            editText.requestFocus()
            imm.toggleSoftInput(
                InputMethodManager.SHOW_FORCED,
                InputMethodManager.HIDE_IMPLICIT_ONLY
            )
        } else {
            editText.clearFocus()
            imm.hideSoftInputFromWindow(editText.windowToken, 0)
        }
    }

}