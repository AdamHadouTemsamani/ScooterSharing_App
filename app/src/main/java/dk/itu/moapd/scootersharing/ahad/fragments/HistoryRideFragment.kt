package dk.itu.moapd.scootersharing.ahad.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import dk.itu.moapd.scootersharing.ahad.adapters.CustomAdapter
import dk.itu.moapd.scootersharing.ahad.adapters.HistoryRideAdapter
import dk.itu.moapd.scootersharing.ahad.application.ScooterApplication
import dk.itu.moapd.scootersharing.ahad.databinding.FragmentHistoryRideBinding
import dk.itu.moapd.scootersharing.ahad.databinding.FragmentMainBinding
import dk.itu.moapd.scootersharing.ahad.databinding.FragmentStartRideBinding
import dk.itu.moapd.scootersharing.ahad.model.HistoryViewModel
import dk.itu.moapd.scootersharing.ahad.model.ScooterViewModel
import dk.itu.moapd.scootersharing.ahad.model.HistoryViewModel.HistoryViewModelFactory

class HistoryRideFragment : Fragment() {

    companion object {
        private lateinit var adapter: HistoryRideAdapter
    }

    private var _binding: FragmentHistoryRideBinding? = null

    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    private val historyViewModel: HistoryViewModel by viewModels {
        HistoryViewModelFactory((requireActivity().application as ScooterApplication).historyRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryRideBinding.inflate(inflater, container, false)
        // Define the recycler view layout manager and adapter
        binding.listPreviousRides.layoutManager = LinearLayoutManager(activity)
        // Collecting data from the dataset.
        adapter = HistoryRideAdapter()
        historyViewModel.previousRides.observe(viewLifecycleOwner) { previousRides ->
            previousRides?.let {
                adapter.submitList(it)
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.listPreviousRides.layoutManager = LinearLayoutManager(activity)
        binding.listPreviousRides.addItemDecoration(
            DividerItemDecoration(activity, DividerItemDecoration.VERTICAL)
        )
        binding.listPreviousRides.adapter = adapter
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}