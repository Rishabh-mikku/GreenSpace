package com.example.greenspace.screens

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.greenspace.R
import com.example.greenspace.SharedPreference
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginPage : AppCompatActivity() {

    private lateinit var signInBtn : Button
    private lateinit var googleBtn : Button
    lateinit var mGoogleSignInClient : GoogleSignInClient
    private lateinit var firebaseAuth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        signInBtn = findViewById(R.id.btnSignIn)
        signInBtn.setOnClickListener {
            val intent = Intent(this, ImageCapture::class.java)
            startActivity(intent)
        }

        FirebaseApp.initializeApp(this)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("573923425585-535umccnvqrhujvq3cteetlul8akaol2.apps.googleusercontent.com")
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        firebaseAuth = FirebaseAuth.getInstance()
        googleBtn = findViewById(R.id.btnGoogle)
        googleBtn.setOnClickListener {
            Toast.makeText(this, "Logging In !!!", Toast.LENGTH_SHORT).show()
            signInGoogle()
        }
    }

    private fun signInGoogle() {
        val signInIntent = mGoogleSignInClient.signInIntent
        signInLauncher.launch(signInIntent)
    }

    private val signInLauncher = registerForActivityResult(ActivityResultContracts
        .StartActivityForResult()) { result ->
        val data = result.data
        if (result.resultCode == RESULT_OK && data != null) {
            val task: Task<GoogleSignInAccount> =
                GoogleSignIn.getSignedInAccountFromIntent(data)
            handleResult(task)
        } else {
            Toast.makeText(this, "Google Sign-in Failed !!!",
                Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            completedTask.getResult(ApiException::class.java)?.let { account ->
                updateProfileSection(account)
            }
        } catch (e: ApiException) {
            Toast.makeText(this, "Sign-in Failed: ${e.localizedMessage}",
                Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateProfileSection(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    SharedPreference.setEmail(this, account.email.toString())
                    SharedPreference.setUsername(this, account.displayName.toString())
                    SharedPreference.setProfilePic(this, account.photoUrl.toString())
                    val intent = Intent(this, ImageCapture::class.java)
                    startActivity(intent)
                    finish()
                }
            }
    }

    override fun onStart() {
        super.onStart()
        if (GoogleSignIn.getLastSignedInAccount(this) != null) {
            startActivity(Intent(this, ImageCapture::class.java))
            finish()
        }
    }
}
