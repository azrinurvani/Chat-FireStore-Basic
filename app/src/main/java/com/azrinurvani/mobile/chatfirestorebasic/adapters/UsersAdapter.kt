package com.azrinurvani.mobile.chatfirestorebasic.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.azrinurvani.mobile.chatfirestorebasic.databinding.ItemContainerUserBinding
import com.azrinurvani.mobile.chatfirestorebasic.listeners.UserListener
import com.azrinurvani.mobile.chatfirestorebasic.models.User

class UsersAdapter(private val listUser : ArrayList<User>,private val userListener: UserListener) : RecyclerView.Adapter<UsersAdapter.UserViewHolder>() {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemContainerUserBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return UserViewHolder(binding)
    }

    override fun getItemCount() = listUser.size

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.setUserData(listUser[position])
        Log.d(TAG, "setUserData: ${listUser[position]}")
    }



    inner class UserViewHolder(val binding : ItemContainerUserBinding) : RecyclerView.ViewHolder(binding.root) {

        fun setUserData(data : User?){
            Log.d(TAG, "setUserData: ${data?.name}")
            binding.apply {
                tvEmail.setText(data?.email)
                tvName.setText(data?.name)
                imageProfile.setImageBitmap(getUserImage(data?.image.toString()))
                Log.d(TAG, "setUserData: ${data?.name}")
                this.root.setOnClickListener {
                    data?.let { userData -> userListener.onUserClicked(userData) }
                }
            }
        }
    }

    private fun getUserImage(encodedImage : String) : Bitmap{
        val bytes = Base64.decode(encodedImage,Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes,0,bytes.size)
    }


    companion object {
        private const val TAG = "UsersAdapter"
    }
}