package dk.itu.moapd.scootersharing.ahad.fragments

import androidx.fragment.app.Fragment
import dk.itu.moapd.scootersharing.ahad.databinding.FragmentStartRideBinding

class HistoryRideFragment : Fragment() {

    private var _binding: FragmentStartRideBinding? = null

    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }



}