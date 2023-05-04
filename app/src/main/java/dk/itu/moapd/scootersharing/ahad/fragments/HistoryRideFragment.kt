package dk.itu.moapd.scootersharing.ahad.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import dk.itu.moapd.scootersharing.ahad.adapters.HistoryRideAdapter
import dk.itu.moapd.scootersharing.ahad.application.ScooterApplication
import dk.itu.moapd.scootersharing.ahad.databinding.FragmentHistoryRideBinding
import dk.itu.moapd.scootersharing.ahad.model.HistoryViewModel

//This is the Fragment responsible for showing the previously active scooters.
class HistoryRideFragment : Fragment() {

    companion object {
        private lateinit var adapter: HistoryRideAdapter
    }

    //Binding that contains reference to root view.
    private var _binding: FragmentHistoryRideBinding? = null

    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    //Create viewModel to access the database table for History (previously active rides).
    private val historyViewModel: HistoryViewModel by viewModels {
        HistoryViewModel.HistoryViewModelFactory((requireActivity().application as ScooterApplication).historyRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //Inflate layout from binding
        _binding = FragmentHistoryRideBinding.inflate(inflater, container, false)
        // Define the recycler view layout manager and adapter
        binding.listPreviousRides.layoutManager = LinearLayoutManager(activity)
        // Initialize Adapter to display the previously active rides.
        adapter = HistoryRideAdapter()
        //Gets the previously active rides from the database
        historyViewModel.previousRides.observe(viewLifecycleOwner) { previousRides ->
            previousRides?.let {
                //These are then added to the Adapter, so they can display in the RecyclerView
                adapter.submitList(it)
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Makes layout manager for our Recycler view.
        binding.listPreviousRides.layoutManager = LinearLayoutManager(activity)
        binding.listPreviousRides.addItemDecoration(
            DividerItemDecoration(activity, DividerItemDecoration.VERTICAL)
        )
        binding.listPreviousRides.adapter = adapter
    }

    override fun onDestroyView() {
        //Destroys the view and binding
        super.onDestroyView()
        _binding = null
    }
}