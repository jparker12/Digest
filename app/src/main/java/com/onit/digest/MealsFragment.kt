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

        val recyclerView: RecyclerView = requireView().findViewById(R.id.rv_meals)
        mealsAdapter = MealsAdapter(
            { mealWithIngredients ->
                viewModel.onMealItemExpandToggle(mealWithIngredients)
            },
            { mealWithIngredients ->
                val action = MealsFragmentDirections.editMealAction()
                findNavController().navigate(action)
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = mealsAdapter

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
