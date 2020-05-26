package com.example.chattingapp
import android.app.Activity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_reg.*
import java.util.*

class RegActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reg)
        ID_BT_RL.setOnClickListener {
            performReg()
        }
        ID_HA_RL.setOnClickListener {
            Log.d("regAct","lets move to that layout")
            startActivity(Intent(this,LoginActivity::class.java))
        }
        ID_SP_AL.setOnClickListener {
            Log.d("regAct","trying to add a photo here")
            val intent =Intent(Intent.ACTION_PICK)
            intent.type="image/*"
            startActivityForResult(intent,0)
       }
   }
    var selectedPhotoURI: Uri? = null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK  && data != null){
            Log.d("regAct","the photo was selected")
            selectedPhotoURI= data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver,selectedPhotoURI)
            profile_image.setImageBitmap(bitmap)
            ID_SP_AL.alpha=0f
        }
    }
    private fun performReg(){
        val user_name = ID_UN_RL.text.toString()
        val email = ID_EM_RL.text.toString()
        val pwd = ID_PW_RL.text.toString()
        Log.d("regAct", "the value of username is :$user_name")
        Log.d("regAct", "the value of email is :$email")
        Log.d("regAct", "the value of password:$pwd")

        if(email =="" || pwd.length<6 || user_name == "") {
            Toast.makeText(this,"please enter the details properly",Toast.LENGTH_SHORT).show()
            return
        }
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,pwd)
            .addOnCompleteListener{
                if(!it.isSuccessful) return@addOnCompleteListener
                //else
                Log.d("regAct","Successfully created a user with UID:${it.result!!.user!!.uid} ")

                upload_selected_photo_to_firebase()
            }
            .addOnFailureListener{
                Log.d("regAct","Failed to create a user :${it.message}")
                Toast.makeText(this,"Error in  user registration",Toast.LENGTH_SHORT).show()
            }
    }
    private fun upload_selected_photo_to_firebase(){
        if(selectedPhotoURI == null) return
        val filename =UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")
        ref.putFile(selectedPhotoURI!!)
            .addOnSuccessListener {
                Log.d("regAct","Successfully uploaded the selected image into the firebase storage:${it.metadata?.path}")
                ref.downloadUrl.addOnSuccessListener {
                    saveUserToFirebaseDatabase(it.toString())
                }
            }
            .addOnFailureListener {
                Log.d("regAct","error in uploading the image into the firebase storage")
            }
    }
    private fun saveUserToFirebaseDatabase(profileImageUrl:String){
        val uid = FirebaseAuth.getInstance().uid ?:""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        val user = User(uid,ID_UN_RL.text.toString(),profileImageUrl)
        // Write a message to the database
        ref.setValue(user)
            .addOnSuccessListener {
                Log.d("regAct","Finally v have saved all the details in the firebase database")
                Toast.makeText(this,"The user has been registered Successfully!!!",Toast.LENGTH_SHORT).show()
                val intent =Intent(this,LatestMessActivity::class.java)
                intent.flags =Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .addOnFailureListener{
             Log.d("regAct","error in adding the details of the user into the firebase database")
            }
    }
}


