package dk.itu.moapd.scootersharing.ahad.application

import android.app.Application
import dk.itu.moapd.scootersharing.ahad.model.ScooterRepository

class ScooterApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ScooterRepository.initialize(this)
    }
}