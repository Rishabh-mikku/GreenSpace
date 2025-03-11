package com.example.greenspace

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

class LoginPage : AppCompatActivity() {

    private lateinit var signInBtn : Button
    private lateinit var googleBtn : Button
    lateinit var mGoogleSignInClient : GoogleSignInClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        signInBtn = findViewById(R.id.btnSignIn)
        signInBtn.setOnClickListener {
            val intent = Intent(this, ImageCapture::class.java)
            startActivity(intent)
        }

        googleBtn = findViewById(R.id.btnGoogle)

    }
}