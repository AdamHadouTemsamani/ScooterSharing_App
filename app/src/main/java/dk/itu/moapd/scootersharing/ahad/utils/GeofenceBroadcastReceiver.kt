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


class GeofenceBroadcastReceiver(view: View) : BroadcastReceiver() {

    companion object {
        private const val TAG = "GeofenceBroadcastReceiver"
    }


    override fun onReceive(context: Context?, intent: Intent?) {
        Log.i(TAG,"biggest bruh moment in the entire existence")

        val geofencingEvent = GeofencingEvent.fromIntent(intent!!)
        if (geofencingEvent!!.hasError()) {
            Log.i(TAG, "onReceive: Error receiving geofence event... please help...")
            return
        }
        val geofenceList = geofencingEvent.triggeringGeofences
        for (geofence in geofenceList!!) {
            Log.i(TAG, "onReceive: " + geofence.requestId)
        }

        val location = geofencingEvent.getTriggeringLocation();
        val transitionType = geofencingEvent.geofenceTransition
        when (transitionType) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                Toast.makeText(context, "GEOFENCE_TRANSITION_ENTER", Toast.LENGTH_SHORT).show()
                    Log.i(TAG,"I have entered the geofence")
            }
            Geofence.GEOFENCE_TRANSITION_DWELL -> {
                Toast.makeText(context, "GEOFENCE_TRANSITION_DWELL", Toast.LENGTH_SHORT).show()
            }
            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                Toast.makeText(context, "GEOFENCE_TRANSITION_EXIT", Toast.LENGTH_SHORT).show()
                Log.i(TAG, "I have exited the geofence")
            }
        }
    }


    private fun showMessage(view: View, message: String) {
        //Snackbar :D
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).show()
    }

}