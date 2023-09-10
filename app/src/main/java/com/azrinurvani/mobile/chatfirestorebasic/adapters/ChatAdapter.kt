package com.azrinurvani.mobile.chatfirestorebasic.adapters

import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.azrinurvani.mobile.chatfirestorebasic.databinding.ItemContainerChatReceivedBinding
import com.azrinurvani.mobile.chatfirestorebasic.databinding.ItemContainerSentMessageBinding
import com.azrinurvani.mobile.chatfirestorebasic.models.ChatMessage


class ChatAdapter(
    private val listChatMessage: ArrayList<ChatMessage>,
    private val receiverProfileImage: Bitmap?,
    private val senderId : String) : RecyclerView.Adapter<RecyclerView.ViewHolder>()
{


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == VIEW_TYPE_SENT){
            return SentMessageViewHolder(ItemContainerSentMessageBinding.inflate(
                LayoutInflater.from(parent.context),parent,false)
            )
        }else{
            return ReceivedMessageViewHolder(
                ItemContainerChatReceivedBinding.inflate(
                LayoutInflater.from(parent.context),parent,false)
            )
        }
    }

    override fun getItemCount() = listChatMessage.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == VIEW_TYPE_SENT){
            (holder as SentMessageViewHolder).setData(listChatMessage[position])
        }else{
            receiverProfileImage?.let {
                (holder as ReceivedMessageViewHolder).setData(listChatMessage[position],
                    it
                )
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (listChatMessage[position].senderId == senderId){
            return VIEW_TYPE_SENT
        }else{
            return VIEW_TYPE_RECEIVED
        }
    }



    class SentMessageViewHolder(val binding : ItemContainerSentMessageBinding) : RecyclerView.ViewHolder(binding.root){
        fun setData(chatMessage: ChatMessage) {
            binding.tvMessage.setText(chatMessage.message)
            binding.tvDateTime.setText(chatMessage.dateTime)
        }


    }

    class ReceivedMessageViewHolder(private val binding : ItemContainerChatReceivedBinding) : RecyclerView.ViewHolder(binding.root){
        fun setData(chatMessage : ChatMessage,receiverProfileImage : Bitmap){
            binding.tvMessage.setText(chatMessage.message)
            binding.tvDateTime.setText(chatMessage.dateTime)
            binding.imageProfile.setImageBitmap(receiverProfileImage)
        }


    }

    companion object {
        const val VIEW_TYPE_SENT = 1
        const val VIEW_TYPE_RECEIVED = 2
        const val TAG = "ChatAdapter"
    }
}