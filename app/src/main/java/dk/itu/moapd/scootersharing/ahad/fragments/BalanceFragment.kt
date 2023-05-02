package dk.itu.moapd.scootersharing.ahad.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import dk.itu.moapd.scootersharing.ahad.R
import dk.itu.moapd.scootersharing.ahad.application.ScooterApplication
import dk.itu.moapd.scootersharing.ahad.databinding.FragmentBalanceBinding
import dk.itu.moapd.scootersharing.ahad.databinding.FragmentQrscannerBinding
import dk.itu.moapd.scootersharing.ahad.model.*

class BalanceFragment : Fragment() {

    companion object {
        private val TAG = BalanceFragment.javaClass::class.java.simpleName
    }

    private val userBalanceViewModel: UserBalanceViewModel by viewModels {
        UserBalanceViewModelFactory((requireActivity().application as ScooterApplication).userRepository)
    }

    private lateinit var auth: FirebaseAuth

    private var userBalance: Double = 0.0
    private lateinit var currentUser: UserBalance

    private var _binding: FragmentBalanceBinding? = null

    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        Log.i(TAG,"Firebase Email: " + auth.currentUser?.email)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        userBalanceViewModel.users.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                for (user in it) {
                    if (user.email == auth.currentUser?.email) {
                        binding.currentBalanceText.text = user.balance.toString() + " Kr."
                    } else {
                        val fragment = CardFragment()
                        requireActivity().supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.fragment_container_view,fragment)
                            .addToBackStack(null)
                            .commit()
                    }
                }
            }
            if (it.isEmpty()) {
                val fragment = CardFragment()
                requireActivity().supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_container_view,fragment)
                    .addToBackStack(null)
                    .commit()
            }
        }

        _binding = FragmentBalanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            userBalanceViewModel.users.observe(viewLifecycleOwner) {
                if(it.isNotEmpty()) {
                    for(user in it) {
                        if(user.email == auth.currentUser?.email) {
                            userBalance = user.balance!!
                            currentUser = user
                        }
                    }
                }
            }

            addCurrencyButton.setOnClickListener {

                var input = EditText(requireContext())
                input.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )

                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("How much would you like to top up with?")
                    .setView(input)
                    .setPositiveButton("Accept") { dialog, which ->
                        val value = input.text.toString().toDoubleOrNull()

                        if(input.text.isNotEmpty() && value != null) {
                            currentUser.balance = updateBalance(value)
                            userBalanceViewModel.update(currentUser)
                        } else {
                            showMessage("Please set the amount of money you would like to top up with!")
                        }
                    }
                    .setNegativeButton("Decline") { dialog, which ->

                    }.show()
            }

            removeCardButton.setOnClickListener {
                userBalanceViewModel.users.observe(viewLifecycleOwner) {
                    if(it.isNotEmpty()) {
                        for(user in it) {
                            if(user.email == auth.currentUser?.email) {
                                userBalanceViewModel.delete(user)
                                showMessage("Your card has succesfully been removed")
                                requireActivity().supportFragmentManager
                                    .popBackStack()
                            }
                        }
                    }

                }
            }

            userBalanceViewModel.users.observe(viewLifecycleOwner) {
                if(it.isNotEmpty()) {
                    for(user in it) {
                        if(user.email == auth.currentUser?.email) {
                            currentBalanceText.text = user.balance.toString() + " Kr."
                        }
                    }
                }

            }
        }
    }

    private fun showMessage(message: String) {
        //Snackbar :D
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }


    fun updateBalance(value: Double) : Double {
        userBalance += value
        return userBalance
    }

}