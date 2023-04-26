package dk.itu.moapd.scootersharing.ahad.utils

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.room.TypeConverter
import java.util.*

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return if (date == null) null else date.getTime()
    }




}