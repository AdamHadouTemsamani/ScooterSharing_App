package dk.itu.moapd.scootersharing.ahad.service

import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.*
import android.util.Log
import androidx.core.content.PermissionChecker
import com.google.android.gms.location.*


/* We have taken inspiration on how to make a Location service, from the github repo given by Fabrizio:
* https://github.com/android/location-samples/blob/432d3b72b8c058f220416958b444274ddd186abd/LocationUpdatesForegroundService/app/src/main/java/com/google/android/gms/location/sample/locationupdatesforegroundservice/LocationUpdatesService.java */

class LocationService : Service() {

    companion object {
        private val TAG = LocationService::class.java.simpleName

        private const val PACKAGE_NAME = "dk.itu.moapd.scootersharing.ahad"

        val EXTRA_LOCATION = "$PACKAGE_NAME.location"
        val ACTION_BROADCAST = "$PACKAGE_NAME.broadcast"

        private const val EXTRA_STARTED_FROM_NOTIFICATION =
            PACKAGE_NAME + ".started_from_notification"
        private const val KEY_REQUESTING_LOCATION_UPDATES = "requesting_locaction_updates"
    }

    //Updating data interval
    private val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 3000

    //Used for getting location from the user
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var mLocationCallback: LocationCallback? = null

    override fun onBind(intent: Intent?): IBinder {
        requestLocationUpdates()
        return LocalBinder()
    }

    var mLocation: Location? = null

    fun requestLocationUpdates() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)
                result.lastLocation?.let {
                    mLocation = it
                }

            }
        }
    }


    fun subscribeToService(init: (Location) -> Unit) {
        if (checkPermission()) return
        // Sets the accuracy and desired interval for active location updates.
        val locationRequest = LocationRequest
            .Builder(Priority.PRIORITY_HIGH_ACCURACY, UPDATE_INTERVAL_IN_MILLISECONDS)
            .build()

        mFusedLocationClient?.lastLocation?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                mLocation = task.result
                init(task.result)
            }
        }
        mLocationCallback?.let {
            mFusedLocationClient?.requestLocationUpdates(
                locationRequest,
                it,
                Looper.getMainLooper()
            )
        }
    }

    fun unsubscribeToService() {
        mLocationCallback?.let { mFusedLocationClient?.removeLocationUpdates(it) }
    }

    inner class LocalBinder : Binder() {
        fun getService(): LocationService {
            return this@LocationService;
        }
    }


    fun checkPermission() =
        PermissionChecker.checkSelfPermission(
            this, android.Manifest.permission.ACCESS_FINE_LOCATION
        ) != PermissionChecker.PERMISSION_GRANTED &&
                PermissionChecker.checkSelfPermission(
                    this, android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PermissionChecker.PERMISSION_GRANTED
}