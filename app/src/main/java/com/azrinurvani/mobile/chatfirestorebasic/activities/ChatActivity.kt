package com.azrinurvani.mobile.chatfirestorebasic.activities

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import com.azrinurvani.mobile.chatfirestorebasic.adapters.ChatAdapter

import com.azrinurvani.mobile.chatfirestorebasic.databinding.ActivityChatBinding
import com.azrinurvani.mobile.chatfirestorebasic.models.ChatMessage
import com.azrinurvani.mobile.chatfirestorebasic.models.User
import com.azrinurvani.mobile.chatfirestorebasic.utilities.KEY_COLLECTION_CHAT
import com.azrinurvani.mobile.chatfirestorebasic.utilities.KEY_MESSAGE
import com.azrinurvani.mobile.chatfirestorebasic.utilities.KEY_RECEIVER_ID
import com.azrinurvani.mobile.chatfirestorebasic.utilities.KEY_SENDER_ID
import com.azrinurvani.mobile.chatfirestorebasic.utilities.KEY_TIMESTAMP
import com.azrinurvani.mobile.chatfirestorebasic.utilities.KEY_USER
import com.azrinurvani.mobile.chatfirestorebasic.utilities.KEY_USER_ID
import com.azrinurvani.mobile.chatfirestorebasic.utilities.PreferenceManager
import com.azrinurvani.mobile.chatfirestorebasic.utilities.getSerializable
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import java.text.SimpleDateFormat
import java.util.Collections
import java.util.Date
import java.util.Locale


class ChatActivity : AppCompatActivity() {

    private lateinit var binding : ActivityChatBinding
    private var receiverUser : User? = null

    private val listChatMessage = ArrayList<ChatMessage>()
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var database: FirebaseFirestore
    private var chatAdapter: ChatAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        listener()
        loadReceiverDetails()
        init()
        listenMessages()
    }

    private fun init(){
        preferenceManager = PreferenceManager(this)
        //TODO - User Id adalah user yang login
        chatAdapter = preferenceManager.getString(KEY_USER_ID)?.let { userId ->
            Log.d(TAG, "init: keyUserId : $userId")

            ChatAdapter(listChatMessage,getBitmapFromEncodedString(receiverUser?.image.toString()), userId)
        }
        binding.rvChat.adapter = chatAdapter
        database = FirebaseFirestore.getInstance()
    }

    private fun sendMessage(){
       val messsage =  HashMap<String, Any>()
        preferenceManager.getString(KEY_USER_ID)?.let { messsage.put(KEY_SENDER_ID, it) }
        messsage[KEY_RECEIVER_ID] = receiverUser?.id.toString()
        messsage[KEY_MESSAGE] = binding.etInputMessage.text.toString()
        messsage[KEY_TIMESTAMP] = Date()

        database.collection(KEY_COLLECTION_CHAT).add(messsage)
        binding.etInputMessage.setText(null)
    }

    private fun listenMessages(){
        //TODO - GET List Chat Sender (Pengirim) chat by sender
        database.collection(KEY_COLLECTION_CHAT)
            .whereEqualTo(KEY_SENDER_ID,preferenceManager.getString(KEY_USER_ID))
            .whereEqualTo(KEY_RECEIVER_ID,receiverUser?.id)
            .addSnapshotListener(eventListener)

        //TODO - GET List Receiver (Penerima) chat by receiver
        database.collection(KEY_COLLECTION_CHAT)
            .whereEqualTo(KEY_SENDER_ID,receiverUser?.id)
            .whereEqualTo(KEY_RECEIVER_ID,preferenceManager.getString(KEY_USER_ID))
            .addSnapshotListener(eventListener)
    }

    @SuppressLint("NotifyDataSetChanged")
    private val eventListener : EventListener<QuerySnapshot> = EventListener { value, error ->
        if (error != null){
            return@EventListener
        }
        if (value!=null){
            val count = listChatMessage.size
            for (documentChange : DocumentChange in value.documentChanges){

                if (documentChange.type == DocumentChange.Type.ADDED){
                    val chatMessage = ChatMessage()
                    chatMessage.senderId = documentChange.document.getString(KEY_SENDER_ID)
                    chatMessage.receiverId = documentChange.document.getString(KEY_RECEIVER_ID)
                    chatMessage.message = documentChange.document.getString(KEY_MESSAGE)
                    chatMessage.dateTime = documentChange.document.getDate(KEY_TIMESTAMP)
                        ?.let { getReadableDateTime(it) }
                    chatMessage.dateObject = documentChange.document.getDate(KEY_TIMESTAMP)
                    listChatMessage.add(chatMessage)
                }
            }
//            Collections.sort(listChatMessage, Comparator { obj1, obj2 -> obj1.dateObject?.compareTo(obj2.dateObject)!! })
            //TODO - Sorting list yang akan dimunculkan berdasarkan date time
            listChatMessage.sortWith { obj1, obj2 -> obj1.dateObject?.compareTo(obj2.dateObject)!! }
            if (count==0){
                chatAdapter?.notifyDataSetChanged()
            }else{
                chatAdapter?.notifyItemRangeInserted(listChatMessage.size,listChatMessage.size)

                //TODO - smoothScrollToPosition --> Penambahan animasi direct scroll ke posisi sebelum dari data yang terakhur masuk
                binding.rvChat.smoothScrollToPosition(listChatMessage.size - 1)
            }
            binding.rvChat.visibility = View.VISIBLE
        }
        binding.progressBar.visibility = View.GONE

    }

    private fun getBitmapFromEncodedString(encodedImage:String?) : Bitmap?{
        val bytes = Base64.decode(encodedImage,Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes,0,bytes.size)
    }


    private fun loadReceiverDetails(){

        receiverUser = getSerializable(this,KEY_USER, User::class.java)
        binding.tvName.setText(receiverUser?.name)
    }

    private fun listener(){
        binding.apply {
            imageBack.setOnClickListener {
                onBackPressed()
            }
            layoutSend.setOnClickListener{
                sendMessage()
            }
        }
    }

    private fun getReadableDateTime(date:Date) : String{
        return SimpleDateFormat("MMMM dd, yyyy - hh:mm a",Locale.getDefault()).format(date)
    }

    companion object {
        private const val TAG = "ChatActivity"
    }
}