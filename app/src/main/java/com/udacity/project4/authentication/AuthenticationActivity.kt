package com.udacity.project4.authentication

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.locationreminders.RemindersActivity

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)

        val loginButton = findViewById<Button>(R.id.login_button)
        loginButton.setOnClickListener {
            launchSignInFlow()
        }

        // TODO: Implement the create account and sign in using FirebaseUI,
        //  use sign in using email and sign in using Google

        // TODO: If the user was authenticated, send him to RemindersActivity

        // TODO: a bonus is to customize the sign in flow to look nice using :
        //https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md#custom-layout
    }

    private val loginLauncher = registerForActivityResult(FirebaseAuthUIActivityResultContract()) {
        if (it.resultCode == Activity.RESULT_OK) {
            Log.i(TAG,"Successful login of ${FirebaseAuth.getInstance().currentUser?.displayName}")
            // Go back into the RemindersActivity cleaning the back stack
            exitLogin()
        } else {
            Log.i(TAG, "Unsuccessful login: ${it.idpResponse?.error?.errorCode}")
        }
    }


    private fun launchSignInFlow() {

        if (FirebaseAuth.getInstance().currentUser != null) {
            exitLogin()
            return
        }

        // Authentication providers
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        val loginIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setTheme(R.style.AppTheme)
            .build()

        loginLauncher.launch(loginIntent)
    }

    private fun exitLogin() {
        supportFragmentManager.popBackStack(RemindersActivity::class.java.name, 0)
    }

    companion object {
        const val LOGIN_REQUEST_CODE = 1001
        private const val TAG = "AuthenticationActivity"
    }
}