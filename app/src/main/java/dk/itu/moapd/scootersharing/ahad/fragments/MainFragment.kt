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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.core.content.PermissionChecker.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import dk.itu.moapd.scootersharing.ahad.MyLocationUpdateService
import dk.itu.moapd.scootersharing.ahad.R
import dk.itu.moapd.scootersharing.ahad.activities.*
import dk.itu.moapd.scootersharing.ahad.utils.SwipeToDeleteCallback
import dk.itu.moapd.scootersharing.ahad.adapters.CustomAdapter
import dk.itu.moapd.scootersharing.ahad.adapters.HistoryRideAdapter
import dk.itu.moapd.scootersharing.ahad.application.ScooterApplication
import dk.itu.moapd.scootersharing.ahad.application.TAG
import dk.itu.moapd.scootersharing.ahad.databinding.FragmentMainBinding
import dk.itu.moapd.scootersharing.ahad.model.*
import java.lang.Math.abs
import java.util.*

/**
 * A fragment to show the `Main Fragment` tab
 */
class MainFragment : Fragment() {

    companion object {
        private val TAG = MainFragment::class.java.simpleName
        private lateinit var adapter: CustomAdapter
        private lateinit var previousRidesAdapter: HistoryRideAdapter
        private const val ALL_PERMISSIONS_RESULT = 1011
    }

    // A reference to the service used to get location updates.
    private var mService: MyLocationUpdateService? = null

    // Tracks the bound state of the service.
    private var mBound = false

    private lateinit var broadcastReceiver: MyReceiver

    /**
     * View binding is a feature that allows you to more easily write code that interacts with
     * views. Once view binding is enabled in a module, it generates a binding class for each XML
     * layout file present in that module. An instance of a binding class contains direct references
     * to all views that have an ID in the corresponding layout.
     */
    private var _binding: FragmentMainBinding? = null

    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }


    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage

    private val scooterViewModel: ScooterViewModel by viewModels {
        ScooterViewModelFactory((requireActivity().application as ScooterApplication).scooterRepository)
    }

    private val historyViewModel: HistoryViewModel by viewModels {
        HistoryViewModel.HistoryViewModelFactory((requireActivity().application as ScooterApplication).historyRepository)
    }

    /**
     * An instance of the Scooter class that has all the information about the scooter
     */

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
        // Initialize Firebase Auth.
        auth = FirebaseAuth.getInstance()
        storage = Firebase.storage("gs://moapd-2023-cc929.appspot.com")

        broadcastReceiver = MyReceiver()
        if(checkPermission()) requestUserPermissions()

        context?.let { LocalBroadcastManager.getInstance(it).registerReceiver(
            broadcastReceiver, IntentFilter(MyLocationUpdateService.ACTION_BROADCAST)
        ) }

        if (auth.currentUser == null) {
            val intent = Intent(activity, LoginActivity::class.java)
            startActivity(intent)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        // Define the recycler view layout manager and adapter
        binding.listRides.layoutManager = LinearLayoutManager(activity)
        // Collecting data from the dataset.
        adapter = CustomAdapter()
        Log.i(TAG,"Current size of Scooter" + adapter.currentList.size)

        previousRidesAdapter = HistoryRideAdapter()

        scooterViewModel.scooters.observe(viewLifecycleOwner) { scooters ->
            scooters?.let {
                adapter.submitList(it)

            }
        }
        Log.i(TAG,"2 Current size of Scooter" + adapter.currentList.size)

        historyViewModel.previousRides.observe(viewLifecycleOwner) { previousRides ->
            previousRides?.let {
                previousRidesAdapter.submitList(it)
            }
        }

        Log.i(TAG,"7 Current size of Scooter" + adapter.currentList.size)


        context?.bindService(
            Intent(context, MyLocationUpdateService::class.java), mServiceConnection,
            Context.BIND_AUTO_CREATE
        )

        context?.let { LocalBroadcastManager.getInstance(it).registerReceiver(
            broadcastReceiver, IntentFilter(MyLocationUpdateService.ACTION_BROADCAST)
        ) }
        mService?.requestLocationUpdates();



        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.i(TAG,"8 Current size of Scooter" + adapter.currentList.size)

        with (binding) {
            startRideButton.setOnClickListener {
                val fragment = StartRideFragment()
                requireActivity().supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_container_view,fragment)
                    .addToBackStack(null)
                    .commit()
            }
            updateRideButton.setOnClickListener {
                val fragment = UpdateRideFragment()
                requireActivity().supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_container_view,fragment)
                    .addToBackStack(null)
                    .commit()
            }

            seeBalanceButton.setOnClickListener {
                val fragment = BalanceFragment()
                requireActivity().supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_container_view,fragment)
                    .addToBackStack(null)
                    .commit()
            }

            findScooterButton.setOnClickListener {
                val fragment = MapsFragment()
                requireActivity().supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_container_view,fragment)
                    .addToBackStack(null)
                    .commit()
            }
            Log.i(TAG,"3 Current size of Scooter" + adapter.currentList.size)

            showRidesButton.setOnClickListener {
                for (ride in adapter.currentList) {
                    setAddress(ride.currentLat,ride.currentLong)
                }
                // Create the custom adapter to populate a list of rides.
                Log.i(TAG,"4 Current size of Scooter" + adapter.currentList.size)
                binding.listRides.layoutManager = LinearLayoutManager(activity)
                binding.listRides.addItemDecoration(
                    DividerItemDecoration(activity,DividerItemDecoration.VERTICAL)
                )
                binding.listRides.adapter = adapter
                Log.i(TAG,"5 Current size of Scooter" + adapter.currentList.size)
                listRides.visibility = if (listRides.visibility == View.VISIBLE){
                    View.INVISIBLE
                } else{
                    View.VISIBLE
                }
                Log.i(TAG,"Scooter id: " + adapter.currentList[0].id)

            }

            historyRideButton.setOnClickListener {
                val fragment = HistoryRideFragment()
                requireActivity().supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_container_view,fragment)
                    .addToBackStack(null)
                    .commit()
            }


            //Adding swipe option
            val swipeHandler = object : SwipeToDeleteCallback() {
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    super.onSwiped(viewHolder, direction)

                    val adapter = listRides.adapter as CustomAdapter
                    val position = viewHolder.adapterPosition

                    val dialog = MaterialAlertDialogBuilder(requireContext())
                    dialog.create()

                    dialog.setTitle("Are you sure you want to end this ride?")
                    dialog.setMessage("Once you accept you can't undo it.")
                    dialog.setNegativeButton("Decline") { dialog, which ->
                        adapter.notifyItemChanged(position)
                        Log.d("TAG", "Ride has not been finished")
                    }
                    dialog.setPositiveButton("Accept") { dialog, which ->
                        val scooter = adapter.currentList[position]
                        Log.i(TAG,"Scooter URL" + scooter.URL)
                        scooter.endTime = Calendar.getInstance().time.minutes.toLong()
                        val diffTime = abs(scooter.startTime - scooter.endTime)
                        val previousRide = History(
                            0,scooter.name,
                            scooter.location,
                            diffTime,
                            scooter.startLong,
                            scooter.startLat,
                            scooter.currentLong,
                            scooter.currentLat,
                            (diffTime * 2).toInt(),
                            scooter.URL) //This needs to be auto incremented
                        scooterViewModel.delete(scooter)
                        historyViewModel.insert(previousRide)
                        previousRidesAdapter.notifyItemChanged(position)

                        val fragment = CameraFragment()
                        val args = Bundle()
                        args.putString("Scooter",scooter.name)
                        fragment.arguments = args
                        requireActivity().supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.fragment_container_view,fragment)
                            .addToBackStack(null)
                            .commit()
                        scooter.URL = scooter.name + ".jpg"
                        Log.d("TAG", "Ride has been finished succesfully")
                    }.show()
                }
            }
            val itemTouchHelper = ItemTouchHelper(swipeHandler)
            itemTouchHelper.attachToRecyclerView(binding.listRides)
            }
        Log.i(TAG,"9 Current size of Scooter" + adapter.currentList.size)
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
                for (ride in adapter.currentList) {
                    ride.currentLong = location.longitude
                    ride.currentLat = location.latitude
                }
            }
        }

    }


    // Monitors the state of the connection to the service.
    private val mServiceConnection: ServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder: MyLocationUpdateService.LocalBinder = service as MyLocationUpdateService.LocalBinder
            mService = binder.getService()
            mBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            mService = null
            mBound = false
        }
    }

    private fun Address.toAddressString() : String {
        val address = this
        val stringBuilder = StringBuilder()
        stringBuilder.apply {
            append(address.getAddressLine(0))
        }
        return stringBuilder.toString()
    }

    private fun setAddress(latitude: Double, longitude: Double) {
        val geocoder = context?.let { Geocoder(it, Locale.getDefault()) }
        val geocodeListener = Geocoder.GeocodeListener { addresses ->
            addresses.firstOrNull()?.toAddressString()?.let {address ->
                for(ride in adapter.currentList) {
                    ride.location = address
                }
            }

        }
        if (Build.VERSION.SDK_INT >= 33)
            geocoder?.getFromLocation(latitude, longitude, 1, geocodeListener)
        else
            geocoder?.getFromLocation(latitude, longitude, 1)?.let { addresses ->
                addresses.firstOrNull()?.toAddressString()?.let { address ->
                    for(ride in adapter.currentList) {
                        ride.location = address
                    }
                }
            }
    }




}

