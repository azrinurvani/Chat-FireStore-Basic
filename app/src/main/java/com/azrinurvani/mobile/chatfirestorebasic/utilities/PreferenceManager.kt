package com.azrinurvani.mobile.chatfirestorebasic.utilities

import android.content.Context

class PreferenceManager(private val context : Context) {
    private val sharedPreferences = context.getSharedPreferences(KEY_PREFERENCE_NAME,Context.MODE_PRIVATE)
    private val editor = sharedPreferences.edit()

    fun putBoolean(key:String,value : Boolean){
        editor.putBoolean(key,value)
        editor.apply()
    }

    fun getBoolean(key: String) : Boolean{
        return sharedPreferences.getBoolean(key,false)
    }

    fun putString(key:String?,value : String?){
        editor.putString(key,value)
        editor.apply()
    }

    fun getString(key:String) : String? {
        return sharedPreferences.getString(key,null)
    }

    fun clear(){
        editor.clear()
        editor.apply {  }
    }

    companion object {

    }
}