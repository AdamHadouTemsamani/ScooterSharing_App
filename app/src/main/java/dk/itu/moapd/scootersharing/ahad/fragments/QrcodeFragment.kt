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

class QrcodeFragment : Fragment() {

    companion object {
        private val TAG = QrcodeFragment.javaClass::class.java.simpleName
        private const val ALL_PERMISSIONS_RESULT = 1011
    }

    private var _binding: FragmentQrscannerBinding? = null

    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }


    private val scooterViewModel: ScooterViewModel by activityViewModels()

    private lateinit var codeScanner: CodeScanner

    private var scooterName: String? = null
    private var currentLat: Double? = null
    private var currentLong: Double? = null
    private var closestScooter: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (checkPermission()) requestUserPermissions()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentQrscannerBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        currentLat = requireArguments().getString("currentLat")!!.toDouble()
        currentLong = requireArguments().getString("currentLong")!!.toDouble()
        closestScooter = requireArguments().getString("closestScooter")
        Log.i(TAG,"Current Lat: " + currentLat + " Current Long: " + currentLong  )

        val scannerView = view.findViewById<CodeScannerView>(R.id.scanner_view)

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
                    Log.i(TAG, "QRCode result: " + it.text)
                    Log.i(TAG,"Is it empty: " + it.text.isEmpty())
                    if (!it.text.isEmpty()) {
                        scooterName = it.text
                        val dialog = MaterialAlertDialogBuilder(requireContext())
                        dialog.create()

                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Are you sure you want to start the ride with: " + it.text)
                            .setNegativeButton("Decline") { dialog, which ->

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
            codeScanner.startPreview()
        }

    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }

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
    @RequiresApi(Build.VERSION_CODES.O)
    private fun addScooterAndExitCamera(currentLat: Double, currentLong: Double) {
        Log.i(TAG,"Scooter is supposed to be updated and ${scooterName}")
        scooterViewModel.scooters.observe(viewLifecycleOwner) {
            var isRideExist = false
            for(ride in it) {
                if (ride.isRide) isRideExist = true
            }

            for(ride in it) {
                if (scooterName != closestScooter) {
                    showMessage("You are not within the area of ${closestScooter}")
                    return@observe
                } else if(ride.name!!.equals(scooterName) && !isRideExist && scooterName == closestScooter) {
                    val id = scooterName?.get(4)?.toInt()
                    val date = LocalTime.now()
                    val minutesInDay = Duration.between(date.withSecond(0).withMinute(0).withHour(0), date).toMinutes()
                    ride.isRide = true
                    val scooter = id?.let { Scooter(it, scooterName,"Unknown",minutesInDay,minutesInDay,currentLong,currentLat,currentLong,currentLat,true,scooterName + ".jpg") }
                    scooterViewModel.update(ride)
                } else {
                    showMessage("You currently already have an active ride.")
                }
            }
        }
        requireActivity().supportFragmentManager
            .popBackStack()
        requireActivity().supportFragmentManager
            .popBackStack()
        val fragment = MainFragment()
        requireActivity().supportFragmentManager.beginTransaction().replace(R.id.fragment_container_view,fragment).commit()
    }

    private fun showMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

}