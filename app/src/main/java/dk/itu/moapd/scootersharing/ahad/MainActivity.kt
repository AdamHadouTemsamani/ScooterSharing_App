package dk.itu.moapd.scootersharing.ahad

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.HapticFeedbackConstants
import android.widget.Button
import android.widget.EditText
import androidx.core.view.WindowCompat
import dk.itu.moapd.scootersharing.ahad.databinding.ActivityMainBinding
import dk.itu.moapd.scootersharing.ahad.databinding.ContentLayoutBinding
import com.google.android.material.snackbar.Snackbar


class MainActivity : AppCompatActivity() {

    // A set of private constants used in this class
    companion object {
        private val TAG = MainActivity::class.qualifiedName
    }

    private lateinit var mainBinding: ActivityMainBinding
    private lateinit var contentBinding: ContentLayoutBinding
    private val scooter: Scooter = Scooter("","")



    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        contentBinding = ContentLayoutBinding.bind(mainBinding.root)

        with (contentBinding) {
            startRideButton.setOnClickListener { view ->
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                addScooter()
            }
        }
        setContentView(mainBinding.root)
    }

    private fun showMessage() {
        //Snackbar :D
        Snackbar.make(mainBinding.root, scooter.toString(),Snackbar.LENGTH_LONG).show()
    }
    private fun addScooter() {
        with (contentBinding) {
            if ( editTextName.text.isNotEmpty() && editTextLocation.text.isNotEmpty()) {
                //Update the object attributes
                val name = editTextName.text.toString().trim()
                val location = editTextLocation.text.toString().trim()
                scooter.name = name
                scooter.location = location

                //Reset the text fields and update the UI
                editTextName.text.clear()
                editTextLocation.text.clear()
                showMessage()
            }
        }
    }
}