package com.azrinurvani.mobile.chatfirestorebasic.models

import java.io.Serializable

data class User(
    var id : String? = null,
    var name : String? = null,
    var image : String? = null,
    var email : String? =null,
    var token : String? =null,

): Serializable