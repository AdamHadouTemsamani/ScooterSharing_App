package dk.itu.moapd.scootersharing.ahad.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat.checkSelfPermission
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import dk.itu.moapd.scootersharing.ahad.fragments.LocationFragment
import java.util.ArrayList

class UserPermissions(_context: Context) {

    /*

    companion object {
        private const val ALL_PERMISSIONS_RESULT = 1011
    }

    private val context = _context

    private fun requestUserPermissions() {
        //An array with permissions.
        val permissions: ArrayList<String> = ArrayList()
        permissions.add(android.Manifest.permission.ACCESS_FINE_LOCATION)
        permissions.add(android.Manifest.permission.ACCESS_COARSE_LOCATION)

        //Check which permissions is needed to ask to the user.
        val permissionsToRequest = permissionsToRequest(permissions)

        //Show the permissions dialogue to the user.
        if (permissionsToRequest.size > 0)
            checkSelfPermission(permissionsToRequest.toTypedArray(), ALL_PERMISSIONS_RESULT)
    }

    private fun checkPermission() =
        context.let {
            ContextCompat.checkSelfPermission(
                it, Manifest.permission.ACCESS_FINE_LOCATION
            )
        } != PackageManager.PERMISSION_GRANTED &&
                context.let {
                    ContextCompat.checkSelfPermission(
                        it, Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                } != PackageManager.PERMISSION_GRANTED

    private fun permissionsToRequest(permissions: ArrayList<String>): ArrayList<String> {
        val result: ArrayList<String> = ArrayList()
        for (permission in permissions)
            if (context.let { PermissionChecker.checkSelfPermission(it, permission) } != PackageManager.PERMISSION_GRANTED)
                result.add(permission)

        return result
    }
    */

}