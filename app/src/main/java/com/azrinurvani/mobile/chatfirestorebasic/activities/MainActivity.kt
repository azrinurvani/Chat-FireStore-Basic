package com.azrinurvani.mobile.chatfirestorebasic.activities

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.azrinurvani.mobile.chatfirestorebasic.databinding.ActivityMainBinding
import com.azrinurvani.mobile.chatfirestorebasic.utilities.KEY_COLLECTION_USERS
import com.azrinurvani.mobile.chatfirestorebasic.utilities.KEY_FCM_TOKEN
import com.azrinurvani.mobile.chatfirestorebasic.utilities.KEY_IMAGE
import com.azrinurvani.mobile.chatfirestorebasic.utilities.KEY_IS_SIGNED_IN
import com.azrinurvani.mobile.chatfirestorebasic.utilities.KEY_NAME
import com.azrinurvani.mobile.chatfirestorebasic.utilities.KEY_USER_ID
import com.azrinurvani.mobile.chatfirestorebasic.utilities.PreferenceManager
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging


class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferenceManager = PreferenceManager(applicationContext)
        loadUserDetails()
        getToken()
        listener()
    }

    private fun listener(){
        binding.apply {
            imageSignOut.setOnClickListener {
                signOut()
            }
            binding.fabNewChat.setOnClickListener {
                startActivity(Intent(applicationContext,UsersActivity::class.java))

            }
        }
    }

    private fun loadUserDetails(){
        binding.apply {
            tvName.setText(preferenceManager.getString(KEY_NAME))
            val bytes = Base64.decode(preferenceManager.getString(KEY_IMAGE),Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.size)
            imageProfile.setImageBitmap(bitmap)
        }
    }

    private fun showToast(message:String){
        Toast.makeText(applicationContext,message,Toast.LENGTH_LONG).show()
    }

    private fun getToken(){
        FirebaseMessaging.getInstance().token.addOnSuccessListener(this::updateToken);
    }

    private fun updateToken(token:String){
        val database = FirebaseFirestore.getInstance()
        val documentReference = preferenceManager.getString(KEY_USER_ID)?.let {
            database.collection(KEY_COLLECTION_USERS)
                .document(
                    it
                )
        }
        documentReference?.update(KEY_FCM_TOKEN,token)
            ?.addOnFailureListener { e -> showToast("Unable to update token") }

    }

    private fun signOut(){
        showToast("Signing out...")
        val database = FirebaseFirestore.getInstance()
        val documentReference = preferenceManager.getString(KEY_USER_ID)?.let {
            database.collection(KEY_COLLECTION_USERS).document(
                it
            )
        }
        val updates = HashMap<String,Any>()
        updates[KEY_FCM_TOKEN] = FieldValue.delete()
        documentReference?.update(updates)?.addOnSuccessListener { unusued ->
            preferenceManager.putBoolean(KEY_IS_SIGNED_IN,false)
            preferenceManager.clear()
            startActivity(Intent(applicationContext,SignInActivity::class.java))
            finish()
        }?.addOnFailureListener {
            showToast("Unable to sign out")
        }
    }
}

