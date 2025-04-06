package com.greenspaceapp.greenspace.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.greenspaceapp.greenspace.R
import com.greenspaceapp.greenspace.SharedPreference
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth

class ProfileInfo : AppCompatActivity() {

    private lateinit var profilePic : ImageView
    private lateinit var tvEmail : TextView
    private lateinit var tvUsername : TextView
    private lateinit var logoutBtn : MaterialButton
    private lateinit var mGoogleSignInClient : GoogleSignInClient
    private lateinit var backArrow: ImageView
    private val auth by lazy {
        FirebaseAuth.getInstance()
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile_info)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        tvEmail = findViewById(R.id.email)
        tvUsername = findViewById(R.id.username)
        tvEmail.text = "${SharedPreference.getEmail(this)}"
        tvUsername.text = "${SharedPreference.getUsername(this)}"
        profilePic = findViewById(R.id.profile_image)
        backArrow = findViewById(R.id.back_arrow)
        val profilePicUrl = SharedPreference.getProfilePic(this)
        Glide.with(this)
            .load(profilePicUrl)
            .placeholder(R.drawable.featuregraphic)
            .into(profilePic)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("573923425585-535umccnvqrhujvq3cteetlul8akaol2.apps.googleusercontent.com")
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        logoutBtn = findViewById(R.id.logout_button)
        logoutBtn.setOnClickListener {
            mGoogleSignInClient.signOut().addOnCompleteListener {
                auth.signOut() // Sign out from Firebase Authentication as well
                SharedPreference.clearData(this) // Clear stored user data if needed

                val intent = Intent(this, LoginPage::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                Toast.makeText(this, "Logging Out !!!", Toast.LENGTH_SHORT).show()
                startActivity(intent)
                finish()
            }
        }

        backArrow.setOnClickListener {
            startActivity(Intent(this, ImageCapture::class.java))
        }
    }
}