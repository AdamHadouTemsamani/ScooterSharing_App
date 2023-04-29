package dk.itu.moapd.scootersharing.ahad.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import dk.itu.moapd.scootersharing.ahad.R
import dk.itu.moapd.scootersharing.ahad.activities.MapsActivity
import dk.itu.moapd.scootersharing.ahad.adapters.CustomAdapter
import dk.itu.moapd.scootersharing.ahad.application.ScooterApplication
import dk.itu.moapd.scootersharing.ahad.databinding.FragmentMainBinding
import dk.itu.moapd.scootersharing.ahad.databinding.FragmentMapsBinding
import dk.itu.moapd.scootersharing.ahad.model.ScooterViewModel
import dk.itu.moapd.scootersharing.ahad.model.ScooterViewModelFactory

class MapsFragment : Fragment(), OnMapReadyCallback {

    companion object {
        private val TAG = MapsFragment::class.java.simpleName
        private const val ALL_PERMISSIONS_RESULT = 1011
        private lateinit var adapter: CustomAdapter
    }

    private var _binding: FragmentMapsBinding? = null

    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }


    private val scooterViewModel: ScooterViewModel by viewModels {
        ScooterViewModelFactory((requireActivity().application as ScooterApplication).scooterRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mapFragment = requireActivity().supportFragmentManager
            .findFragmentById(R.id.google_maps) as SupportMapFragment

        mapFragment.getMapAsync(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapsBinding.inflate(inflater,container,false)
        adapter = CustomAdapter()

        scooterViewModel.scooters.observe(viewLifecycleOwner) { scooters ->
            scooters?.let {
                adapter.submitList(it)
            }
        }

        requestUserPermissions()
        return binding.root
    }

    override fun onMapReady(googleMap: GoogleMap) {
        if (checkPermission())
            return

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

        for (ride in adapter.currentList) {
            Log.i(TAG,"Current scooter name:" + ride.name)
            googleMap.addMarker(
                MarkerOptions()
                    .position(LatLng(ride.currentLat,ride.currentLong))
                    .title("Marker in IT University of Copenhagen")
            )
        }
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(itu, 18f))

        // Move the Google Maps UI buttons under the OS top bar.
        googleMap.setPadding(0, 100, 0, 0)
    }

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
            if (context?.let { PermissionChecker.checkSelfPermission(it, permission) } != PackageManager.PERMISSION_GRANTED)
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




}