package com.azrinurvani.mobile.chatfirestorebasic.listeners

import com.azrinurvani.mobile.chatfirestorebasic.models.User

interface ConversionListener {
    fun onConversionClicked(user : User)
}