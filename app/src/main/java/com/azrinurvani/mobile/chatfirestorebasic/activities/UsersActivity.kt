package com.azrinurvani.mobile.chatfirestorebasic.activities


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.azrinurvani.mobile.chatfirestorebasic.adapters.UsersAdapter

import com.azrinurvani.mobile.chatfirestorebasic.databinding.ActivityUsersBinding
import com.azrinurvani.mobile.chatfirestorebasic.listeners.UserListener
import com.azrinurvani.mobile.chatfirestorebasic.models.User
import com.azrinurvani.mobile.chatfirestorebasic.utilities.KEY_COLLECTION_USERS
import com.azrinurvani.mobile.chatfirestorebasic.utilities.KEY_EMAIL
import com.azrinurvani.mobile.chatfirestorebasic.utilities.KEY_FCM_TOKEN
import com.azrinurvani.mobile.chatfirestorebasic.utilities.KEY_IMAGE
import com.azrinurvani.mobile.chatfirestorebasic.utilities.KEY_NAME
import com.azrinurvani.mobile.chatfirestorebasic.utilities.KEY_USER
import com.azrinurvani.mobile.chatfirestorebasic.utilities.KEY_USER_ID
import com.azrinurvani.mobile.chatfirestorebasic.utilities.PreferenceManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.log


class UsersActivity : AppCompatActivity(), UserListener {

    private lateinit var binding : ActivityUsersBinding
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferenceManager = PreferenceManager(applicationContext)
        getUsers()
        listener()
    }

    private fun listener(){
        binding.apply {
            imageBack.setOnClickListener {
                onBackPressed()
            }
        }
    }

    private fun getUsers(){
        loading(true)
        val database = FirebaseFirestore.getInstance()
        database.collection(KEY_COLLECTION_USERS)
            .get()
            .addOnCompleteListener {task->
                loading(false)
                val currentUserId = preferenceManager.getString(KEY_USER_ID)
                if (task.isSuccessful && task.result != null){
                    val listUser = ArrayList<User>()
                    for (queryDocumentSnapshot in task.result){
                        if (currentUserId.equals(queryDocumentSnapshot.id)){
                            continue
                        }
                        val user = User()
                        user.name = queryDocumentSnapshot.getString(KEY_NAME)
                        user.email = queryDocumentSnapshot.getString(KEY_EMAIL)
                        user.image = queryDocumentSnapshot.getString(KEY_IMAGE)
                        user.token = queryDocumentSnapshot.getString(KEY_FCM_TOKEN)
                        user.id = queryDocumentSnapshot.id
                        listUser.add(user)
                        Log.d(TAG, "getUsers: name : ${user.name}")
                    }

                    if (listUser.size>0){
                        val userAdapter = UsersAdapter(listUser,this)
                        Log.d(TAG, "getUsers: ${listUser[0].email}")
                        Log.d(TAG, "adapter icon con : ${userAdapter.itemCount}")
                        binding.rvUsers.visibility = View.VISIBLE
                        binding.rvUsers.adapter = userAdapter
                        binding.rvUsers.layoutManager = LinearLayoutManager(this)

                    }else{
                        showErrorMessage("No user available")
                    }

                }
            }
            .addOnFailureListener {
                showErrorMessage(it.message.toString())
            }
    }

    private fun showErrorMessage(message:String){
        binding.tvErrorMessage.setText(String.format("%s",message))
        binding.tvErrorMessage.visibility = View.VISIBLE
    }

    private fun loading(isLoading : Boolean){
        if (isLoading){
            binding.progressBar.visibility = View.VISIBLE
        }else{
            binding.progressBar.visibility = View.INVISIBLE
        }
    }

    companion object {
        private const val TAG = "UsersActivity"
    }

    override fun onUserClicked(user: User) {
        val intent = Intent(applicationContext,ChatActivity::class.java)
        intent.putExtra(KEY_USER,user)
        startActivity(intent)
        finish()
    }
}