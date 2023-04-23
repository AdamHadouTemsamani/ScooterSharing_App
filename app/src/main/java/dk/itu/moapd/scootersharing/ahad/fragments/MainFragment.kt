package dk.itu.moapd.scootersharing.ahad.fragments

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.PermissionChecker.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import dk.itu.moapd.scootersharing.ahad.activities.HistoryRideActivity
import dk.itu.moapd.scootersharing.ahad.utils.SwipeToDeleteCallback
import dk.itu.moapd.scootersharing.ahad.activities.LoginActivity
import dk.itu.moapd.scootersharing.ahad.activities.StartRideActivity
import dk.itu.moapd.scootersharing.ahad.activities.UpdateRideActivity
import dk.itu.moapd.scootersharing.ahad.adapters.CustomAdapter
import dk.itu.moapd.scootersharing.ahad.adapters.HistoryRideAdapter
import dk.itu.moapd.scootersharing.ahad.application.ScooterApplication
import dk.itu.moapd.scootersharing.ahad.databinding.FragmentMainBinding
import dk.itu.moapd.scootersharing.ahad.model.*
import java.lang.Math.abs
import java.util.*

/**
 * A fragment to show the `Main Fragment` tab
 */
class MainFragment : Fragment() {

    companion object {
        private lateinit var adapter: CustomAdapter
        private lateinit var previousRidesAdapter: HistoryRideAdapter
        private const val ALL_PERMISSIONS_RESULT = 1011
    }

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
        previousRidesAdapter = HistoryRideAdapter()

        scooterViewModel.scooters.observe(viewLifecycleOwner) { scooters ->
            scooters?.let {
                adapter.submitList(it)
            }
        }

        historyViewModel.previousRides.observe(viewLifecycleOwner) { previousRides ->
            previousRides?.let {
                previousRidesAdapter.submitList(it)
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requestUserPermissions()
        if (auth.currentUser != null)
            binding.loginButton.text = "Sign Out"
        if (auth.currentUser == null)
            binding.loginButton.text = "Sign In"

        with (binding) {
            startRideButton.setOnClickListener {
                val intent = Intent(activity, StartRideActivity::class.java)
                startActivity(intent)
                activity?.finish()
            }

            updateRideButton.setOnClickListener {
                val intent = Intent(activity, UpdateRideActivity::class.java)
                startActivity(intent)
                activity?.finish()
            }

            showRidesButton.setOnClickListener {
                // Create the custom adapter to populate a list of rides.
                binding.listRides.layoutManager = LinearLayoutManager(activity)
                binding.listRides.addItemDecoration(
                    DividerItemDecoration(activity,DividerItemDecoration.VERTICAL)
                )
                binding.listRides.adapter = adapter
                listRides.visibility = if (listRides.visibility == View.VISIBLE){
                    View.INVISIBLE
                } else{
                    View.VISIBLE
                }

            }

            historyRideButton.setOnClickListener {
                val intent = Intent(activity, HistoryRideActivity::class.java)
                startActivity(intent)
                activity?.finish()
            }

            loginButton.setOnClickListener {
                if (auth.currentUser == null) {
                    val intent = Intent(activity, LoginActivity::class.java)
                    startActivity(intent)
                    loginButton.text = "Sign Out"
                }
                if (auth.currentUser != null) {
                    auth.signOut()
                    loginButton.text = "Sign In"
                }
                val user = auth.currentUser
            }

            //Adding swipe option
            val swipeHandler = object : SwipeToDeleteCallback() {
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    super.onSwiped(viewHolder, direction)

                    val adapter = listRides.adapter as CustomAdapter
                    val position = viewHolder.adapterPosition

                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Are you sure you want to end this ride?")
                        .setMessage("Once you accept you can't undo it.")

                        .setNegativeButton("Decline") { dialog, which ->
                            adapter.notifyItemChanged(position)
                            Log.d("TAG", "Ride has not been finished")
                        }
                        .setPositiveButton("Accept") { dialog, which ->
                            val scooter = adapter.currentList[position]
                            scooter.endTime = Calendar.getInstance().time.minutes.toLong()
                            val diffTime = abs(scooter.startTime - scooter.endTime)
                            val previousRide = History(
                                0,scooter.name, scooter.location,diffTime, (diffTime * 2).toInt()) //This needs to be auto incremented

                            scooterViewModel.delete(scooter)
                            historyViewModel.insert(previousRide)
                            previousRidesAdapter.notifyItemChanged(position)
                            Log.d("TAG", "Ride has been finished succesfully")
                        }
                        .show()
                }
            }
            val itemTouchHelper = ItemTouchHelper(swipeHandler)
            itemTouchHelper.attachToRecyclerView(binding.listRides)
            }
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

    private fun permissionsToRequest(permissions: ArrayList<String>): ArrayList<String> {
        val result: ArrayList<String> = ArrayList()
        for (permission in permissions)
            if (context?.let { checkSelfPermission(it,permission) } != PackageManager.PERMISSION_GRANTED)
                result.add(permission)

        return result
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}