package dk.itu.moapd.scootersharing.ahad.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dk.itu.moapd.scootersharing.ahad.databinding.ListPreviousRidesBinding
import dk.itu.moapd.scootersharing.ahad.model.History

class HistoryRideAdapter() :
    ListAdapter<History, HistoryRideAdapter.ViewHolder>(HistoryComparator()) {

        private lateinit var deletedPreviousRide: History
        private var previousRideIndex: Int = 0

        class ViewHolder(private val binding: ListPreviousRidesBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(previousRides : History) {
                    binding.name.text = previousRides.name
                    binding.location.text = previousRides.location
                    binding.time.text = previousRides.time.toString()
                    binding.price.text = previousRides.price.toString()
            }
        }

        class HistoryComparator : DiffUtil.ItemCallback<History>() {
            override fun areItemsTheSame(oldItem: History, newItem: History): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: History, newItem: History): Boolean {
                return oldItem.name == newItem.name
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            Log.d("TAG()", "Creating a new ViewHolder.")

            val inflater = LayoutInflater.from(parent.context)
            val binding = ListPreviousRidesBinding.inflate(
                inflater, parent, false
            )
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val previousRides = getItem(position)
            Log.d("TAG()", "Populate an item at position: $position")
            holder.bind(previousRides)
        }

        override fun getItemCount() = currentList.size

    }