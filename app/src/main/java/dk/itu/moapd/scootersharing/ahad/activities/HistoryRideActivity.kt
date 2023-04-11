package dk.itu.moapd.scootersharing.ahad.activities

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dk.itu.moapd.scootersharing.ahad.R
import dk.itu.moapd.scootersharing.ahad.databinding.ActivityHistoryRideBinding
import dk.itu.moapd.scootersharing.ahad.databinding.ActivityMainBinding
import dk.itu.moapd.scootersharing.ahad.fragments.HistoryRideFragment
import dk.itu.moapd.scootersharing.ahad.fragments.MainFragment

class HistoryRideActivity : AppCompatActivity() {

    private lateinit var mainBinding: ActivityHistoryRideBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityHistoryRideBinding.inflate(layoutInflater)

        val mainFragment = HistoryRideFragment()
        setContentView(mainBinding.root)

        supportFragmentManager
            .beginTransaction()
            .add(R.id.fragment_container_view,mainFragment)
            .commit()
    }
}