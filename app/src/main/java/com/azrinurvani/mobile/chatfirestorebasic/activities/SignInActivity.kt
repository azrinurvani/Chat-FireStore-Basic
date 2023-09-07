package com.azrinurvani.mobile.chatfirestorebasic.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.azrinurvani.mobile.chatfirestorebasic.R
import com.azrinurvani.mobile.chatfirestorebasic.databinding.ActivitySignInBinding
import com.azrinurvani.mobile.chatfirestorebasic.utilities.KEY_COLLECTION_USERS
import com.azrinurvani.mobile.chatfirestorebasic.utilities.KEY_EMAIL
import com.azrinurvani.mobile.chatfirestorebasic.utilities.KEY_IMAGE
import com.azrinurvani.mobile.chatfirestorebasic.utilities.KEY_IS_SIGNED_IN
import com.azrinurvani.mobile.chatfirestorebasic.utilities.KEY_NAME
import com.azrinurvani.mobile.chatfirestorebasic.utilities.KEY_PASSWORD
import com.azrinurvani.mobile.chatfirestorebasic.utilities.KEY_USER_ID
import com.azrinurvani.mobile.chatfirestorebasic.utilities.PreferenceManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import java.util.Objects


class SignInActivity : AppCompatActivity() {

    private lateinit var binding : ActivitySignInBinding
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferenceManager = PreferenceManager(applicationContext)

        if (preferenceManager.getBoolean(KEY_IS_SIGNED_IN)){
            val intent = Intent(applicationContext,MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        listener()
        getToken()
    }

    private fun getToken(){
        FirebaseMessaging.getInstance().token.addOnSuccessListener {
            Log.d(TAG, "getToken: $it")
        }
    }

    private fun listener(){
        binding.apply {
            tvCreateNewAccount.setOnClickListener {
                startActivity(Intent(applicationContext,SignUpActivity::class.java))
            }
            btnSignIn.setOnClickListener {
                if (isValidSignInDetails()){
                    signIn()
                }
            }
        }
    }

    private fun signIn(){
        binding.apply {
            loading(true)
            val database = FirebaseFirestore.getInstance()
            database.collection(KEY_COLLECTION_USERS)
                .whereEqualTo(KEY_EMAIL,inputEmail.text.toString())
                .whereEqualTo(KEY_PASSWORD,inputPassword.text.toString())
                .get()
                .addOnCompleteListener { task->
                    if (task.isSuccessful &&
                        task.result != null &&
                        task.result.documents.size > 0
                    ){
                        val documentSnapshot = task.result.documents[0]
                        preferenceManager.putBoolean(KEY_IS_SIGNED_IN,true)
                        preferenceManager.putString(KEY_USER_ID,documentSnapshot.id)
                        preferenceManager.putString(KEY_NAME,documentSnapshot.getString(KEY_NAME))
                        preferenceManager.putString(KEY_IMAGE,documentSnapshot.getString(KEY_IMAGE))
                        val intent = Intent(applicationContext,MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    }else{
                        loading(false)
                        showToast("Unable to sign in")
                    }
                }
        }
    }

    private fun loading(isLoading : Boolean){
        if (isLoading){
            binding.btnSignIn.visibility = View.INVISIBLE
            binding.progressBar.visibility = View.VISIBLE
        }else{
            binding.btnSignIn.visibility = View.VISIBLE
            binding.progressBar.visibility = View.INVISIBLE
        }
    }

    private fun showToast(message:String){
        Toast.makeText(applicationContext,message,Toast.LENGTH_LONG).show()
    }

    private fun isValidSignInDetails() : Boolean{
        binding.apply {
            return if (inputEmail.text.toString().trim().isEmpty()){
                showToast("Enter email")
                false
            }else if (!Patterns.EMAIL_ADDRESS.matcher(inputEmail.text.toString()).matches()){
                showToast("Enter valid email")
                false
            }else if (inputPassword.text.toString().trim().isEmpty()){
                showToast("Enter password")
                false
            }else{
                true
            }
        }
    }



    companion object {
        private const val TAG = "SignInActivity"
    }
}