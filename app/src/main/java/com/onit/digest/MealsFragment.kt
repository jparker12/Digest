package com.onit.digest

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.onit.digest.viewmodel.MealsViewModel

/**
 * A simple [Fragment] subclass.
 */
class MealsFragment : Fragment() {

    private lateinit var viewModel: MealsViewModel
    private lateinit var mealsAdapter: MealsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_meals, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = ViewModelProvider(
            requireActivity(),
            MealsViewModel.Factory(requireActivity().application)
        ).get(MealsViewModel::class.java)

        val view = requireView()

        val recyclerView: RecyclerView = view.findViewById(R.id.rv_meals)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        mealsAdapter = MealsAdapter(
            { mealWithIngredients ->
                viewModel.onMealItemExpandToggle(mealWithIngredients)
            },
            { mealWithIngredients, cvMeal ->
                viewModel.onEditMealClick(findNavController(), mealWithIngredients, cvMeal)
            }
        )
        recyclerView.adapter = mealsAdapter

        val fabAddMeal: FloatingActionButton = view.findViewById(R.id.fab_add_meal)
        fabAddMeal.setOnClickListener {
            viewModel.onAddMealClick(findNavController())
        }

        // Hide FAB on scroll down
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0 && fabAddMeal.isShown) {
                    fabAddMeal.hide()
                } else if (dy < 0 && !fabAddMeal.isShown) {
                    fabAddMeal.show()
                }
            }
        })

        ItemTouchHelper(object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val mealWithIngredients =
                    mealsAdapter.getMealWithIngredients(viewHolder.adapterPosition)
                viewModel.onMealArchive(mealWithIngredients)
            }
        }).attachToRecyclerView(recyclerView)

        // Observe changes in MealWithIngredients list
        viewModel.allMeals.observe(viewLifecycleOwner, Observer { allMeals ->
            mealsAdapter.submitList(allMeals)
        })
        viewModel.archivedMeal.observe(viewLifecycleOwner, Observer { mealWithIngredients ->
            if (mealWithIngredients != null) {
                viewModel.onSnackbarShown()
                Snackbar.make(view, R.string.meal_deleted, Snackbar.LENGTH_LONG)
                    .setAction(R.string.undo) {
                        viewModel.onArchivedMealUndo(mealWithIngredients)
                    }
                    .addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                            if (event != DISMISS_EVENT_ACTION) {
                                viewModel.onMealDelete(mealWithIngredients)
                            }
                        }
                    })
                    .show()
            }
        })
        // Observe changes in expanded meal items to show ingredients in the recycler view
        viewModel.expandedMealIds.observe(viewLifecycleOwner, Observer { expandedMealIds ->
            mealsAdapter.setExpandedMealIds(expandedMealIds)
        })

        // Ensures the pop animation works when exiting EditMealFragment
        postponeEnterTransition()
        recyclerView.viewTreeObserver.addOnPreDrawListener(object :
            ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                recyclerView.viewTreeObserver.removeOnPreDrawListener(this)
                startPostponedEnterTransition()
                return true
            }
        })
    }
}
