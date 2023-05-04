package dk.itu.moapd.scootersharing.ahad.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import dk.itu.moapd.scootersharing.ahad.application.ScooterApplication
import dk.itu.moapd.scootersharing.ahad.databinding.FragmentCardBinding
import dk.itu.moapd.scootersharing.ahad.model.CardViewModel
import dk.itu.moapd.scootersharing.ahad.model.UserBalance
import dk.itu.moapd.scootersharing.ahad.model.UserBalanceViewModel
import dk.itu.moapd.scootersharing.ahad.model.UserBalanceViewModelFactory

//This is Fragment responsible for adding a card for the User in the database.
class CardFragment : Fragment() {

    companion object {
        private val TAG = BalanceFragment.javaClass::class.java.simpleName
    }

    //Binding that contains reference to root view.
    private var _binding: FragmentCardBinding? = null

    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    //Firebase Authentication
    private lateinit var auth: FirebaseAuth

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
        //Inflate layout from binding
        _binding = FragmentCardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            addCardButton.setOnClickListener {
                //Checks if user has written something in all text fields.
                //If yes the isCard field in the database, should be set to true.
                if (editTextName.editText?.text.toString().isNotEmpty() &&
                    editTextCardnumber.editText?.text.toString().isNotEmpty() &&
                    editTextExpire.editText?.text.toString().isNotEmpty() &&
                    editTextCvv.editText?.text.toString().isNotEmpty()
                ) {
                    //Get the appropriate information from the database and update the local fields
                    userBalanceViewModel.users.observe(viewLifecycleOwner) {
                        //Checks whether the users exists in the database. If yes update the field.
                        if (it.isNotEmpty()) {
                            for (user in it) {
                                if (user.email == auth.currentUser!!.email) {
                                    user.isCard = true
                                    userBalanceViewModel.update(user)
                                    //When it has updated the user in the database, it pops back to the Main Fragment.
                                    requireActivity().supportFragmentManager
                                        .popBackStack()
                                    requireActivity().supportFragmentManager
                                        .popBackStack()
                                }
                            }
                        } else {
                            //If there isn't any users added to the database. Insert them.
                            val user = auth.currentUser?.email?.let { email ->
                                UserBalance(
                                    0,
                                    email,
                                    0.0,
                                    true
                                )
                            }
                            if (user != null) {
                                userBalanceViewModel.insert(user)
                                //Pops back to the Main Fragment
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