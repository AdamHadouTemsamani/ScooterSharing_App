package dk.itu.moapd.scootersharing.ahad.activities;

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import dk.itu.moapd.scootersharing.ahad.R

//Activity that handles login for the user
class LoginActivity : AppCompatActivity() {

    //SignInLauncher using Firebase Auth
    private val signInLauncher =
        registerForActivityResult(
            FirebaseAuthUIActivityResultContract()
        ) { result ->
            onSignInResult(result) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createSignInIntent()
    }

    private fun createSignInIntent() {

        // Choose authentication providers.
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build())

        // Create and launch sign-in intent.
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setIsSmartLockEnabled(false)
            .setLogo(R.drawable.ic_launcher_background)
            .setTheme(R.style.Theme_ScooterSharing)
            .build()
        signInLauncher.launch(signInIntent)
    }

    private fun onSignInResult(
            result: FirebaseAuthUIAuthenticationResult
    ) {
        //Checks the result of Authentication.
        if (result.resultCode == RESULT_OK) {
            Log.d("TAG", "Authentication Successful")
            startMainActivity()
        } else
            Log.d("TAG", "Authentication Failed")
    }

    //Starts MainActivity by using an Intent
    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

}
