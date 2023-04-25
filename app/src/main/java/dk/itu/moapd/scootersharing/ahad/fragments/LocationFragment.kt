package dk.itu.moapd.scootersharing.ahad.fragments

import android.content.*
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.Looper
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.*
import dk.itu.moapd.scootersharing.ahad.MyLocationUpdateService
import dk.itu.moapd.scootersharing.ahad.databinding.FragmentLocationBinding
import java.text.SimpleDateFormat
import java.util.*


class LocationFragment : Fragment() {

    /**
     * A set of static attributes used in this fragment
     */
    companion object {
        private val TAG = LocationFragment::class.java.simpleName
        private const val ALL_PERMISSIONS_RESULT = 1011
    }


    // A reference to the service used to get location updates.
    private var mService: MyLocationUpdateService? = null

    // Tracks the bound state of the service.
    private var mBound = false

    private lateinit var broadcastReceiver: MyReceiver

    private var _binding: FragmentLocationBinding? = null

    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }


    /**
     * This callback is called when `FusedLocationProviderClient` has a new `Location`.
     */
    private lateinit var locationCallback: LocationCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG,"I have made le receiver merci beacoup")
        broadcastReceiver = MyReceiver()
        if(checkPermission()) requestUserPermissions()

        context?.let { LocalBroadcastManager.getInstance(it).registerReceiver(
            broadcastReceiver, IntentFilter(MyLocationUpdateService.ACTION_BROADCAST)
        ) }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLocationBinding.inflate(inflater, container, false)
        // Define the recycler view layout manager and adapter
        return binding.root
    }

    override fun onStart() {
        Log.i(TAG,"I am starting xD")
        super.onStart()

        if(checkPermission()) requestUserPermissions()
        mService?.requestLocationUpdates();

        context?.bindService(
            Intent(context, MyLocationUpdateService::class.java), mServiceConnection,
            Context.BIND_AUTO_CREATE
        )

        context?.let { LocalBroadcastManager.getInstance(it).registerReceiver(
            broadcastReceiver, IntentFilter(MyLocationUpdateService.ACTION_BROADCAST)
        ) }

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        Log.i(TAG,"I am resummeignng")
        context?.let { LocalBroadcastManager.getInstance(it).registerReceiver(
            broadcastReceiver, IntentFilter(MyLocationUpdateService.ACTION_BROADCAST)

        ) }

        Log.i(TAG,"I am resummeignng 2")
        context?.startService(Intent(context,MyLocationUpdateService::class.java))
        //subscribeToLocationUpdates()
    }

    override fun onPause() {
        Log.i(TAG,"I am peeeeeingg")
        super.onPause()
        context?.let { LocalBroadcastManager.getInstance(it).unregisterReceiver(broadcastReceiver) }
        //unsubscribeToLocationUpdates()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        if (mBound) {
            context?.unbindService(mServiceConnection)
            mBound = false
        }
        context?.stopService(Intent(context,MyLocationUpdateService::class.java))

        _binding = null
    }

    /*
    private fun startLocationAware() {
        // Show a dialog to ask the user to allow the application to access the device's location.


        // Start receiving location updates.
        fusedLocationProviderClient = context?.let {
            LocationServices
                .getFusedLocationProviderClient(it)
        }!!

        // Initialize the `LocationCallback`.
        locationCallback = object : LocationCallback() {

            /**
             * This method will be executed when `FusedLocationProviderClient` has a new location.
             *
             * @param locationResult The last known location.
             */
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                // Updates the user interface components with GPS data location.
                    locationResult.lastLocation?.let { location ->
                        binding.latitudeTextField.editText?.setText(location.latitude.toString())
                        binding.longtitudeTextField.editText?.setText(location.longitude.toString())
                    }
                }
            }
    }

     */




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
            checkSelfPermission(
                it, android.Manifest.permission.ACCESS_FINE_LOCATION
            )
        } != PackageManager.PERMISSION_GRANTED &&
                context?.let {
                    checkSelfPermission(
                        it, android.Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                } != PackageManager.PERMISSION_GRANTED

    private fun permissionsToRequest(permissions: ArrayList<String>): ArrayList<String> {
        val result: ArrayList<String> = ArrayList()
        for (permission in permissions)
            if (context?.let { PermissionChecker.checkSelfPermission(it, permission) } != PackageManager.PERMISSION_GRANTED)
                result.add(permission)

    return result
    }


    /*
    /**
     * Subscribes this application to get the location changes via the `locationCallback()`.
     */
    private fun subscribeToLocationUpdates() {

        // Check if the user allows the application to access the location-aware resources.
        if (checkPermission())
            return

        // Sets the accuracy and desired interval for active location updates.
        val locationRequest = LocationRequest
            .Builder(Priority.PRIORITY_HIGH_ACCURACY, 5)
            .build()

        // Subscribe to location changes.
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest, locationCallback, Looper.getMainLooper()
        )
    }


    /**
     * Unsubscribes this application of getting the location changes from  the `locationCallback()`.
     */
    private fun unsubscribeToLocationUpdates() {
        // Unsubscribe to location changes.
        fusedLocationProviderClient
            .removeLocationUpdates(locationCallback)
    }
    */

    private inner class MyReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val location: Location? =
                intent.extras?.getParcelable(MyLocationUpdateService.EXTRA_LOCATION)
            if (location != null) {
                Log.i(TAG,location.latitude.toString())
                Log.i(TAG,location.longitude.toString())
                binding.latitudeTextField.editText?.setText(location.latitude.toString())
                binding.longtitudeTextField.editText?.setText(location.longitude.toString())
                setAddress(location.latitude,location.longitude)
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

    /**
     * Return the timestamp as a `String`.
     *
     * @return The timestamp formatted as a `String` using the default locale.
     */
    private fun Long.toDateString() : String {
        val date = Date(this)
        val format = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        return format.format(date)
    }

    private fun Address.toAddressString() : String {
        val address = this
        val stringBuilder = StringBuilder()
        stringBuilder.apply {
            append(address.getAddressLine(0))
            append(address.locality)
            append(address.postalCode)
            append(address.countryName)
        }
        return stringBuilder.toString()
    }

    private fun setAddress(latitude: Double, longitude: Double) {
        val geocoder = context?.let { Geocoder(it, Locale.getDefault()) }
        val geocodeListener = Geocoder.GeocodeListener { addresses ->
            addresses.firstOrNull()?.toAddressString()?.let {address ->
                binding.addressTextField.editText?.setText(address)

            }

        }
        if (Build.VERSION.SDK_INT >= 33)
            geocoder?.getFromLocation(latitude, longitude, 1, geocodeListener)
        else
            geocoder?.getFromLocation(latitude, longitude, 1)?.let { addresses ->
                addresses.firstOrNull()?.toAddressString()?.let { address ->
                    binding.addressTextField.editText?.setText(address)
                }
            }
    }

}