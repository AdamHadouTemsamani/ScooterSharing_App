package dk.itu.moapd.scootersharing.ahad.fragments

import GeofenceBroadcastReceiver
import android.Manifest
import android.R.attr.radius
import android.annotation.TargetApi
import android.app.PendingIntent
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import dk.itu.moapd.scootersharing.ahad.MyLocationUpdateService
import dk.itu.moapd.scootersharing.ahad.application.ScooterApplication
import dk.itu.moapd.scootersharing.ahad.databinding.FragmentMapsBinding
import dk.itu.moapd.scootersharing.ahad.model.ScooterViewModel
import dk.itu.moapd.scootersharing.ahad.model.ScooterViewModelFactory
import dk.itu.moapd.scootersharing.ahad.utils.GeofenceHelper
import dk.itu.moapd.scootersharing.ahad.utils.IOnLoadLocationListener


class MapsFragment : Fragment(), OnMapReadyCallback {

    companion object {
        private val TAG = MapsFragment::class.java.simpleName
        private const val ALL_PERMISSIONS_RESULT = 1011
        private val GEOFENCE_RADIUS_IN_METERS = 3F
    }


    //Used for location-aware service
    private var currentLocation: Location? = null
    private var mService: MyLocationUpdateService? = null
    private var mBound = false

    // BroadcastReceiver that gets data from service
    private lateinit var broadcastReceiver: MyReceiver

    //Used for GeoFencing
    private lateinit var geoClient: GeofencingClient
    private lateinit var geofenceHelper: GeofenceHelper
    private lateinit var geofenceList: MutableList<Geofence>
    private val isAndroidQ = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q

    //ViewModel for getting the scooters that exist in the database
    private val scooterViewModel: ScooterViewModel by viewModels {
        ScooterViewModelFactory((requireActivity().application as ScooterApplication).scooterRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        geoClient = LocationServices.getGeofencingClient(requireActivity())
        geofenceHelper = GeofenceHelper(requireContext())

        broadcastReceiver = MyReceiver()

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

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(
            dk.itu.moapd.scootersharing.ahad.R.layout.fragment_maps,
            container,
            false
        )

        val mapFragment = childFragmentManager
            .findFragmentById(dk.itu.moapd.scootersharing.ahad.R.id.google_maps) as SupportMapFragment?

        mapFragment?.getMapAsync(this)


        if(isAndroidQ) {
            requestUserPermissions()
            requestUserPermissionsForAndroidQ()
        } else {
            requestUserPermissions()
        }

        return view
    }

    @TargetApi(Build.VERSION_CODES.Q)
    override fun onMapReady(googleMap: GoogleMap) {
        if (isAndroidQ && checkPermissionForAndroidQ() && checkPermission()) return
        if (checkPermission()) return
        Log.i(TAG,"Your location is currently null. " +
                "To fix this either return to previous fragment and try again. Or minimize the application and try again.")

        if(currentLocation == null) {
            Log.i(TAG,"Your location is null")
        }


        Log.i(TAG, "Map is now running")

        googleMap.clear()

        // Show the current device's location as a blue dot.
        googleMap.isMyLocationEnabled = true


        // Set the default map type.
        googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL

        // Setup the UI settings state.
        googleMap.uiSettings.apply {
            isCompassEnabled = true
            isIndoorLevelPickerEnabled = true
            isMyLocationButtonEnabled = true
            isRotateGesturesEnabled = true
            isScrollGesturesEnabled = true
            isTiltGesturesEnabled = true
            isZoomControlsEnabled = true
            isZoomGesturesEnabled = true
        }




        val itu = LatLng(55.6596, 12.5910)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(itu, 18f))

        geofenceList = mutableListOf()

        scooterViewModel.scooters.observe(viewLifecycleOwner) {
            for (ride in it) {
                Log.i(TAG, "Current scooter name:" + ride.name)
                googleMap.addMarker(
                    MarkerOptions()
                        .position(LatLng(ride.currentLat, ride.currentLong))
                        .title(ride.name)
                )
                ride.name?.let { it1 ->
                    addGeofence(
                        it1,LatLng(ride.currentLat,ride.currentLong))
                }

                googleMap.addCircle(CircleOptions()
                    .center(LatLng(ride.currentLat,ride.currentLong))
                    .radius(GEOFENCE_RADIUS_IN_METERS.toDouble())
                    .strokeColor(Color.argb(255, 238,130,238))
                    .fillColor(Color.argb(64, 238,130,238))
                    .strokeWidth(2f)
                )
                Log.i(TAG,"Should draw a circle wtf?")

            }
        }

        // Move the Google Maps UI buttons under the OS top bar.
        googleMap.setPadding(0, 100, 0, 0)
    }


    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
        intent.action = "MapsFragment.ACTION_GEOFENCE_EVENT"
        PendingIntent.getBroadcast(requireContext(),0,intent,PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    }

    @TargetApi(Build.VERSION_CODES.Q)
    private fun requestUserPermissions() {

        // An array with location-aware permissions.
        val permissions: ArrayList<String> = ArrayList()
        permissions.add(android.Manifest.permission.ACCESS_FINE_LOCATION)
        permissions.add(android.Manifest.permission.ACCESS_COARSE_LOCATION)


        // Check which permissions is needed to ask to the user.
        val permissionsToRequest = permissionsToRequest(permissions)

        // Show the permissions dialogs to the user.
        if (permissionsToRequest.size > 0)
            requestPermissions(
                permissionsToRequest.toTypedArray(),
                ALL_PERMISSIONS_RESULT
            )
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun requestUserPermissionsForAndroidQ() {
        val permissions: ArrayList<String> = ArrayList()
        permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)

        // Check which permissions is needed to ask to the user.
        val permissionsToRequest = permissionsToRequest(permissions)

        // Show the permissions dialogs to the user.
        if (permissionsToRequest.size > 0)
            requestPermissions(
                permissionsToRequest.toTypedArray(),
                ALL_PERMISSIONS_RESULT
            )
    }

    /**
     * Create an array with the permissions to show to the user.
     *
     * @param permissions An array with the permissions needed by this applications.
     *
     * @return An array with the permissions needed to ask to the user.
     */
    private fun permissionsToRequest(permissions: ArrayList<String>): ArrayList<String> {
        val result: ArrayList<String> = ArrayList()
        for (permission in permissions)
            if (context?.let {
                    PermissionChecker.checkSelfPermission(
                        it,
                        permission
                    )
                } != PackageManager.PERMISSION_GRANTED)
                result.add(permission)
        return result
    }

    /**
     * This method checks if the user allows the application uses all location-aware resources to
     * monitor the user's location.
     *
     * @return A boolean value with the user permission agreement.
     */

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

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun checkPermissionForAndroidQ() =
        context?.let {
            ContextCompat.checkSelfPermission(
                it,Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        } != PackageManager.PERMISSION_GRANTED



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

    @TargetApi(Build.VERSION_CODES.Q)
    private fun addGeofence(scooterName: String, latLng: LatLng) {
        val geofence = Geofence.Builder()
            .setRequestId(scooterName)
            .setCircularRegion(latLng.latitude,latLng.longitude,GEOFENCE_RADIUS_IN_METERS)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .build()

        val geofenceRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        if(!checkPermission() && !checkPermissionForAndroidQ()) {
            geoClient.addGeofences(geofenceRequest,geofencePendingIntent).run {
                addOnSuccessListener {
                    Log.i(TAG,"Geofence added")
                }
                addOnFailureListener {
                    Log.i(TAG, "Geofencing Exception: " + it.message)
                }
            }


        }

    }

}

