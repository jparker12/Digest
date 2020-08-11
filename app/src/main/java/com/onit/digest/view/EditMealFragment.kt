package com.onit.digest.view

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.forEach
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionInflater
import com.google.android.material.snackbar.Snackbar
import com.onit.digest.DigestApplication
import com.onit.digest.R
import com.onit.digest.viewmodel.EditMealViewModel

/**
 * [Fragment] for editing an existing meal with ingredients or creating a new one.
 */
class EditMealFragment : Fragment() {

    private lateinit var viewModel: EditMealViewModel
    private lateinit var menuItemSave: MenuItem
    private lateinit var etMealName: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var bnAddIngredient: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Animate on entry
        sharedElementEnterTransition =
            TransitionInflater.from(context).inflateTransition(android.R.transition.move)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_edit_meal, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val safeArgs: EditMealFragmentArgs by navArgs()

        val activity = requireActivity() as AppCompatActivity
        val application = activity.application as DigestApplication

        viewModel = ViewModelProvider(
            this,
            EditMealViewModel.Factory(application, safeArgs.selectedMeal, application.getMealRepository())
        ).get(EditMealViewModel::class.java)

        val view = requireView()

        // Set action bar title
        activity.supportActionBar?.title = viewModel.actionBarTitle

        etMealName = view.findViewById(R.id.et_meal_name)
        etMealName.addTextChangedListener(onTextChanged = { text, _, _, _ ->
            // Update model when meal name changes
            viewModel.onMealNameChanged(text?.toString() ?: "")
        })
        // Observe meal name changes (two-way binding so only update if changed)
        viewModel.mealName.observe(viewLifecycleOwner, Observer { mealName ->
            if (mealName != etMealName.text.toString()) {
                etMealName.setText(mealName)
            }
        })

        recyclerView = view.findViewById(R.id.rv_ingredients)
        recyclerView.layoutManager = LinearLayoutManager(view.context)
        bnAddIngredient = view.findViewById(R.id.bn_add_ingredient)

        // Observe snackbar text and display
        viewModel.snackbar.observe(viewLifecycleOwner, Observer { text ->
            text?.let {
                Snackbar.make(requireView(), text, Snackbar.LENGTH_SHORT).show()
                viewModel.onSnackbarShown()
            }
        })

        // Observe isFinished (when user has exited or saved a meal) and go back
        viewModel.isFinished.observe(viewLifecycleOwner, Observer { isFinished ->
            if (isFinished) {
                findNavController().popBackStack()
            }
        })

        // Postpone animation until data is ready
        postponeEnterTransition()
        // Get all ingredient names for autocomplete dropdown and setup adapter
        viewModel.allIngredientsName.observe(viewLifecycleOwner, object : Observer<List<String>> {
            override fun onChanged(ingredientNames: List<String>) {
                // observe once only
                viewModel.allIngredientsName.removeObserver(this)
                // Setup adapter
                val adapter =
                    EditIngredientAdapter(
                        viewModel.editIngredients,
                        ingredientNames.toTypedArray()
                    )
                recyclerView.adapter = adapter
                bnAddIngredient.setOnClickListener {
                    adapter.onAddIngredientClick()
                }
                // Start the animation
                startPostponedEnterTransition()
                (activity as? MainActivity)?.hideBottomNav()
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.edit_meal_menu, menu)
        menuItemSave = menu.findItem(R.id.menu_item_save)
        menuItemSave.setOnMenuItemClickListener {
            onSaveMealClick()
            return@setOnMenuItemClickListener true
        }
        // Observe the isLoading state and update the UI accordingly
        viewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            menuItemSave.isEnabled = !isLoading
            if (isLoading) {
                menuItemSave.setActionView(R.layout.actionview_progress)
            } else {
                menuItemSave.actionView = null
            }
            etMealName.isEnabled = !isLoading
            bnAddIngredient.isEnabled = !isLoading
            recyclerView.forEach { setViewAndChildrenEnabled(it, !isLoading) }
        })
    }

    private fun setViewAndChildrenEnabled(view: View, enabled: Boolean) {
        view.isEnabled = enabled
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val childView = view.getChildAt(i)
                setViewAndChildrenEnabled(childView, enabled)
            }
        }
    }

    private fun onSaveMealClick() {
        viewModel.onSaveMealClick()
    }

    override fun onPause() {
        super.onPause()
        // Make sure keyboard is closed
        view?.windowToken?.let {
            val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            imm?.hideSoftInputFromWindow(it, 0)
        }
    }
}
