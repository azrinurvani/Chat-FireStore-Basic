package com.azrinurvani.mobile.chatfirestorebasic.listeners

import com.azrinurvani.mobile.chatfirestorebasic.models.User

interface UserListener  {
    fun onUserClicked(user : User)
}