package com.example.chattingapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        ID_AL_LB.setOnClickListener {
            val email = ID_AL_EM.text.toString()
            val pwd = ID_AL_PW.text.toString()
            Log.d("gagan","the email is :$email")
            Log.d("gagan","the password is :$pwd")

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email,pwd)
                .addOnCompleteListener {
                    if(! it.isSuccessful) return@addOnCompleteListener
                    //else
                    Log.d("gagan","the user has logged in with id:${it.result!!.user!!.uid}")
                    Toast.makeText(this,"You have Logged in Successfully!!!", Toast.LENGTH_SHORT).show()
                    val intent =Intent(this,LatestMessActivity::class.java)
                    intent.flags =Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
                .addOnFailureListener{
                    Log.d("gagan","there was an error in logging :${it.message}")
                    Toast.makeText(this,"Error in LogIn !!!",Toast.LENGTH_SHORT).show()
                }
        }
        ID_BK_AL.setOnClickListener {
            finish()
        }
    }
}