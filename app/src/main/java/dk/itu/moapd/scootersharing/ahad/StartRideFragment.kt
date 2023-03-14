package dk.itu.moapd.scootersharing.ahad

import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dk.itu.moapd.scootersharing.ahad.databinding.ActivityStartRideBinding
import dk.itu.moapd.scootersharing.ahad.databinding.ContentLayoutBinding
import dk.itu.moapd.scootersharing.ahad.databinding.FragmentMainBinding
import dk.itu.moapd.scootersharing.ahad.databinding.FragmentStartRideBinding
import java.util.*

class StartRideFragment : Fragment() {

    private var _binding: FragmentStartRideBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    companion object {
        lateinit var ridesDB : RidesDB
    }

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
        // Singleton to share an object between the app activities.
        ridesDB = RidesDB.get(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStartRideBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with (binding) {
            startRideButton.setOnClickListener { view ->
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    /**
     * Displays the Scooter information using a Snackbar
     */
    private fun showMessage() {
        //Snackbar :D
        Snackbar.make(binding.root, "Scooterride started", Snackbar.LENGTH_LONG).show()
    }

    /* *
    * Generate a random timestamp in the last 365 days .
    *
    * @return A random timestamp in the last year .
    */
    private fun randomDate(): Long {
        val random = Random()
        val now = System.currentTimeMillis()
        val year = random.nextDouble() * 1000 * 60 * 60 * 24 * 365
        return (now - year).toLong()
    }

    /**
     * By using ViewBinding updates the values of the Properties of Scooter class
     */
    private fun addScooter() {
        with (binding) {
            if ( editTextName.editText?.text.toString().isNotEmpty() && editTextLocation.editText?.text.toString().isNotEmpty()) {
                //Update the object attributes
                val name = editTextName.editText?.text.toString().trim()
                val location = editTextLocation.editText?.text.toString().trim()
                ridesDB.addScooter(name, location, System.currentTimeMillis())

                //Reset the text fields and update the UI
                editTextName.editText?.text?.clear()
                editTextLocation.editText?.text?.clear()
                showMessage()
            }
        }
    }




}