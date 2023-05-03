package dk.itu.moapd.scootersharing.ahad.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import dk.itu.moapd.scootersharing.ahad.application.ScooterApplication
import dk.itu.moapd.scootersharing.ahad.databinding.FragmentBalanceBinding
import dk.itu.moapd.scootersharing.ahad.databinding.FragmentCardBinding
import dk.itu.moapd.scootersharing.ahad.databinding.FragmentQrscannerBinding
import dk.itu.moapd.scootersharing.ahad.model.UserBalance
import dk.itu.moapd.scootersharing.ahad.model.UserBalanceViewModel
import dk.itu.moapd.scootersharing.ahad.model.UserBalanceViewModelFactory

class CardFragment : Fragment() {

    companion object {
        private val TAG = BalanceFragment.javaClass::class.java.simpleName
    }

    private var _binding: FragmentCardBinding? = null

    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    private lateinit var auth: FirebaseAuth

    private val userBalanceViewModel: UserBalanceViewModel by activityViewModels() {
        UserBalanceViewModelFactory((requireActivity().application as ScooterApplication).userRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            addCardButton.setOnClickListener {
                if (editTextName.editText?.text.toString().isNotEmpty() &&
                    editTextCardnumber.editText?.text.toString().isNotEmpty() &&
                    editTextExpire.editText?.text.toString().isNotEmpty() &&
                    editTextCvv.editText?.text.toString().isNotEmpty()) {

                    userBalanceViewModel.users.observe(viewLifecycleOwner) {
                        if (it.isNotEmpty()) {
                            for (user in it) {
                                    if(user.email == auth.currentUser!!.email) {
                                        user.isCard = true
                                        userBalanceViewModel.update(user)
                                        requireActivity().supportFragmentManager
                                            .popBackStack()
                                        requireActivity().supportFragmentManager
                                            .popBackStack()
                                    }
                            }
                        } else {
                            val user = auth.currentUser?.email?.let { email ->
                                UserBalance(0,
                                    email,
                                    0.0,
                                true)
                            }
                            if (user != null) {
                                userBalanceViewModel.insert(user)
                                requireActivity().supportFragmentManager
                                    .popBackStack()
                                requireActivity().supportFragmentManager
                                    .popBackStack()
                            }
                        }

                    }
                }

            }
        }
    }


}