package com.brohit.smsbackup.domain.model

import com.google.gson.annotations.SerializedName

data class CallLogCustom(
    @SerializedName("call_type")
    val callType: String,
    @SerializedName("phone_number")
    val phoneNumber: String,
    @SerializedName("call_date")
    val callDate: String,
    @SerializedName("call_duration")
    val callDuration: String
)