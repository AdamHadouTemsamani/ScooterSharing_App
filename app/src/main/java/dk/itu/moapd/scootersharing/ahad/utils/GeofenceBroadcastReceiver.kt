import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.google.android.material.snackbar.Snackbar
import dk.itu.moapd.scootersharing.ahad.activities.MapsActivity
import dk.itu.moapd.scootersharing.ahad.fragments.MapsFragment
import dk.itu.moapd.scootersharing.ahad.utils.NotificationHelper


class GeofenceBroadcastReceiver(context: Context?) : BroadcastReceiver() {

    companion object {
        private val TAG = GeofenceBroadcastReceiver::class.java.simpleName
    }

    private lateinit var key: String
    private lateinit var message: String

    override fun onReceive(context: Context?, intent: Intent?) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent!!)
        val geofencingTransition = geofencingEvent?.geofenceTransition

        if(geofencingTransition == Geofence.GEOFENCE_TRANSITION_EXIT ||
            geofencingTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
            geofencingTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {
            if (intent != null) {
                key = intent.getStringExtra("key")!!
                message = intent.getStringExtra("message")!!
                Toast.makeText(context,message,Toast.LENGTH_SHORT).show()
            }
            Log.i(TAG,"You have done something with the app so very nice!")
        }
    }


    private fun showMessage(view: View, message: String) {
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).show()
    }

}