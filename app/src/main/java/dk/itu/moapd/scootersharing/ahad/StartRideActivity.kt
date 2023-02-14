package dk.itu.moapd.scootersharing.ahad

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.HapticFeedbackConstants
import androidx.core.view.WindowCompat
import com.google.android.material.snackbar.Snackbar
import dk.itu.moapd.scootersharing.ahad.databinding.ActivityStartRideBinding
import dk.itu.moapd.scootersharing.ahad.databinding.ContentLayoutBinding

class StartRideActivity : AppCompatActivity() {

    private lateinit var mainBinding: ActivityStartRideBinding
    private lateinit var contentBinding: ContentLayoutBinding

    /**
     * An instance of the Scooter class that has all the information about the scooter
     */
    private val scooter: Scooter = Scooter("","", System.currentTimeMillis())


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
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        mainBinding = ActivityStartRideBinding.inflate(layoutInflater)
        contentBinding = ContentLayoutBinding.bind(mainBinding.root)

        with (mainBinding) {
            startRideButton.setOnClickListener { view ->
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                addScooter()
            }
        }
        setContentView(mainBinding.root)
    }

    /**
     * Displays the Scooter information using a Snackbar
     */
    private fun showMessage() {
        //Snackbar :D
        Snackbar.make(mainBinding.root, scooter.toString(), Snackbar.LENGTH_LONG).show()
    }

    /**
     * By using ViewBinding updates the values of the Properties of Scooter class
     */
    private fun addScooter() {
        with (contentBinding) {
            if ( editTextName.text.isNotEmpty() && editTextLocation.text.isNotEmpty()) {
                //Update the object attributes
                val name = editTextName.text.toString().trim()
                val location = editTextLocation.text.toString().trim()
                scooter.name = name
                scooter.location = location

                //Reset the text fields and update the UI
                editTextName.text.clear()
                editTextLocation.text.clear()
                showMessage()
            }
        }
    }
}