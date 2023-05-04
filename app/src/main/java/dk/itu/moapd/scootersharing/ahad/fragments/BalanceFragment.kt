package dk.itu.moapd.scootersharing.ahad.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import dk.itu.moapd.scootersharing.ahad.R
import dk.itu.moapd.scootersharing.ahad.application.ScooterApplication
import dk.itu.moapd.scootersharing.ahad.databinding.FragmentBalanceBinding
import dk.itu.moapd.scootersharing.ahad.model.UserBalance
import dk.itu.moapd.scootersharing.ahad.model.UserBalanceViewModel
import dk.itu.moapd.scootersharing.ahad.model.UserBalanceViewModelFactory

//This is the Fragment responsible for handling the Balance of the User
class BalanceFragment : Fragment() {

    companion object {
        private val TAG = BalanceFragment.javaClass::class.java.simpleName
    }

    //Firebase Authentication
    private lateinit var auth: FirebaseAuth

    //Local fields with the users current balance + the currentUser
    private var userBalance: Double = 0.0
    private lateinit var currentUser: UserBalance

    //Binding that contains reference to root view.
    private var _binding: FragmentBalanceBinding? = null

    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    //Create viewModel to access the database table for UserBalance.
    private val userBalanceViewModel: UserBalanceViewModel by activityViewModels() {
        UserBalanceViewModelFactory((requireActivity().application as ScooterApplication).userRepository)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Gets an instance of Firebase Authenication
        auth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //Checks our database for current user.
        userBalanceViewModel.users.observe(viewLifecycleOwner) {
            //If an user already exists in the database
            if (it.isNotEmpty()) {
                for (user in it) {
                    //If the current users email is corrects and if they have added their card.
                    //If they have, navigate to the CardFragment
                    if (user.email == auth.currentUser?.email && !user.isCard) {
                        Log.i(TAG, "I am checking their card and it is: ${user.isCard}")
                        changeFragment()
                    }
                }
            }
            //If the current users list is empty, there is obviously no users the database.
            //Therefor change to CardFragment
            if (it.isEmpty()) {
                changeFragment()
            }
        }

        //Inflate layout from binding
        _binding = FragmentBalanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            //Get the appropriate information from the database and update the local fields.
            userBalanceViewModel.users.observe(viewLifecycleOwner) {
                if (it.isNotEmpty()) {
                    for (user in it) {
                        if (user.email == auth.currentUser?.email) {
                            userBalance = user.balance!!
                            currentUser = user
                        }
                    }
                }
            }

            addCurrencyButton.setOnClickListener {
                //Input fields used for our Alert Dialogue box.
                var input = EditText(requireContext())
                input.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )

                //Alert Dialogue to confirm whether the user wants to add the given amount to their balance.
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("How much would you like to top up with?")
                    .setView(input) //EditText (input field) is added here
                    .setPositiveButton("Accept") { dialog, which ->
                        //We get the value from the input field and update the UI and database accordingly.
                        val value = input.text.toString().toDoubleOrNull()
                        if (input.text.isNotEmpty() && value != null) {
                            currentUser.balance = updateBalance(value)
                            userBalanceViewModel.update(currentUser)
                        } else {
                            //If the user hasn't entered anything. We show a Snackbar.
                            showMessage("Please set the amount of money you would like to top up with!")
                        }
                    }
                    .setNegativeButton("Decline") { dialog, which ->
                        //Do nothing if the user declines.
                    }.show()
            }

            removeCardButton.setOnClickListener {
                //We get the the correct user from the database and update their information
                //in the database.
                userBalanceViewModel.users.observe(viewLifecycleOwner) {
                    if (it.isNotEmpty()) {
                        for (user in it) {
                            if (user.email == auth.currentUser?.email) {
                                user.isCard = false
                                userBalanceViewModel.update(user)
                                showMessage("Your card has successfully been removed")
                                requireActivity().supportFragmentManager
                                    .popBackStack()
                            }
                        }
                    }

                }
            }
            //Update the TextView with the balance that the user has.
            userBalanceViewModel.users.observe(viewLifecycleOwner) {
                if (it.isNotEmpty()) {
                    for (user in it) {
                        if (user.email == auth.currentUser?.email && user.isCard) {
                            currentBalanceText.text = user.balance.toString() + " Kr."
                        }
                    }
                }

            }
        }
    }

    //Used to more easily make a snackbox.
    private fun showMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    //Changes to the
    fun changeFragment() {
        val fragment = CardFragment()
        requireActivity().supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container_view, fragment)
            .addToBackStack(null)
            .commit()
    }

    //Updates the local field userBalance with the given value
    fun updateBalance(value: Double): Double {
        userBalance += value
        return userBalance
    }

}