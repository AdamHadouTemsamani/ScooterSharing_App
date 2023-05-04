package dk.itu.moapd.scootersharing.ahad.fragments

import android.Manifest
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dk.itu.moapd.scootersharing.ahad.R
import dk.itu.moapd.scootersharing.ahad.application.ScooterApplication
import dk.itu.moapd.scootersharing.ahad.databinding.FragmentMainBinding
import dk.itu.moapd.scootersharing.ahad.databinding.FragmentQrscannerBinding
import dk.itu.moapd.scootersharing.ahad.model.History
import dk.itu.moapd.scootersharing.ahad.model.Scooter
import dk.itu.moapd.scootersharing.ahad.model.ScooterViewModel
import dk.itu.moapd.scootersharing.ahad.model.ScooterViewModelFactory
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

//This is the Fragment responsible for scanning QR codes of a Scooter to start a ride.
class QrcodeFragment : Fragment() {

    companion object {
        private val TAG = QrcodeFragment.javaClass::class.java.simpleName
        private const val ALL_PERMISSIONS_RESULT = 1011
    }

    //Binding that contains reference to root view.
    private var _binding: FragmentQrscannerBinding? = null

    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    //ViewModel for getting the scooters that exist in the database
    private val scooterViewModel: ScooterViewModel by activityViewModels()

    //Library responsible for the QR scanner.
    private lateinit var codeScanner: CodeScanner

    //Private fields used to start a ride
    private var scooterName: String? = null
    private var currentLat: Double? = null
    private var currentLong: Double? = null
    private var closestScooter: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //We start by checking the required permissions (camera)
        if (checkPermission()) requestUserPermissions()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //Inflate layout from binding
        _binding = FragmentQrscannerBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Get the data given by our MapFragment so that we can start a ride.
        currentLat = requireArguments().getString("currentLat")!!.toDouble()
        currentLong = requireArguments().getString("currentLong")!!.toDouble()
        closestScooter = requireArguments().getString("closestScooter")

        //Start our QR scanner view
        val scannerView = view.findViewById<CodeScannerView>(R.id.scanner_view)

        //Initialize our QR scanner.
        codeScanner = CodeScanner(requireContext(), scannerView)

        //Parameters for camera that uses CodeScanner
        codeScanner.camera = CodeScanner.CAMERA_BACK
        codeScanner.formats = CodeScanner.ALL_FORMATS
        codeScanner.autoFocusMode = AutoFocusMode.SAFE
        codeScanner.scanMode = ScanMode.SINGLE
        codeScanner.isAutoFocusEnabled = true
        codeScanner.isFlashEnabled = false

        if (!checkPermission()) {
            //Callbacks for codeScanner
            codeScanner.decodeCallback = DecodeCallback {

                requireActivity().runOnUiThread {
                    if (!it.text.isEmpty()) {
                        scooterName = it.text
                        val dialog = MaterialAlertDialogBuilder(requireContext())
                        dialog.create()

                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Are you sure you want to start the ride with: " + it.text)
                            .setNegativeButton("Decline") { dialog, which ->
                                //Do nothing
                            }
                            .setPositiveButton("Accept") { dialog, which ->
                                addScooterAndExitCamera(currentLat!!, currentLong!!)
                            }
                            .show()
                    }
                }

            }
        }
        codeScanner.errorCallback = ErrorCallback {
            requireActivity().runOnUiThread {
                Log.i(TAG, "There was an error!")
            }
        }

        scannerView.setOnClickListener {
            codeScanner.startPreview() //This starts the QR scanner preview.
        }

    }

    override fun onResume() {
        super.onResume()
        //When we resume our Fragment we also call startPreview.
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources() //This is done so that our app is not resource heavy.
        super.onPause()
    }

    //Method that asks the user the appropriate permissions to use features in this fragment.
    private fun requestUserPermissions() {
        //An array with permissions.
        val permissions: ArrayList<String> = ArrayList()
        permissions.add(android.Manifest.permission.CAMERA)

        //Check which permissions is needed to ask to the user.
        val permissionsToRequest = permissionsToRequest(permissions)

        //Show the permissions dialogue to the user.
        if (permissionsToRequest.size > 0)
            requestPermissions(
                permissionsToRequest.toTypedArray(),
                ALL_PERMISSIONS_RESULT
            )
    }

    //Checks if the user has accepted the required permissions.
    private fun checkPermission() =
        context?.let {
            ContextCompat.checkSelfPermission(
                it, Manifest.permission.CAMERA
            )
        } != PackageManager.PERMISSION_GRANTED

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

    //This updates our our scooter in database so that isRide is true.
    //When done it is done it pops back to CameraFragment.
    @RequiresApi(Build.VERSION_CODES.O)
    private fun addScooterAndExitCamera(currentLat: Double, currentLong: Double) {
        scooterViewModel.scooters.observe(viewLifecycleOwner) {
            var isRideExist = false
            for (ride in it) {
                if (ride.isRide) isRideExist = true
            }

            for (ride in it) {
                if (scooterName != closestScooter) {
                    //If it isn't the closest scooter it can't be within it's geolocation.
                    showMessage("You are not within the area of ${closestScooter}")
                    return@observe

                } else if (ride.name!!.equals(scooterName) && !isRideExist && scooterName == closestScooter) {
                    //We check whether a ride already exists. Do nothing if it does.
                    ride.isRide = true
                    scooterViewModel.update(ride)
                } else {
                    showMessage("You currently already have an active ride.")
                }
            }
        }
        //This goes pops our stack and returns to MainFragment.
        requireActivity().supportFragmentManager
            .popBackStack()
        requireActivity().supportFragmentManager
            .popBackStack()
        val fragment = MainFragment()
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container_view, fragment).commit()
    }

    //Makes it easier to make a Snackbar
    private fun showMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

}