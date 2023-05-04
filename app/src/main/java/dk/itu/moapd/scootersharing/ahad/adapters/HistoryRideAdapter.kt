package dk.itu.moapd.scootersharing.ahad.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import dk.itu.moapd.scootersharing.ahad.databinding.ListPreviousRidesBinding
import dk.itu.moapd.scootersharing.ahad.model.History

//Adapter for holding our the history of the user. (aka. previously active rides)
class HistoryRideAdapter() :
    ListAdapter<History, HistoryRideAdapter.ViewHolder>(HistoryComparator()) {

    //Define a ViewHolder that uses a RecyclerView
    class ViewHolder(private val binding: ListPreviousRidesBinding) :
        RecyclerView.ViewHolder(binding.root) {

        //Bind the History (previously active scooter) information to the fields in our Recycler View
        fun bind(previousRides: History) {
            // Get the public thumbnail URL.
            val storage = Firebase.storage("gs://moapd-2023-cc929.appspot.com")
            val imageRef = storage.reference.child(previousRides.URL)

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

            //Bind the appropriate information of the previously active scooter to the UI
            binding.name.text = previousRides.name
            binding.location.text = previousRides.location
            binding.time.text = previousRides.time.toString() + " Minutes"
            val startLocation = Pair(previousRides.startLong, previousRides.startLat)
            val endLocation = Pair(previousRides.endLong, previousRides.endLat)
            binding.startLocation.text =
                "Longtitude: " + startLocation.first.toString() + " Latitude: " + startLocation.second.toString()
            binding.endLocation.text =
                "Longtitude: " + endLocation.first.toString() + " Latitude: " + endLocation.second.toString()
            binding.price.text = previousRides.price.toString() + "Kr."
        }
    }

    //Comparator that checks whether two history objects are the same.
    class HistoryComparator : DiffUtil.ItemCallback<History>() {
        override fun areItemsTheSame(oldItem: History, newItem: History): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: History, newItem: History): Boolean {
            return oldItem.name == newItem.name
        }
    }

    //Creates the ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Log.d("TAG()", "Creating a new ViewHolder.")

        val inflater = LayoutInflater.from(parent.context)
        val binding = ListPreviousRidesBinding.inflate(
            inflater, parent, false
        )
        return ViewHolder(binding)
    }

    //Populates an item to the ViewHolder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val previousRides = getItem(position)
        Log.d("TAG()", "Populate an item at position: $position")
        holder.bind(previousRides)
    }

    //Gets the amount of items in our ViewHolder
    override fun getItemCount() = currentList.size

}