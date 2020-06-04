package com.onit.digest

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.onit.digest.viewmodel.MealsViewModel

/**
 * A simple [Fragment] subclass.
 */
class MealsFragment : Fragment() {

    private lateinit var viewModel: MealsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_meals, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val recyclerView: RecyclerView = requireView().findViewById(R.id.rv_meals)
        val adapter = MealsAdapter()
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        viewModel = ViewModelProvider(
            requireActivity(),
            MealsViewModel.Factory(requireActivity().application)
        ).get(MealsViewModel::class.java)
        viewModel.allMeals.observe(viewLifecycleOwner, Observer { allMeals ->
            adapter.submitList(allMeals)
        })
    }
}
