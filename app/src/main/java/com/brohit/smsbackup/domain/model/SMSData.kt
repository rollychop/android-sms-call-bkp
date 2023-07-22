package com.brohit.smsbackup.domain.model

import com.google.gson.annotations.SerializedName

data class SMSData(
    @SerializedName("_id")
    val id: String,
    @SerializedName("thread_id")
    val threadId: String,
    @SerializedName("address")
    val address: String,
    @SerializedName("person")
    val person: String,
    @SerializedName("date")
    val date: String,
    @SerializedName("date_sent")
    val dateSent: String,
    @SerializedName("protocol")
    val protocol: String,
    @SerializedName("read")
    val read: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("reply_path_present")
    val replyPathPresent: String,
    @SerializedName("subject")
    val subject: String,
    @SerializedName("body")
    val body: String,
    @SerializedName("service_center")
    val serviceCenter: String,
    @SerializedName("locked")
    val locked: String,
    @SerializedName("sub_id")
    val subId: String,
    @SerializedName("error_code")
    val errorCode: String,
    @SerializedName("creator")
    val creator: String,
    @SerializedName("seen")
    val seen: String
)