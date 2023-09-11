package com.azrinurvani.mobile.chatfirestorebasic.models

import java.util.Date

data class ChatMessage(
     var senderId : String? = null,
     var receiverId : String? = null,
     var message : String? = null,
     var dateTime : String? = null,
     var dateObject : Date? = null,
     var conversionId : String? = null,
     var conversionName : String? = null,
     var conversionImage : String? = null,
)
