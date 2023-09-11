package com.azrinurvani.mobile.chatfirestorebasic.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.azrinurvani.mobile.chatfirestorebasic.adapters.RecentConversationAdapters
import com.azrinurvani.mobile.chatfirestorebasic.databinding.ActivityMainBinding
import com.azrinurvani.mobile.chatfirestorebasic.listeners.ConversionListener
import com.azrinurvani.mobile.chatfirestorebasic.models.ChatMessage
import com.azrinurvani.mobile.chatfirestorebasic.models.User
import com.azrinurvani.mobile.chatfirestorebasic.utilities.KEY_COLLECTION_CONVERSATIONS
import com.azrinurvani.mobile.chatfirestorebasic.utilities.KEY_COLLECTION_USERS
import com.azrinurvani.mobile.chatfirestorebasic.utilities.KEY_FCM_TOKEN
import com.azrinurvani.mobile.chatfirestorebasic.utilities.KEY_IMAGE
import com.azrinurvani.mobile.chatfirestorebasic.utilities.KEY_IS_SIGNED_IN
import com.azrinurvani.mobile.chatfirestorebasic.utilities.KEY_LAST_MESSAGE
import com.azrinurvani.mobile.chatfirestorebasic.utilities.KEY_NAME
import com.azrinurvani.mobile.chatfirestorebasic.utilities.KEY_RECEIVER_ID
import com.azrinurvani.mobile.chatfirestorebasic.utilities.KEY_RECEIVER_IMAGE
import com.azrinurvani.mobile.chatfirestorebasic.utilities.KEY_RECEIVER_NAME
import com.azrinurvani.mobile.chatfirestorebasic.utilities.KEY_SENDER_ID
import com.azrinurvani.mobile.chatfirestorebasic.utilities.KEY_SENDER_IMAGE
import com.azrinurvani.mobile.chatfirestorebasic.utilities.KEY_SENDER_NAME
import com.azrinurvani.mobile.chatfirestorebasic.utilities.KEY_TIMESTAMP
import com.azrinurvani.mobile.chatfirestorebasic.utilities.KEY_USER
import com.azrinurvani.mobile.chatfirestorebasic.utilities.KEY_USER_ID
import com.azrinurvani.mobile.chatfirestorebasic.utilities.PreferenceManager
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.messaging.FirebaseMessaging


class MainActivity : BaseActivity(), ConversionListener {

    private lateinit var binding : ActivityMainBinding
    private lateinit var preferenceManager: PreferenceManager
    private val listConversations = ArrayList<ChatMessage>()
    private var conversationAdapters : RecentConversationAdapters? = null
    private var database: FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferenceManager = PreferenceManager(applicationContext)

        loadUserDetails()
        getToken()
        init()
        listener()
        listenConversations()
    }

    private fun init(){
        conversationAdapters = RecentConversationAdapters(listConversations,this)
        binding.rvConversations.adapter = conversationAdapters
        database = FirebaseFirestore.getInstance()
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

    private fun listenConversations(){
        database?.collection(KEY_COLLECTION_CONVERSATIONS)
            ?.whereEqualTo(KEY_SENDER_ID,preferenceManager.getString(KEY_USER_ID))
            ?.addSnapshotListener(eventListener)

        database?.collection(KEY_COLLECTION_CONVERSATIONS)
            ?.whereEqualTo(KEY_RECEIVER_ID,preferenceManager.getString(KEY_USER_ID))
            ?.addSnapshotListener(eventListener)
    }

    @SuppressLint("NotifyDataSetChanged")
    private val eventListener : EventListener<QuerySnapshot> = EventListener { value, error ->
        if (error!= null){
            return@EventListener
        }
        if (value!=null){
            for (documentChange in  value.documentChanges){

                if (documentChange.type == DocumentChange.Type.ADDED){
                    val senderId = documentChange.document.getString(KEY_SENDER_ID)
                    val receiverId = documentChange.document.getString(KEY_RECEIVER_ID)
                    val chatMessage = ChatMessage()
                    chatMessage.senderId = senderId
                    chatMessage.receiverId = receiverId
                    if (preferenceManager.getString(KEY_USER_ID) == senderId){
                        chatMessage.conversionImage = documentChange.document.get(KEY_RECEIVER_IMAGE).toString()
                        chatMessage.conversionName = documentChange.document.get(KEY_RECEIVER_NAME).toString()
                        chatMessage.conversionId = documentChange.document.get(KEY_RECEIVER_ID).toString()
                    }else{
                        chatMessage.conversionImage = documentChange.document.getString(KEY_SENDER_IMAGE)
                        chatMessage.conversionName = documentChange.document.getString(KEY_SENDER_NAME)
                        chatMessage.conversionImage = documentChange.document.getString(KEY_SENDER_ID)
                    }
                    chatMessage.message = documentChange.document.getString(KEY_LAST_MESSAGE)
                    chatMessage.dateObject = documentChange.document.getDate(KEY_TIMESTAMP)
                    listConversations.add(chatMessage)
                } else if (documentChange.type == DocumentChange.Type.MODIFIED){

                    val senderId = documentChange.document.getString(KEY_SENDER_ID)
                    val receiverId = documentChange.document.getString(KEY_RECEIVER_ID)

                    for (i in 0 until listConversations.size){
                        if (listConversations[i].senderId == senderId &&
                            listConversations[i].receiverId == receiverId)
                        {
                            listConversations[i].message = documentChange.document.getString(KEY_LAST_MESSAGE)
                            listConversations[i].dateObject = documentChange.document.getDate(KEY_TIMESTAMP)
                            break
                        }
                    }
                }

            }
            listConversations.sortWith { obj1, obj2 -> obj1.dateObject?.compareTo(obj2?.dateObject)!! }
            conversationAdapters?.notifyDataSetChanged()
            binding.rvConversations.smoothScrollToPosition(0)
            binding.rvConversations.visibility = View.VISIBLE
            binding.progressBar.visibility = View.GONE
        }
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

    override fun onConversionClicked(user: User) {
        val intent = Intent(this,ChatActivity::class.java)
        intent.putExtra(KEY_USER,user)
        startActivity(intent)
    }
}

