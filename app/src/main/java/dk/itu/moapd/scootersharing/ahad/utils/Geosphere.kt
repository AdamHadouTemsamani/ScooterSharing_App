package dk.itu.moapd.scootersharing.ahad.utils

import com.google.android.gms.maps.model.LatLng

//Class used to encapsulate a geofence.
// It includes name (name of scooter) and it position (latlng) and the radius of effect.
data class Geosphere(
    val name: String,
    val latlng: LatLng,
    val radius: Double
) {
}