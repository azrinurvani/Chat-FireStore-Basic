package com.azrinurvani.mobile.chatfirestorebasic.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.azrinurvani.mobile.chatfirestorebasic.databinding.ItemContainerRecentConversionBinding
import com.azrinurvani.mobile.chatfirestorebasic.listeners.ConversionListener
import com.azrinurvani.mobile.chatfirestorebasic.models.ChatMessage
import com.azrinurvani.mobile.chatfirestorebasic.models.User

class RecentConversationAdapters(
    private val listMessage: ArrayList<ChatMessage>,
    private val conversionListener: ConversionListener
) : RecyclerView.Adapter<RecentConversationAdapters.ConversionViewHolder>()  {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversionViewHolder {
        return ConversionViewHolder(
            ItemContainerRecentConversionBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount() = listMessage.size

    override fun onBindViewHolder(holder: ConversionViewHolder, position: Int) {
        holder.setData(listMessage[position])
    }

    fun getConversionImage(encodeImage:String?) : Bitmap?{
        val bytes = Base64.decode(encodeImage,Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes,0,bytes.size)
    }

    inner class ConversionViewHolder(private val binding : ItemContainerRecentConversionBinding) : RecyclerView.ViewHolder(binding.root){
        fun setData(data : ChatMessage){
            binding.imageProfile.setImageBitmap(getConversionImage(data.conversionImage.toString()))
            binding.tvName.setText(data.conversionName)
            binding.tvRecentMessage.setText(data.message)
            binding.root.setOnClickListener {
                val user = User()
                user.id = data.conversionId
                user.name = data.conversionName
                user.image = data.conversionImage
                conversionListener.onConversionClicked(user)
            }
        }
    }



}