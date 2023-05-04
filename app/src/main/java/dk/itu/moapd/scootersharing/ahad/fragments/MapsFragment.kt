package dk.itu.moapd.scootersharing.ahad.fragments

import android.Manifest
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
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.maps.android.SphericalUtil
import dk.itu.moapd.scootersharing.ahad.service.LocationService
import dk.itu.moapd.scootersharing.ahad.R
import dk.itu.moapd.scootersharing.ahad.application.ScooterApplication
import dk.itu.moapd.scootersharing.ahad.databinding.FragmentMapsBinding
import dk.itu.moapd.scootersharing.ahad.model.*
import dk.itu.moapd.scootersharing.ahad.utils.Geosphere

//This is the fragment responsible for handling Google Maps
class MapsFragment : Fragment(), OnMapReadyCallback {

    companion object {
        private val TAG = MapsFragment::class.java.simpleName

        //Saves the permissions that the user has accepted
        private const val ALL_PERMISSIONS_RESULT = 1011

        //The radius of geofence. The area of where the user can start a ride.
        //You can change this for testing of the program :).
        private val GEOFENCE_RADIUS_IN_METERS = 50.0
    }

    //Used for location-aware service
    private var currentLocation: Location? = null
    private var mService: LocationService? = null
    private var mBound = false

    // BroadcastReceiver that gets data from service and updated the currentLocation field.
    private lateinit var broadcastReceiver: LocationBroadcastReceiver

    //A list of the geofences and scooters.
    private var geofenceList: MutableList<Geosphere> = mutableListOf()
    private var scootersList: MutableList<Scooter> = mutableListOf()

    //Firebase Authentication
    private lateinit var auth: FirebaseAuth

    //Binding that contains reference to root view.
    private var _binding: FragmentMapsBinding? = null

    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    //ViewModel for getting the scooters that exist in the database
    private val scooterViewModel: ScooterViewModel by activityViewModels() {
        ScooterViewModelFactory((requireActivity().application as ScooterApplication).scooterRepository)
    }

    //ViewModel for getting the users that exist in the database
    private val userBalanceViewModel: UserBalanceViewModel by activityViewModels() {
        UserBalanceViewModelFactory((requireActivity().application as ScooterApplication).userRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Gets an instance of Firebase Authenication
        auth = FirebaseAuth.getInstance()

        //Initialized our broadcast receiver for getting location.
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
        //Start by requesting user permissions for locations.
        requestUserPermissions()

        //Start intent for our Location Service
        Intent(requireContext(), LocationService::class.java).also {
            requireActivity().bindService(it, mServiceConnection, Context.BIND_AUTO_CREATE)
        }

        //Inflate layout from binding
        _binding = FragmentMapsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //create our magFrament from our google_maps and creates the UI for the Google Maps.
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.google_maps) as SupportMapFragment?
        mapFragment?.getMapAsync(this) //This gets the maps UI

        //Adds the scooters from the database to a list so they are easier to work with.
        scooterViewModel.scooters.observe(viewLifecycleOwner) {
            for (ride in it) {
                scootersList.add(ride)
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.Q)
    override fun onMapReady(googleMap: GoogleMap) {
        if (checkPermission()) return
        //We start by clearing everything we've drawn on the map so far.
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
                if (!ride.isRide) {
                    //If the isRide is false, we add it's markers and geoFences on the map.
                    googleMap.addMarker(
                        MarkerOptions()
                            .position(LatLng(ride.currentLat, ride.currentLong))
                            .title(ride.name)
                    )
                    ride.name?.let { name ->
                        geofenceList.add(
                            Geosphere(
                                name,
                                LatLng(ride.currentLat, ride.currentLong),
                                GEOFENCE_RADIUS_IN_METERS.toDouble()
                            )
                        )

                    }
                    //This adds the geofence circles to visualize for the user they are inside of the geoFence.
                    googleMap.addCircle(
                        CircleOptions()
                            .center(LatLng(ride.currentLat, ride.currentLong))
                            .radius(GEOFENCE_RADIUS_IN_METERS.toDouble())
                            .strokeColor(Color.argb(255, 238, 130, 238))
                            .fillColor(Color.argb(64, 238, 130, 238))
                            .strokeWidth(2f)
                    )
                }
            }

            with(binding) {
                closestScooterButton.setOnClickListener {
                    //Finds the closest scooter based on their location and moves the camera there.
                    val scooter = findClosestScooter(
                        LatLng(
                            currentLocation!!.latitude,
                            currentLocation!!.longitude
                        ), scootersList
                    )
                    googleMap.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(
                                scooter!!.currentLat,
                                scooter!!.currentLong
                            ), 18f
                        )
                    )
                }

                scanScooterButton.setOnClickListener {
                    //We find the users in the database
                    userBalanceViewModel.users.observe(viewLifecycleOwner) {
                        for (user in it) {
                            //We check whether the user is loged in and have added their card.
                            if (user.email == auth.currentUser!!.email && user.isCard) {
                                var isRideActive = false
                                scooterViewModel.scooters.observe(viewLifecycleOwner) {
                                    //We get the scooters from the database and use an accumulator to check
                                    //whether a ride is sltrady active.
                                    for (ride in it) {
                                        if (ride.isRide) isRideActive = true
                                    }
                                    if (isRideActive) showMessage("You have an active ride")
                                }

                                //We get our location and find the closest scooter
                                //This is used to check whether we are inside of the geoFence.
                                val ourLocation =
                                    LatLng(currentLocation!!.latitude, currentLocation!!.longitude)
                                val closestScooter = findClosestScooter(ourLocation, scootersList)

                                var geopshere: Geosphere? = null
                                for (geo in geofenceList) {
                                    if (closestScooter!!.name.equals(geo.name)) geopshere = geo
                                }
                                //Check whether we are inside of our geoFence and whether we have an active ride.
                                if (checkForGeofenceEntry(
                                        ourLocation,
                                        geopshere!!
                                    ) && !isRideActive
                                ) {
                                    val fragment = QrcodeFragment()
                                    val args = Bundle()
                                    //We send the data needed for a ride to our QrcodeFragment.
                                    //This is done so we can start a ride.
                                    args.putString(
                                        "currentLat",
                                        currentLocation!!.latitude.toString()
                                    )
                                    args.putString(
                                        "currentLong",
                                        currentLocation!!.longitude.toString()
                                    )
                                    args.putString("closestScooter", geopshere.name)
                                    fragment.arguments = args
                                    //Navigate to QrcodeFragment
                                    requireActivity().supportFragmentManager
                                        .beginTransaction()
                                        .replace(R.id.fragment_container_view, fragment)
                                        .addToBackStack(null)
                                        .commit()
                                } else {
                                    showMessage("You are not within the geosphere of a Scooter")
                                }
                            } else {
                                showMessage("You have removed your card. Please add a card. So you can pay for a scooter.")
                            }
                        }
                        if (it.isEmpty()) showMessage("Please add a card. So you can pay for a scooter.")
                    }

                }


            }


        }

        //In case the users location is currently null we move the camera to ITU.
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(55.6596, 12.5910), 18f))
        var isShown: Boolean = false
        googleMap.setOnMyLocationChangeListener {
            if (currentLocation != null) {
                if (!isShown) {
                    isShown = true
                    //isShown is false when you first load the Map. and we move the camera to the users location.
                    //This is triggered whenever the user moves in real life.
                    googleMap.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(
                                currentLocation!!.latitude,
                                currentLocation!!.longitude
                            ), 18f
                        )
                    )
                }
            }
        }
        googleMap.setOnCameraIdleListener {
            //This is triggered when the user is done panning in the MapFragment.
            if (currentLocation != null) {
                if (!isShown) {
                    isShown = true
                    //isShown is false when you first load the Map. and we move the camera to the users location.
                    googleMap.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(
                                currentLocation!!.latitude,
                                currentLocation!!.longitude
                            ), 18f
                        )
                    )
                }
            }

        }
        // Move the Google Maps UI buttons under the OS top bar.
        googleMap.setPadding(0, 100, 0, 0)
    }

    //We check whether the user is inside of the given geofence.
    private fun checkForGeofenceEntry(userLocation: LatLng, geopshere: Geosphere): Boolean {
        val startLatLng = LatLng(userLocation.latitude, userLocation.longitude) // User Location
        val geofenceLatLng = geopshere.latlng // Center of geofence

        val distanceInMeters = SphericalUtil.computeDistanceBetween(startLatLng, geofenceLatLng)

        if (distanceInMeters < geopshere.radius) {
            return true
        }
        return false
    }

    //This finds the closest scooter to the user based on their location.
    private fun findClosestScooter(userLocation: LatLng, scooters: MutableList<Scooter>): Scooter? {
        var distance: Double = Double.MAX_VALUE
        var closestScooter: Scooter? = null
        for (scooter in scooters) {
            val scooterDistance = SphericalUtil.computeDistanceBetween(
                userLocation,
                LatLng(scooter.currentLat, scooter.currentLong)
            )
            if (distance >= scooterDistance && !scooter.isRide) {
                distance = scooterDistance
                closestScooter = scooter
            }
        }
        return closestScooter
    }

    //Method that asks the user the appropriate permissions to use features in this fragment.
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

    //Checks if the user has accepted the required permissions.
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

    //Makes a snackbar
    private fun showMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    //BroadcastReceiver that listens to service and updates our current location
    private inner class LocationBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val location: Location? =
                intent.extras?.getParcelable(LocationService.EXTRA_LOCATION)
            if (location != null) {
                currentLocation = location
            }
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
        }

        override fun onServiceDisconnected(name: ComponentName) {
            mService?.unsubscribeToService()
            mService = null
            mBound = false
        }
    }


}

