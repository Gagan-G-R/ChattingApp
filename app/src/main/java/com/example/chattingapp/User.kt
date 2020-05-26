package com.example.chattingapp

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class User(val uid:String,val username :String,val profileImgUrl:String):Parcelable{
    constructor():this("","","")
}