package com.azrinurvani.mobile.chatfirestorebasic.activities

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.azrinurvani.mobile.chatfirestorebasic.databinding.ActivitySignUpBinding
import com.azrinurvani.mobile.chatfirestorebasic.utilities.KEY_COLLECTION_USERS
import com.azrinurvani.mobile.chatfirestorebasic.utilities.KEY_EMAIL
import com.azrinurvani.mobile.chatfirestorebasic.utilities.KEY_IMAGE
import com.azrinurvani.mobile.chatfirestorebasic.utilities.KEY_IS_SIGNED_IN
import com.azrinurvani.mobile.chatfirestorebasic.utilities.KEY_NAME
import com.azrinurvani.mobile.chatfirestorebasic.utilities.KEY_PASSWORD
import com.azrinurvani.mobile.chatfirestorebasic.utilities.KEY_USER_ID
import com.azrinurvani.mobile.chatfirestorebasic.utilities.PreferenceManager
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding : ActivitySignUpBinding
    private lateinit var preferenceManager : PreferenceManager
    private var encodeImage : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferenceManager = PreferenceManager(applicationContext)
        listener()
    }

    private fun listener(){
        binding.apply {
            tvSignIn.setOnClickListener {
                startActivity(Intent(applicationContext,SignInActivity::class.java))
            }
            btnSignUp.setOnClickListener {
                if (isValidSignUpDetails()){
                    signUp()
                }
            }
            layoutImage.setOnClickListener {
                val intent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                pickImage.launch(intent)
            }
        }
    }

    private fun showToast(message : String){
        Toast.makeText(applicationContext,message,Toast.LENGTH_LONG).show()
    }
    private fun signUp(){
        binding.apply {
            loading(true)
            val database = FirebaseFirestore.getInstance()
            val user = HashMap<String,Any>()
            user[KEY_NAME] = inputName.text.toString()
            user[KEY_EMAIL] = inputEmail.text.toString()
            user[KEY_PASSWORD] = inputPassword.text.toString()
            user[KEY_IMAGE] = encodeImage.toString()
            database.collection(KEY_COLLECTION_USERS)
                .add(user)
                .addOnSuccessListener { documentReference->
                    loading(false)
                    preferenceManager.putBoolean(KEY_IS_SIGNED_IN, true)
                    preferenceManager.putString(KEY_USER_ID, documentReference.id)
                    preferenceManager.putString(KEY_NAME, inputName.text.toString())
                    preferenceManager.putString(KEY_IMAGE, encodeImage.toString())
                    val intent = Intent(applicationContext,MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                }.addOnFailureListener {
                    loading(false)
                    showToast(it.message.toString())
                }
        }

    }

    private fun encodeImage(bitmap:Bitmap) : String{
        val previewWidth = 150
        val previewHeight = bitmap.height * previewWidth / bitmap.width
        val previewBitmap = Bitmap.createScaledBitmap(bitmap,previewWidth,previewHeight,false)
        val byteOutputStream = ByteArrayOutputStream()
        previewBitmap.compress(Bitmap.CompressFormat.JPEG,50,byteOutputStream)
        val bytes = byteOutputStream.toByteArray()
        return Base64.encodeToString(bytes,Base64.DEFAULT)
    }

    private val pickImage : ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            if (result.data != null){
                val imageUri =  result?.data?.data
                try {
                    val inputStream = imageUri?.let { contentResolver.openInputStream(it) }
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    binding.imageProfile.setImageBitmap(bitmap)
                    binding.tvLabelAddImage.visibility = View.GONE
                    encodeImage = encodeImage(bitmap)
                }catch (e : FileNotFoundException){
                    e.printStackTrace()
                }
            }
        }
    }

    private fun isValidSignUpDetails() : Boolean {
        binding.apply {
            if (encodeImage==null){
                showToast("Select profile image")
                return false
            }else if(inputName.text.toString().trim().isEmpty()){
                showToast("Input name")
                return false
            }else if(inputEmail.text.toString().trim().isEmpty()){
                showToast("Input email")
                return false
            } else if (!Patterns.EMAIL_ADDRESS.matcher(inputEmail.text.toString()).matches()){
                showToast("Enter valid image")
                return false
            }
            else if(inputPassword.text.toString().trim().isEmpty()){
                showToast("Input password")
                return false
            }else if(inputConfirmPassword.text.toString().trim().isEmpty()){
                showToast("Input confirm password")
                return false
            }else if (inputPassword.text.toString() != inputConfirmPassword.text.toString()){
                showToast("Password and confirm password must be same")
                return false
            }else{
                return true
            }
        }
    }

    private fun loading(isLoading : Boolean){
        if (isLoading){
            binding.btnSignUp.visibility = View.INVISIBLE
            binding.progressBar.visibility = View.VISIBLE
        }else{
            binding.btnSignUp.visibility = View.VISIBLE
            binding.progressBar.visibility = View.INVISIBLE
        }
    }
}