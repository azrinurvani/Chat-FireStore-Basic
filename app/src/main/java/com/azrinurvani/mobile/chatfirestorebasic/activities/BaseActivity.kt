package com.azrinurvani.mobile.chatfirestorebasic.activities

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.azrinurvani.mobile.chatfirestorebasic.utilities.KEY_AVAILABILITY
import com.azrinurvani.mobile.chatfirestorebasic.utilities.KEY_COLLECTION_USERS
import com.azrinurvani.mobile.chatfirestorebasic.utilities.KEY_USER_ID
import com.azrinurvani.mobile.chatfirestorebasic.utilities.PreferenceManager
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

open class BaseActivity : AppCompatActivity() {

    private var documentReference : DocumentReference? = null

//    override fun onStart() {
//        super.onStart()
//        val preferencesManager = PreferenceManager(this)
//        val database = FirebaseFirestore.getInstance()
//        documentReference = database.collection(KEY_COLLECTION_USERS).document(preferencesManager.getString(
//            KEY_USER_ID).toString())
//
//        Log.d(TAG, "onStart: is working..")
//
//
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val preferencesManager = PreferenceManager(this)
        val database = FirebaseFirestore.getInstance()
        documentReference = database.collection(KEY_COLLECTION_USERS).document(preferencesManager.getString(
            KEY_USER_ID).toString())

        Log.d(TAG, "onStart: is working..")

        Log.d(TAG, "onCreate: is working..")



    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause: is working..")
        documentReference?.update(KEY_AVAILABILITY,0)
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: is working..")
        documentReference?.update(KEY_AVAILABILITY,1)
    }

    companion object {
        private const val TAG = "BaseActivity"
    }
}