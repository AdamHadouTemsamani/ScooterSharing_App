package dk.itu.moapd.scootersharing.ahad.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.WindowCompat
import dk.itu.moapd.scootersharing.ahad.R
import dk.itu.moapd.scootersharing.ahad.fragments.UpdateRideFragment
import dk.itu.moapd.scootersharing.ahad.databinding.ActivityUpdateRideBinding

class UpdateRideActivity : AppCompatActivity() {
    private lateinit var mainBinding: ActivityUpdateRideBinding

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
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        mainBinding = ActivityUpdateRideBinding.inflate(layoutInflater)

        setContentView(mainBinding.root)
        val updateRideFragment = UpdateRideFragment()

        supportFragmentManager
            .beginTransaction()
            .add(R.id.fragment_container_view,updateRideFragment)
            .commit()
    }
}
