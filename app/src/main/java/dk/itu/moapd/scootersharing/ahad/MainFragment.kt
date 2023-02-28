package dk.itu.moapd.scootersharing.ahad

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import dk.itu.moapd.scootersharing.ahad.databinding.ActivityMainBinding
import dk.itu.moapd.scootersharing.ahad.databinding.ContentLayoutBinding


/**
 * A fragment to show the `Main Fragment` tab
 */
class MainFragment : Fragment() {

    /**
     * View binding is a feature that allows you to more easily write code that interacts with
     * views. Once view binding is enabled in a module, it generates a binding class for each XML
     * layout file present in that module. An instance of a binding class contains direct references
     * to all views that have an ID in the corresponding layout.
     */
    private lateinit var mainBinding: ActivityMainBinding
    private lateinit var contentBinding: ContentLayoutBinding

    companion object {
        lateinit var ridesDB : RidesDB
        private lateinit var adapter: CustomArrayAdapter
    }

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

        // Singleton to share an object between the app activities.
        ridesDB = RidesDB.get(this)

        val data = ArrayList<Scooter>()

        // Create the custom adapter to populate a list of rides.
        adapter = CustomArrayAdapter(this, R.layout.list_rides, ridesDB.getRidesList())
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        mainBinding.listRides.adapter = adapter

        with (mainBinding) {
            startRideButton.setOnClickListener {
                val intent = Intent(baseContext, StartRideActivity::class.java)
                startActivity(intent)
            }

            updateRideButton.setOnClickListener {
                val intent = Intent(baseContext, UpdateRideActivity::class.java)
                startActivity(intent)
            }

            showRidesButton.setOnClickListener {
                listRides.visibility = if (listRides.visibility == View.VISIBLE){
                    View.INVISIBLE
                } else{
                    View.VISIBLE
                }

            }
        }
        setContentView(mainBinding.root)

    }

    override fun onRestart() {
        super.onRestart()
        adapter.notifyDataSetChanged()
    }
}