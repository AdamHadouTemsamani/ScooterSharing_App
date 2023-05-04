package dk.itu.moapd.scootersharing.ahad.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import dk.itu.moapd.scootersharing.ahad.model.Scooter
import dk.itu.moapd.scootersharing.ahad.databinding.ListRidesBinding
import java.lang.Math.abs

//Adapter for holding our currently active scooter (aka. Rides)
class CustomAdapter() :
    ListAdapter<Scooter, CustomAdapter.ViewHolder>(ScooterComparator()) {

    //Define a ViewHolder that uses a RecyclerView
    class ViewHolder(private val binding: ListRidesBinding) :
        RecyclerView.ViewHolder(binding.root) {

        //Bind the scooter information to the fields in our Recycler View
        fun bind(scooter: Scooter) {
            // Get the public thumbnail URL.
            val storage = Firebase.storage("gs://moapd-2023-cc929.appspot.com")
            val imageRef = storage.reference.child(scooter.URL)

            // Clean the image UI component.
            binding.image.setImageResource(0)

            // Download and set an image into the ImageView.
            imageRef.downloadUrl.addOnSuccessListener {
                Glide.with(itemView.context)
                    .load(it)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .centerCrop()
                    .into(binding.image)
            }

            //Bind the appropriate information of the scooter to the UI
            binding.name.text = scooter.name
            binding.location.text = scooter.location
            binding.timestamp.text = abs(scooter.startTime - scooter.endTime).toString() + " Kr."
            val startLocation = Pair(scooter.startLong,scooter.startLat)
            val currentLocation = Pair(scooter.currentLong,scooter.currentLat)
            binding.startLocation.text = "Longtitude: " + startLocation.first.toString() + " Latitude: " + startLocation.second.toString()
            binding.currentLocation.text = "Longtitude: " + currentLocation.first.toString() + " Latitude: " + currentLocation.second.toString()
        }
    }

    //Comparator that checks whether two scooters are the same.
    class ScooterComparator : DiffUtil.ItemCallback<Scooter>() {
        override fun areItemsTheSame(oldItem: Scooter, newItem: Scooter): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Scooter, newItem: Scooter): Boolean {
            return oldItem.name == newItem.name
        }
    }

    //Creates the ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Log.d("TAG()", "Creating a new ViewHolder.")

        val inflater = LayoutInflater.from(parent.context)
        val binding = ListRidesBinding.inflate(
            inflater, parent, false
        )
        return ViewHolder(binding)
    }

    //Populates an item to the ViewHolder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val scooter = getItem(position)
        Log.d("TAG()", "Populate an item at position: $position")
        holder.bind(scooter)
    }

    //Gets the amount of items in our ViewHolder
    override fun getItemCount() = currentList.size

}