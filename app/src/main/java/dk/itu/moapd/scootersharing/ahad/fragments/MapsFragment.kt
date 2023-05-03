package dk.itu.moapd.scootersharing.ahad.fragments

import android.Manifest
import android.R.attr.radius
import android.annotation.TargetApi
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
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.SphericalUtil
import dk.itu.moapd.scootersharing.ahad.LocationService
import dk.itu.moapd.scootersharing.ahad.R
import dk.itu.moapd.scootersharing.ahad.application.ScooterApplication
import dk.itu.moapd.scootersharing.ahad.databinding.FragmentMapsBinding
import dk.itu.moapd.scootersharing.ahad.model.Scooter
import dk.itu.moapd.scootersharing.ahad.model.ScooterViewModel
import dk.itu.moapd.scootersharing.ahad.model.ScooterViewModelFactory
import dk.itu.moapd.scootersharing.ahad.utils.GeofenceHelper
import dk.itu.moapd.scootersharing.ahad.utils.Geosphere


class MapsFragment : Fragment(), OnMapReadyCallback {

    companion object {
        private val TAG = MapsFragment::class.java.simpleName
        private const val ALL_PERMISSIONS_RESULT = 1011
        private val GEOFENCE_RADIUS_IN_METERS = 50.0
    }


    //Used for location-aware service
    private var currentLocation: Location? = null
    private var mService: LocationService? = null
        private var mBound = false

    // BroadcastReceiver that gets data from service
    private lateinit var broadcastReceiver: LocationBroadcastReceiver

    private var geofenceList: MutableList<Geosphere> = mutableListOf()
    private var scootersList: MutableList<Scooter> = mutableListOf()

    //Binding
    private var _binding: FragmentMapsBinding? = null

    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    //ViewModel for getting the scooters that exist in the database
    private val scooterViewModel: ScooterViewModel by activityViewModels() {
        ScooterViewModelFactory((requireActivity().application as ScooterApplication).scooterRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Setup classes for location
        broadcastReceiver = LocationBroadcastReceiver()

        //Start requesting location and binding to service
        // This also includes starting service
        mService?.requestLocationUpdates();


    }

    @TargetApi(Build.VERSION_CODES.Q)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        requestUserPermissions()

        Intent(requireContext(),LocationService::class.java).also {
            requireActivity().bindService(it,mServiceConnection,Context.BIND_AUTO_CREATE)
        }

        _binding = FragmentMapsBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager.findFragmentById(R.id.google_maps) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        //Adds the scooters from the database to a list so they are easier to work with.
        scooterViewModel.scooters.observe(viewLifecycleOwner) {
            for(ride in it) {
                scootersList.add(ride)
            }
        }


        with(binding) {
            closestScooterButton.setOnClickListener {
                val scooter = findClosestScooter(LatLng(currentLocation!!.latitude,currentLocation!!.longitude),scootersList)
                Log.i(TAG,"Closest scooter is: " + scooter?.name)
            }

            scanScooterButton.setOnClickListener {
                if(currentLocation != null) {
                    Log.i(TAG, "I am sending the following data: " + currentLocation?.latitude.toString())
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
                }
            }


        }



    }

    @TargetApi(Build.VERSION_CODES.Q)
    override fun onMapReady(googleMap: GoogleMap) {
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



        //Checks for scooters in database and adds markers + geofences
        scooterViewModel.scooters.observe(viewLifecycleOwner) {
            for (ride in it) {
                Log.i(TAG,"${ride.name} is: " + ride.isRide)
                if(!ride.isRide) {
                    Log.i(TAG, "Current scooter name:" + ride.name)
                    googleMap.addMarker(
                        MarkerOptions()
                            .position(LatLng(ride.currentLat, ride.currentLong))
                            .title(ride.name)
                    )
                    ride.name?.let { name ->
                        geofenceList.add(
                            Geosphere(name,
                                LatLng(ride.currentLat,ride.currentLong),
                                GEOFENCE_RADIUS_IN_METERS.toDouble()
                            )
                        )

                    }

                    googleMap.addCircle(CircleOptions()
                        .center(LatLng(ride.currentLat,ride.currentLong))
                        .radius(GEOFENCE_RADIUS_IN_METERS.toDouble())
                        .strokeColor(Color.argb(255, 238,130,238))
                        .fillColor(Color.argb(64, 238,130,238))
                        .strokeWidth(2f)
                    )
                }


            }
        }


        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(55.6596, 12.5910), 18f))
        var isShown: Boolean = false
        googleMap.setOnMyLocationChangeListener {
            if (currentLocation != null) {
                if(!isShown) {
                    isShown = true
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(currentLocation!!.latitude, currentLocation!!.longitude), 18f))
                }
                val currentLat = currentLocation?.latitude
                val currentLong = currentLocation?.longitude
                for (geospere in geofenceList) {
                    if (currentLat != null && currentLong != null) {
                        checkForGeofenceEntry(LatLng(currentLat, currentLong), geospere)
                    }
                }
            }
        }


        // Move the Google Maps UI buttons under the OS top bar.
        googleMap.setPadding(0, 100, 0, 0)
    }

    private fun checkForGeofenceEntry(userLocation: LatLng, geopshere: Geosphere) {
        val startLatLng = LatLng(userLocation.latitude, userLocation.longitude) // User Location
        val geofenceLatLng = geopshere.latlng // Center of geofence

        val distanceInMeters = SphericalUtil.computeDistanceBetween(startLatLng, geofenceLatLng)

        if (distanceInMeters < geopshere.radius) {
            Log.i(TAG,"You have entered the geosphere of ${geopshere.name} my brother!")
        }
    }

    private fun findClosestScooter(userLocation: LatLng, scooters: MutableList<Scooter>): Scooter? {
        var distance: Double = Double.MAX_VALUE
        var closestScooter: Scooter? = null
        for (scooter in scooters) {
            val scooterDistance = SphericalUtil.computeDistanceBetween(userLocation,
                LatLng(scooter.currentLat,scooter.currentLong)
            )
            if(distance >= scooterDistance ) {
                distance = scooterDistance
                closestScooter = scooter
            }
        }
        return closestScooter
    }


    private fun requestUserPermissions() {

        val permissions: ArrayList<String> = ArrayList()
        if (checkPermission()) {
            permissions.add(android.Manifest.permission.ACCESS_FINE_LOCATION)
            permissions.add(android.Manifest.permission.ACCESS_COARSE_LOCATION)

        }
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

    @TargetApi(Build.VERSION_CODES.Q)
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


    private inner class LocationBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val location: Location? =
                intent.extras?.getParcelable(LocationService.EXTRA_LOCATION)
            if (location != null) {
                Log.i(TAG,"Receiever lat: " + location.latitude.toString())
                Log.i(TAG,"Receiever long: " + location.longitude.toString())
                currentLocation = location
                Log.i(TAG,"Location is being updated")
            }
            if(location == null) Log.i(TAG,"There is no location dumbass")
        }

    }

    // Monitors the state of the connection to the service.
    private val mServiceConnection: ServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder: LocationService.LocalBinder = service as LocationService.LocalBinder
            mService = binder.getService()
            mBound = true

            mService?.subscribeToService { location ->
                currentLocation = location
            }

            Log.i(TAG,"Yes work, service yes yes")
        }

        override fun onServiceDisconnected(name: ComponentName) {
            mService?.unsubscribeToService()
            mService = null
            mBound = false
            Log.i(TAG,"no work, service no no")
        }
    }



}

