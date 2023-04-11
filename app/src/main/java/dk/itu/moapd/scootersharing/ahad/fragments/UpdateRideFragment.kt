package dk.itu.moapd.scootersharing.ahad.fragments

import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dk.itu.moapd.scootersharing.ahad.application.ScooterApplication
import dk.itu.moapd.scootersharing.ahad.databinding.FragmentUpdateRideBinding
import dk.itu.moapd.scootersharing.ahad.model.ScooterViewModel
import dk.itu.moapd.scootersharing.ahad.model.ScooterViewModelFactory
import java.util.*

class UpdateRideFragment : Fragment() {

    private var _binding: FragmentUpdateRideBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    private val scooterViewModel: ScooterViewModel by viewModels {
        ScooterViewModelFactory((requireActivity().application as ScooterApplication).scooterRepository)
    }

    /**
     * An instance of the Scooter class that has all the information about the scooter
     */

    /**
     * Called when the activity is starting. This is where most initialization should go: calling
     * `setContentView(int)` to inflate the activity's UI, using `findViewById()` to
     * programmatically interact with widgets in the UI, calling
     * `managedQuery(android.net.Uri, String[], String, String[], String)` to retrieve cursors for
     * data being displayed, etc.
     *
     * You can call `finish()` from within this function, in which case `onDestroy()` will be
     * immediately called after `onCreate()` without any of the rest of the activity lifecycle
     * (`onStart()`, `onResume()`, onPause()`, etc) executing.
     *
     * <em>Derived classes must call through to the super class's implementation of this method. If
     * they do not, an exception will be thrown.</em>
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut
     * down then this Bundle contains the data it most recently supplied in `onSaveInstanceState()`.
     * <b><i>Note: Otherwise it is null.</i></b>
     */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUpdateRideBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with (binding) {
            editTextName.editText?.isEnabled = false

            updateRideButton.setOnClickListener { view ->
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)

                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Are you sure you want to update the ride")
                    .setNegativeButton("Decline") { dialog, which ->

                    }
                    .setPositiveButton("Accept") { dialog, which ->
                        addScooter()
                    }
                    .show()


            }
        }
    }

    /**
     * Displays the Scooter information using a Snackbar
     */
    private fun showMessage() {
        //Snackbar :D
        Snackbar.make(binding.root, "Scooter updated", Snackbar.LENGTH_LONG).show()
    }

    /**
     * By using ViewBinding updates the values of the Properties of Scooter class
     */
    private fun addScooter() {
        with (binding) {
            if ( editTextLocation.editText?.text.toString().isNotEmpty()) {
                //Update the object attributes
                val scooter = scooterViewModel.scooters.value?.last()
                scooter?.location = editTextLocation.editText?.text.toString().trim()
                scooter?.endTime = Calendar.getInstance().time.minutes.toLong()
                if (scooter != null) {
                    scooterViewModel.update(scooter)
                }
                //Reset the text fields and update the UI
                editTextName.editText?.text?.clear()
                editTextLocation.editText?.text?.clear()
                showMessage()
            }
        }
    }

}