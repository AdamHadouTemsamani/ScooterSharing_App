package dk.itu.moapd.scootersharing.ahad.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dk.itu.moapd.scootersharing.ahad.model.Scooter
import dk.itu.moapd.scootersharing.ahad.databinding.ListRidesBinding

class CustomAdapter(private val data: ArrayList<Scooter>) :
    RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

    private lateinit var deletedScooter: Scooter
    private var scooterIndex: Int = 0

    class ViewHolder(private val binding: ListRidesBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(scooter: Scooter) {
            binding.name.text = scooter.name
            binding.location.text = scooter.location
            binding.timestamp.text = scooter.timestamp.toString()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListRidesBinding.inflate(
            inflater, parent, false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val scooter = data[position]
        holder.bind(scooter)
    }

    fun removeAt(position: Int) {
        deletedScooter = data.get(position)
        scooterIndex = position

        data.removeAt(position)
        notifyItemRemoved(position)
    }

    fun AddScooter(scooter: Scooter) {
        data.add(scooter)
        notifyItemInserted(data.size)
    }

}