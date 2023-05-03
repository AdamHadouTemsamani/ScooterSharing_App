package dk.itu.moapd.scootersharing.ahad.utils

import com.google.android.gms.maps.model.LatLng

data class Geosphere(
    val name: String,
    val latlng: LatLng,
    val radius: Double
) {
}