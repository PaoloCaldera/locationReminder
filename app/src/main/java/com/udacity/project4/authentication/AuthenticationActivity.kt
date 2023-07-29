package com.udacity.project4.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
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

        // TODO: a bonus is to customize the sign in flow to look nice using :
        //https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md#custom-layout
    }

    /**
     * Variable identifying the result of the login request
     */
    private val loginLauncher = registerForActivityResult(FirebaseAuthUIActivityResultContract()) {
        if (it.resultCode == Activity.RESULT_OK) {
            // Go back into the RemindersActivity cleaning the back stack
            exitLogin()
        } else {
            Toast.makeText(
                this,
                "Unsuccessful login: ${it.idpResponse?.error?.errorCode}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Build the Firebase activity that is going to be used for the login request
     */
    private fun launchSignInFlow() {
        if (FirebaseAuth.getInstance().currentUser != null) {
            // If user is already logged, exit the login panel and go directly to RemindersActivity
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

    /**
     * Launch an intent towards the RemindersActivity when user is logged in
     */
    private fun exitLogin() {
        val intent = Intent(this, RemindersActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }
}