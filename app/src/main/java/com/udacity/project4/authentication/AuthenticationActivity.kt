package com.udacity.project4.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == LOGIN_REQUEST_CODE) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                Log.i(TAG,
                    "Successful login of ${FirebaseAuth.getInstance().currentUser?.displayName}"
                )
                // Go back into the RemindersActivity cleaning the back stack
                exitLogin()
            } else {
                Log.i(TAG, "Unsuccessful login: ${response?.error?.errorCode}")
            }
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

        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setTheme(R.style.AppTheme)
                .build(),
            LOGIN_REQUEST_CODE
        )
    }

    private fun exitLogin() {
        val intent = Intent(this, RemindersActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }

    companion object {
        const val LOGIN_REQUEST_CODE = 1001
        private const val TAG = "AuthenticationActivity"
    }
}