package dk.itu.moapd.scootersharing.ahad.fragments

import android.Manifest
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment
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


    private val scooterViewModel: ScooterViewModel by viewModels {
        ScooterViewModelFactory((requireActivity().application as ScooterApplication).scooterRepository)
    }

    private lateinit var codeScanner: CodeScanner

    private var scooterName: String? = null
    private var currentLat: Double? = null
    private var currentLong: Double? = null

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentLat = arguments?.getString("currentLat")?.toDouble()
        currentLong = arguments?.getString("currentLong")?.toDouble()
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
                                Log.i(TAG,"I am gonna print something!")
                                val lat =  currentLat
                                val long = currentLong
                                Log.i(TAG,"lat " + currentLat + "long" + currentLong)

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
    private fun addScooterAndExitCamera(currentLat: Double, currentLong: Double) {
            Log.i(TAG,"It enters the function")
            if(currentLat == null && currentLong == null) {
                Log.i(TAG,"Your location is null :(")
            showMessage("Your location is currently null. Please go back to Main Screen and try again!")
            return
        }
        val name = scooterName
        val location = "Something went wrong?"
        val date = Calendar.getInstance().time.minutes.toLong()
        val scooter = Scooter(0,name,location,date,date,
            currentLong,
            currentLat,
            currentLong,
            currentLat,
            true)
        scooter.URL = name + ".jpg"
        Log.i(TAG,"I am about to add this scooter the database")
        scooterViewModel.insert(scooter)

        showMessage("Your scooter ride has been started. Enjoy!")

        requireActivity().supportFragmentManager
            .popBackStack()
        requireActivity().supportFragmentManager
            .popBackStack()
    }


    private fun showMessage(message: String) {
        //Snackbar :D
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }







}