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
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import dk.itu.moapd.scootersharing.ahad.service.LocationService
import dk.itu.moapd.scootersharing.ahad.R
import dk.itu.moapd.scootersharing.ahad.activities.*
import dk.itu.moapd.scootersharing.ahad.utils.SwipeToDeleteCallback
import dk.itu.moapd.scootersharing.ahad.adapters.CustomAdapter
import dk.itu.moapd.scootersharing.ahad.adapters.HistoryRideAdapter
import dk.itu.moapd.scootersharing.ahad.application.ScooterApplication
import dk.itu.moapd.scootersharing.ahad.databinding.FragmentMainBinding
import dk.itu.moapd.scootersharing.ahad.model.*
import java.lang.Math.abs
import java.time.Duration
import java.time.LocalTime
import java.util.*

//This is Fragment that is hosted first when MainActivity is run.
class MainFragment : Fragment() {

    companion object {
        private val TAG = MainFragment::class.java.simpleName
        private lateinit var adapter: CustomAdapter
        private lateinit var previousRidesAdapter: HistoryRideAdapter
        private const val ALL_PERMISSIONS_RESULT = 1011
    }

    //Used for location-aware service
    private var currentLocation: Location? = null
    private var mService: LocationService? = null
    private var mBound = false

    // BroadcastReceiver that gets data from our Location service
    private var broadcastReceiver = LocationBroadcastReceiver()

    //Binding that contains reference to root view.
    private var _binding: FragmentMainBinding? = null

    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    //Firebase authentication & Firebase storage
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage

    //Each of the viewModels - used for getting data from SQL database through room.
    private val scooterViewModel: ScooterViewModel by viewModels {
        ScooterViewModelFactory((requireActivity().application as ScooterApplication).scooterRepository)
    }

    private val historyViewModel: HistoryViewModel by viewModels {
        HistoryViewModel.HistoryViewModelFactory((requireActivity().application as ScooterApplication).historyRepository)
    }

    private val userBalanceViewModel: UserBalanceViewModel by activityViewModels() {
        UserBalanceViewModelFactory((requireActivity().application as ScooterApplication).userRepository)
    }

    //Called when activity is started.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize Firebase Auth && set url for Firebase storage.
        auth = FirebaseAuth.getInstance()
        storage = Firebase.storage("gs://moapd-2023-cc929.appspot.com")

        //Check if the user is logged in. If not make them.
        if (auth.currentUser == null) {
            val intent = Intent(activity, LoginActivity::class.java)
            startActivity(intent)
        }

        //Check if the user has accepted the required permissions.
        if (checkPermission()) requestUserPermissions()

        //Broadcast receiever that listens to location updated
        broadcastReceiver = LocationBroadcastReceiver()

        //Start requesting location and binding to service
        mService?.requestLocationUpdates();

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //Fix main fragment twice when orientation changes.
        if (savedInstanceState != null) {
            val fragment = MainFragment()
            requireActivity().supportFragmentManager
                .popBackStack()
        }

        //Inflate layout from binding
        _binding = FragmentMainBinding.inflate(inflater, container, false)

        // Define the recycler view layout manager and adapter
        binding.listRides.layoutManager = LinearLayoutManager(activity)

        // Adapter for Active rides and History of Rides.
        adapter = CustomAdapter()
        previousRidesAdapter = HistoryRideAdapter()


        //Get the currently active ride from database.
        val yourCurrentScooter = mutableListOf<Scooter>()
        scooterViewModel.scooters.observe(viewLifecycleOwner) {
            for (ride in it) {
                if (ride.isRide) yourCurrentScooter.add(ride)
            }
            adapter.submitList(yourCurrentScooter)
        }

        //Start intent for our Location Service
        Intent(requireContext(), LocationService::class.java).also {
            requireActivity().bindService(it, mServiceConnection, Context.BIND_AUTO_CREATE)
        }

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {

            //Updates the appropriate information of the current ride.
            val date = LocalTime.now()
            val minutesInDay =
                Duration.between(date.withSecond(0).withMinute(0).withHour(0), date).toMinutes()
            //Updating the current location of Scooter
            scooterViewModel.scooters.observe(viewLifecycleOwner) {
                for (ride in it) {
                    if (ride.isRide) {
                        if (currentLocation != null) {
                            ride.endTime = minutesInDay
                            ride.currentLong = currentLocation!!.longitude
                            ride.currentLat = currentLocation!!.latitude
                            setAddress(ride.currentLat, ride.currentLong)
                        }

                    }
                }
            }

            //Makes layout manager for our Recycler view.
            binding.listRides.layoutManager = LinearLayoutManager(activity)
            binding.listRides.addItemDecoration(
                DividerItemDecoration(activity, DividerItemDecoration.VERTICAL)
            )
            binding.listRides.adapter = adapter

            //Each of these handles what happens when user clicks on button in the layout.

            seeBalanceButton.setOnClickListener {
                if (auth.currentUser == null) {
                    val intent = Intent(activity, LoginActivity::class.java)
                    startActivity(intent)
                }
                val fragment = BalanceFragment()
                requireActivity().supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_container_view, fragment)
                    .addToBackStack(null)
                    .commit()
            }

            findScooterButton.setOnClickListener {
                if (auth.currentUser == null) {
                    val intent = Intent(activity, LoginActivity::class.java)
                    startActivity(intent)
                }
                val fragment = MapsFragment()
                requireActivity().supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_container_view, fragment)
                    .addToBackStack(null)
                    .commit()
            }

            historyRideButton.setOnClickListener {
                val fragment = HistoryRideFragment()
                requireActivity().supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_container_view, fragment)
                    .addToBackStack(null)
                    .commit()
            }

            logOutButton.setOnClickListener {
                if (auth.currentUser != null) {
                    auth.signOut()
                    val intent = Intent(activity, LoginActivity::class.java)
                    startActivity(intent)
                }
            }

            //Adds the ability to swipe on a scooter in Recycler view.
            val swipeHandler = object : SwipeToDeleteCallback() {
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    super.onSwiped(viewHolder, direction)

                    //Get specific scooter fra Recycler View
                    val adapter = listRides.adapter as CustomAdapter
                    val position = viewHolder.adapterPosition

                    //Makes a Dialogue option for the user.
                    val dialog = MaterialAlertDialogBuilder(requireContext())
                    dialog.create()
                    dialog.setTitle("Are you sure you want to end this ride?")
                    dialog.setMessage("Once you accept you can't undo it.")

                    //Decline
                    dialog.setNegativeButton("Decline") { dialog, which ->
                        adapter.notifyItemChanged(position)
                    }
                    //Accept
                    dialog.setPositiveButton("Accept") { dialog, which ->

                        //Calculate current time and price of scooter
                        val scooter = adapter.currentList[position]
                        val date = LocalTime.now()
                        val minutesInDay =
                            Duration.between(date.withSecond(0).withMinute(0).withHour(0), date)
                                .toMinutes()
                        val price = abs(scooter.startTime - minutesInDay)
                        //Make a History object
                        val previousRide = History(
                            0, scooter.name,
                            scooter.location,
                            price,
                            scooter.startLong,
                            scooter.startLat,
                            scooter.currentLong,
                            scooter.currentLat,
                            price.toInt(),
                            scooter.URL
                        )

                        //Gets the current users balance and checks whether they can pay for scooter.
                        val user = getCurrentUser()
                        var userBalance = user!!.balance?.minus(price)
                        if (userBalance != null) {
                            if (userBalance < 0) {
                                showMessage("Your account doesn't have enough balance. Please tank up.")
                            } else {
                                //It enters this if the user has enough balance.
                                scooter.isRide = false
                                scooter.endTime = System.currentTimeMillis()
                                user.balance = userBalance
                                //Update all ViewModels
                                userBalanceViewModel.update(user)
                                scooterViewModel.update(scooter)
                                historyViewModel.insert(previousRide)
                                previousRidesAdapter.notifyItemChanged(position)

                                //Navigate to Camera Fragment.
                                val fragment = CameraFragment()
                                val args = Bundle()
                                args.putString("Scooter", scooter.name)
                                fragment.arguments = args
                                requireActivity().supportFragmentManager
                                    .beginTransaction()
                                    .replace(R.id.fragment_container_view, fragment)
                                    .addToBackStack(null)
                                    .commit()
                                scooter.URL = scooter.name + ".jpg"
                            }
                        }

                    }.show()
                }
            }
            val itemTouchHelper = ItemTouchHelper(swipeHandler)
            itemTouchHelper.attachToRecyclerView(binding.listRides)
        }
    }

    override fun onResume() {
        super.onResume()

        //Starts service up again.
        context?.let {
            LocalBroadcastManager.getInstance(it).registerReceiver(
                broadcastReceiver, IntentFilter(LocationService.ACTION_BROADCAST)

            )
        }
        context?.startService(Intent(context, LocationService::class.java))
    }

    override fun onPause() {
        super.onPause()
        //If the fragment is paused we unregister the broadcast receiever
        context?.let { LocalBroadcastManager.getInstance(it).unregisterReceiver(broadcastReceiver) }
        //unsubscribeToLocationUpdates()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        //When view is destroyed we stop our service connection.
        if (mBound) {
            context?.unbindService(mServiceConnection)
            mBound = false
        }
        context?.stopService(Intent(context, LocationService::class.java))
        _binding = null
    }

    //Method that asks the user the appropriate permissions to use features in this fragment.
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

    //Checks if the user has accepted the required permissions.
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
            if (context?.let {
                    PermissionChecker.checkSelfPermission(
                        it,
                        permission
                    )
                } != PackageManager.PERMISSION_GRANTED)
                result.add(permission)

        return result
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

    //Gets the current user from Firebase Authentication.
    fun getCurrentUser(): UserBalance? {
        var userBalance: UserBalance? = null
        userBalanceViewModel.users.observe(viewLifecycleOwner) {
            for (user in it) {
                if (user.email == auth.currentUser!!.email) {
                    userBalance = user
                }
            }
        }
        return userBalance
    }

    //Makes a snackbar
    private fun showMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    //These two functions are used for building the address of the user based on their location

    private fun Address.toAddressString(): String {
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
            addresses.firstOrNull()?.toAddressString()?.let { address ->
                for (ride in adapter.currentList) {
                    ride.location = address
                }
            }

        }
        if (Build.VERSION.SDK_INT >= 33)
            geocoder?.getFromLocation(latitude, longitude, 1, geocodeListener)
        else
            geocoder?.getFromLocation(latitude, longitude, 1)?.let { addresses ->
                addresses.firstOrNull()?.toAddressString()?.let { address ->
                    for (ride in adapter.currentList) {
                        ride.location = address
                    }
                }
            }
    }

}


