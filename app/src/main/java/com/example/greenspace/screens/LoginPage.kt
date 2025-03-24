package com.example.greenspace.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
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

    private lateinit var signInBtn: Button
    private lateinit var googleBtn: Button
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var tvForgotPassword: TextView
    private lateinit var tvSignUp: TextView
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var firebaseAuth: FirebaseAuth

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        firebaseAuth = FirebaseAuth.getInstance()

        // Initialize UI elements
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        signInBtn = findViewById(R.id.btnSignIn)
        googleBtn = findViewById(R.id.btnGoogle)
        tvForgotPassword = findViewById(R.id.tvForgotPassword)
        tvSignUp = findViewById(R.id.tvSignUp)

        // Email Sign-In
        signInBtn.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
            } else {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            }
        }

        // Google Sign-In Configuration
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // Fetch client ID from google-services.json
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        googleBtn.setOnClickListener {
            signInGoogle()
        }

        // Forgot Password
        tvForgotPassword.setOnClickListener {
            val email = etEmail.text.toString().trim()
            if (email.isNotEmpty()) {
                resetPassword(email)
            } else {
                Toast.makeText(this, "Enter your email to reset password", Toast.LENGTH_SHORT).show()
            }
        }

        // Navigate to Sign-Up Page
        tvSignUp.setOnClickListener {
            startActivity(Intent(this, SignUp::class.java))
        }
    }

    private fun loginUser(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val username = email.substringBefore("@") // Extract username

                    SharedPreference.setEmail(this, email)
                    SharedPreference.setUsername(this, username)

                    Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, ImageCapture::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Login Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun resetPassword(email: String) {
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Password reset email sent", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun signInGoogle() {
        val signInIntent = mGoogleSignInClient.signInIntent
        signInLauncher.launch(signInIntent)
    }

    private val signInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleResult(task)
        } else {
            Toast.makeText(this, "Google Sign-in Failed!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            account?.let {
                updateProfileSection(it)
            }
        } catch (e: ApiException) {
            Toast.makeText(this, "Sign-in Failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateProfileSection(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val email = account.email ?: ""
                    val username = email.substringBefore("@") // Extract username from email
                    val profilePicUrl = account.photoUrl?.toString() ?: ""

                    SharedPreference.setEmail(this, email)
                    SharedPreference.setUsername(this, username)
                    SharedPreference.setProfilePic(this, profilePicUrl)

                    // ✅ Toast message for Google Sign-In success
                    Toast.makeText(this, "Google Sign-In Successful!", Toast.LENGTH_SHORT).show()

                    startActivity(Intent(this, ImageCapture::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Authentication Failed!", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onStart() {
        super.onStart()
        if (firebaseAuth.currentUser != null) {
            startActivity(Intent(this, ImageCapture::class.java))
            finish()
        }
    }
}
