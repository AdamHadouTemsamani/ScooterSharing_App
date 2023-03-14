package dk.itu.moapd.scootersharing.ahad

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.behavior.SwipeDismissBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dk.itu.moapd.scootersharing.ahad.databinding.FragmentMainBinding
import dk.itu.moapd.scootersharing.ahad.databinding.ListRidesBinding


/**
 * A fragment to show the `Main Fragment` tab
 */
class MainFragment : Fragment() {

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

    companion object {
        lateinit var ridesDB : RidesDB
        private lateinit var adapter: CustomAdapter
    }

    /**
     * An instance of the Scooter class that has all the information about the scooter
     */
    private val scooter: Scooter = Scooter("","", System.currentTimeMillis())


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

        // Singleton to share an object between the app activities.
        ridesDB = RidesDB.get(requireContext())

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        // Define the recycler view layout manager and adapter
        binding.listRides.layoutManager = LinearLayoutManager(activity)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with (binding) {
            startRideButton.setOnClickListener {
                val intent = Intent(activity, StartRideActivity::class.java)
                startActivity(intent)
            }

            updateRideButton.setOnClickListener {
                val intent = Intent(activity, UpdateRideActivity::class.java)
                startActivity(intent)
            }

            showRidesButton.setOnClickListener {
                // Create the custom adapter to populate a list of rides.
                adapter = CustomAdapter(ridesDB.getRidesList() as ArrayList<Scooter>)
                binding.listRides.layoutManager = LinearLayoutManager(activity)
                binding.listRides.adapter = adapter
                listRides.visibility = if (listRides.visibility == View.VISIBLE){
                    View.INVISIBLE
                } else{
                    View.VISIBLE
                }

            }

            //Adding swipe option
            val swipeHandler = object : SwipeToDeleteCallback() {
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    super.onSwiped(viewHolder, direction)

                    val adapter = listRides.adapter as CustomAdapter
                    val position = viewHolder.adapterPosition

                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Are you sure you want to delete this ride?")
                        .setMessage("Once you accept you can't undo it.")

                        .setNegativeButton("Decline") { dialog, which ->
                            adapter.notifyItemChanged(position)
                            showSnackbar("Ride has not been deleted")
                        }
                        .setPositiveButton("Accept") { dialog, which ->
                            adapter.removeAt(position)
                            showSnackbar("Ride has been succesfully deleted")
                        }
                        .show()
                }
            }
            val itemTouchHelper = ItemTouchHelper(swipeHandler)
            itemTouchHelper.attachToRecyclerView(binding.listRides)

            }

    }

    /*
    override fun onResume() {
        super.onResume()
        adapter.notifyDataSetChanged()
    }
    */


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun showSnackbar(text: String) {
        Snackbar.make(binding.root, text, Snackbar.LENGTH_SHORT)
            .show()
    }



}