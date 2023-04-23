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

class CustomAdapter() :
    ListAdapter<Scooter, CustomAdapter.ViewHolder>(ScooterComparator()) {

    private lateinit var deletedScooter: Scooter
    private var scooterIndex: Int = 0

    class ViewHolder(private val binding: ListRidesBinding) :
        RecyclerView.ViewHolder(binding.root) {

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

            binding.name.text = scooter.name
            binding.location.text = scooter.location
            binding.timestamp.text = scooter.startTime.toString()
        }
    }

    class ScooterComparator : DiffUtil.ItemCallback<Scooter>() {
        override fun areItemsTheSame(oldItem: Scooter, newItem: Scooter): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Scooter, newItem: Scooter): Boolean {
            return oldItem.name == newItem.name
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Log.d("TAG()", "Creating a new ViewHolder.")

        val inflater = LayoutInflater.from(parent.context)
        val binding = ListRidesBinding.inflate(
            inflater, parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val scooter = getItem(position)
        Log.d("TAG()", "Populate an item at position: $position")
        holder.bind(scooter)
    }

    override fun getItemCount() = currentList.size

}