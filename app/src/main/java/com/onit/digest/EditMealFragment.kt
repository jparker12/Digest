package com.onit.digest

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionInflater
import com.onit.digest.viewmodel.EditMealViewModel

/**
 * A simple [Fragment] subclass.
 */
class EditMealFragment : Fragment() {

    private lateinit var viewModel: EditMealViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition =
            TransitionInflater.from(context).inflateTransition(android.R.transition.move)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_meal, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val safeArgs: EditMealFragmentArgs by navArgs()

        viewModel = ViewModelProvider(
            this,
            EditMealViewModel.Factory(requireActivity().application, safeArgs.selectedMeal)
        ).get(EditMealViewModel::class.java)

        val view = requireView()

        (requireActivity() as AppCompatActivity).supportActionBar?.title = viewModel.actionBarTitle

        val etMealName: EditText = view.findViewById(R.id.et_meal_name)
        etMealName.addTextChangedListener(onTextChanged = { text, _, _, _ ->
            viewModel.onMealNameChanged(text?.toString() ?: "")
        })
        viewModel.mealName.observe(viewLifecycleOwner, Observer { mealName ->
            if (mealName != etMealName.text.toString()) {
                etMealName.setText(mealName)
            }
        })

        val recyclerView: RecyclerView = view.findViewById(R.id.rv_ingredients)
        recyclerView.layoutManager = LinearLayoutManager(view.context)
        val adapter = EditIngredientAdapter(viewModel.editIngredients)
        recyclerView.adapter = adapter

        val bnAddIngredient = view.findViewById<Button>(R.id.bn_add_ingredient)
        bnAddIngredient.setOnClickListener {
            adapter.onAddIngredientClick()
        }
    }
}
