package dk.itu.moapd.scootersharing.ahad.fragments

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dk.itu.moapd.scootersharing.ahad.MyLocationUpdateService
import dk.itu.moapd.scootersharing.ahad.R
import dk.itu.moapd.scootersharing.ahad.application.ScooterApplication
import dk.itu.moapd.scootersharing.ahad.databinding.FragmentStartRideBinding
import dk.itu.moapd.scootersharing.ahad.model.Scooter
import dk.itu.moapd.scootersharing.ahad.model.ScooterViewModel
import dk.itu.moapd.scootersharing.ahad.model.ScooterViewModelFactory
import dk.itu.moapd.scootersharing.ahad.utils.UserPermissions
import java.util.*

class StartRideFragment : Fragment() {


    companion object {
        private val TAG = StartRideFragment::class.java.simpleName
        private const val ALL_PERMISSIONS_RESULT = 1011
    }

    //Binding
    private var _binding: FragmentStartRideBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }
    private lateinit var currentScooter: Scooter

    //Used for location-aware service
    private var currentLocation: Location? = null
    // A reference to the service used to get location updates
    private var mService: MyLocationUpdateService? = null
    // Tracks the bound state of the service.
    private var mBound = false
    // BroadcastReceiver that gets data from service
    private lateinit var broadcastReceiver: MyReceiver
    // Class that requests for Location-Aware permissions

    private val scooterViewModel: ScooterViewModel by viewModels {
        ScooterViewModelFactory((requireActivity().application as ScooterApplication).scooterRepository)
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
        //Initialize broadcastReceiver
        broadcastReceiver = MyReceiver()
        if(checkPermission()) requestUserPermissions()

        mService?.requestLocationUpdates();

        context?.bindService(
            Intent(context, MyLocationUpdateService::class.java), mServiceConnection,
            Context.BIND_AUTO_CREATE
        )

        context?.let { LocalBroadcastManager.getInstance(it).registerReceiver(
            broadcastReceiver, IntentFilter(MyLocationUpdateService.ACTION_BROADCAST)
        ) }

        context?.startService(Intent(context, MyLocationUpdateService::class.java))
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

        with(binding) {
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

            scanScooterButton.setOnClickListener {
                if(!isLocationNull()) {
                    Log.i(TAG, "I am sending the following data: " +currentLocation?.latitude.toString())
                    val fragment = QrcodeFragment()
                    val args = Bundle()
                    args.putString("currentLat",currentLocation!!.latitude.toString())
                    args.putString("currentLong",currentLocation!!.longitude.toString())
                    fragment.arguments = args
                    requireActivity().supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.fragment_container_view,fragment)
                        .addToBackStack(null)
                        .commit()
                } else {
                    showMessage("Your location is currently null. Please exit back to the main menu and try again!")
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        context?.let { LocalBroadcastManager.getInstance(it).registerReceiver(
            broadcastReceiver, IntentFilter(MyLocationUpdateService.ACTION_BROADCAST)
        ) }

        if(currentLocation != null) {
            val args = Bundle()
            args.putDouble("currentLat",currentLocation!!.latitude)
            args.putDouble("currentLong",currentLocation!!.longitude)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        if (mBound) {
            context?.unbindService(mServiceConnection)
            mBound = false
        }
        context?.stopService(Intent(context,MyLocationUpdateService::class.java))
    }
    /**
     * Displays the Scooter information using a Snackbar
     */
    private fun showMessage(message: String) {
        //Snackbar :D
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
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
            if ( editTextName.editText?.text.toString().isNotEmpty()) {
                if(currentLocation == null) {
                    showMessage("Your location is currently null. Please go back to Main Screen and try again!")
                    return
                }
                //Update the object attributes
                val name = editTextName.editText?.text.toString().trim()
                val location = "Something went wrong?"
                val date = Calendar.getInstance().time.minutes.toLong()
                val startLocation = Pair(currentLocation!!.longitude,currentLocation!!.latitude)
                val endLocation = Pair(currentLocation!!.longitude,currentLocation!!.latitude)
                val scooter = Scooter(0,name,location,date,date,
                    startLocation.first,
                    startLocation.second,
                    endLocation.first,
                    endLocation.second,
                    true)
                    scooter.URL = name + ".jpg"
                scooterViewModel.insert(scooter)

                //Reset the text fields and update the UI
                editTextName.editText?.text?.clear()
                showMessage("Ride started!")
            }
        }
    }



    private fun requestUserPermissions() {
        //An array with permissions.
        val permissions: ArrayList<String> = ArrayList()
        permissions.add(android.Manifest.permission.ACCESS_FINE_LOCATION)
        permissions.add(android.Manifest.permission.ACCESS_COARSE_LOCATION)

        //Check which permissions is needed to ask to the user.
        val permissionsToRequest = permissionsToRequest(permissions)

        //Show the permissions dialogue to the user.
        if (permissionsToRequest.size > 0)
            requestPermissions(
                permissionsToRequest.toTypedArray(),
                ALL_PERMISSIONS_RESULT
            )
    }

    private fun checkPermission() =
        context?.let {
            ContextCompat.checkSelfPermission(
                it, Manifest.permission.ACCESS_FINE_LOCATION
            )
        } != PackageManager.PERMISSION_GRANTED &&
                context?.let {
                    ContextCompat.checkSelfPermission(
                        it, Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                } != PackageManager.PERMISSION_GRANTED

    private fun permissionsToRequest(permissions: ArrayList<String>): ArrayList<String> {
        val result: ArrayList<String> = ArrayList()
        for (permission in permissions)
            if (context?.let { PermissionChecker.checkSelfPermission(it, permission) } != PackageManager.PERMISSION_GRANTED)
                result.add(permission)

        return result
    }

    private inner class MyReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val location: Location? =
                intent.extras?.getParcelable(MyLocationUpdateService.EXTRA_LOCATION)
            if (location != null) {
                Log.i(TAG,location.latitude.toString())
                Log.i(TAG,location.longitude.toString())
                currentLocation = location
            }
            if(location == null) Log.i(TAG,"There is no location dumbass")
        }

    }

    // Monitors the state of the connection to the service.
    private val mServiceConnection: ServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder: MyLocationUpdateService.LocalBinder = service as MyLocationUpdateService.LocalBinder
            mService = binder.getService()
            mBound = true
            Log.i(TAG,"Yes work, service yes yes")
        }

        override fun onServiceDisconnected(name: ComponentName) {
            mService = null
            mBound = false
            Log.i(TAG,"no work, service no no")
        }
    }

    private fun isLocationNull() : Boolean {
        return currentLocation == null
    }









}