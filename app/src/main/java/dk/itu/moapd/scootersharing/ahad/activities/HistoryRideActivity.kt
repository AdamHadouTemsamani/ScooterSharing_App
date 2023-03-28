package dk.itu.moapd.scootersharing.ahad.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dk.itu.moapd.scootersharing.ahad.R
import dk.itu.moapd.scootersharing.ahad.databinding.ActivityMainBinding
import dk.itu.moapd.scootersharing.ahad.fragments.MainFragment

class HistoryRideActivity : AppCompatActivity() {

    private lateinit var mainBinding: ActivityHistoryRideBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)

        val mainFragment = MainFragment()
        setContentView(mainBinding.root)

        supportFragmentManager
            .beginTransaction()
            .add(R.id.fragment_container_view,mainFragment)
            .commit()
    }
}