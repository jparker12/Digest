package com.onit.digest

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
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
            { mealWithIngredients ->
                val directions = MealsFragmentDirections.editMealAction(mealWithIngredients)
                findNavController().navigate(directions)
            }
        )
        recyclerView.adapter = mealsAdapter

        val fabAddMeal: FloatingActionButton = view.findViewById(R.id.fab_add_meal)
        fabAddMeal.setOnClickListener {
            val directions = MealsFragmentDirections.editMealAction()
            findNavController().navigate(directions)
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

        // Observe changes in MealWithIngredients list
        viewModel.allMeals.observe(viewLifecycleOwner, Observer { allMeals ->
            mealsAdapter.submitList(allMeals)
        })
        // Observe changes in expanded meal items to show ingredients in the recycler view
        viewModel.expandedMealIds.observe(viewLifecycleOwner, Observer { expandedMealIds ->
            mealsAdapter.setExpandedMealIds(expandedMealIds)
        })
    }
}
