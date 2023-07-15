package com.brohit.smsbackup.ui.screen

import android.content.Context
import android.content.ContextWrapper
import android.database.Cursor
import android.provider.CallLog
import android.provider.Telephony
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


private const val TAG = "SmsLogViewModel"


data class SMSLogScreenState(
    val loading: Boolean = false,
    val error: String = "",
    val message: String = "",
    val smsBkpS: List<String> = emptyList(),
    val logBkpS: List<String> = emptyList()
)

class SmsLogViewModel : ViewModel() {

    companion object {
        const val smsDirName = "sms-bkp"
        const val callLogDir = "call-logs-bkp"

    }

    private val _state = MutableStateFlow(SMSLogScreenState())
    val state = _state.asStateFlow()


    private val gson by lazy { Gson() }


    @Volatile
    private var isRunning = false
    fun saveAllSms(context: Context) {

        if (isRunning) {
            _state.value = SMSLogScreenState(
                loading = true,
                error = "Already Process Running",
                logBkpS = state.value.logBkpS,
                smsBkpS = state.value.smsBkpS
            )
        }
        isRunning = true
        _state.value = SMSLogScreenState(
            loading = true,
            logBkpS = state.value.logBkpS,
            smsBkpS = state.value.smsBkpS
        )
        val obbPath = (context as ContextWrapper).obbDir.absolutePath




        synchronized(this) {
            val cr = context.contentResolver
            viewModelScope.launch(Dispatchers.IO) {
                val smsDir = File(obbPath, smsDirName).also { it.mkdir() }

                val dateTime = LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("dd_MMM_yy-hh-mm-ss")
                )
                val bkpFile = File(
                    smsDir,
                    "SMS-bkp--$dateTime.json"
                )


                delay(1000)
                BufferedWriter(FileWriter(bkpFile)).use { br ->
                    cr.query(
                        Telephony.Sms.CONTENT_URI,
                        null, null,
                        null, null
                    ).use { c ->
                        if (c != null) {
                            if (c.moveToFirst()) {
                                val totalSMS = c.count
                                br.append('[')
                                Log.d(TAG, "getAllSms: $totalSMS")
                                for (j in 0 until totalSMS) {
                                    br.append(gson.toJson(getSMSData(c), SMSData::class.java))
                                    if (c.moveToNext()) {
                                        br.append(',')
                                    }
                                }
                                br.append(']')
                                br.flush()
                            }

                        } else {
                            _state.value = SMSLogScreenState(
                                error = "No message to show!",
                                logBkpS = state.value.logBkpS,
                                smsBkpS = state.value.smsBkpS
                            )
                        }
                    }
                }

                isRunning = false
                _state.value = SMSLogScreenState(
                    message = "Saved at $obbPath",
                    logBkpS = state.value.logBkpS,
                    smsBkpS = state.value.smsBkpS
                )
            }
        }
    }

    fun saveCallLogs(context: Context) {

        if (isRunning) {
            _state.value = SMSLogScreenState(
                loading = true,
                error = "Already Process Running",
                logBkpS = state.value.logBkpS,
                smsBkpS = state.value.smsBkpS
            )
        }
        isRunning = true
        _state.value = SMSLogScreenState(
            loading = true,
            logBkpS = state.value.logBkpS,
            smsBkpS = state.value.smsBkpS
        )

        val obbPath = (context as ContextWrapper).obbDir.absolutePath

        val smsDir = File(obbPath, callLogDir).also { it.mkdir() }

        val dateTime = LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("dd_MMM_yy-hh-mm-ss")
        )
        val bkpFile = File(
            smsDir,
            "CALL-LOG-bkp--$dateTime.json"
        )


        synchronized(this) {
            viewModelScope.launch(Dispatchers.IO) {
                runCatching {
                    val br = BufferedWriter(FileWriter(bkpFile))
                    val c: Cursor = context.contentResolver.query(
                        CallLog.Calls.CONTENT_URI,
                        null, null, null, CallLog.Calls.DATE + " DESC"
                    ) ?: return@launch
                    try {
                        br.append('[')
                        c.moveToFirst()
                        val logCount = c.count

                        for (j in 0 until logCount) {
                            br.append(
                                gson.toJson(getCallLog(c), CallLogCustom::class.java)
                            )
                            if (c.moveToNext()) {
                                br.append(',')
                            }
                        }
                        br.append(']')
                    } catch (ioe: IOException) {
                        _state.value = SMSLogScreenState(
                            error = ioe.localizedMessage ?: "Error",
                            logBkpS = state.value.logBkpS,
                            smsBkpS = state.value.smsBkpS
                        )
                    } finally {
                        c.close()
                        br.close()
                    }

                    isRunning = false
                    _state.value = SMSLogScreenState(
                        message = "Saved at $obbPath",
                        logBkpS = state.value.logBkpS,
                        smsBkpS = state.value.smsBkpS
                    )
                }
            }
        }

    }


    @Volatile
    private var isScanning = false

    fun getSavedBkpS(context: Context) {
        if (isScanning) return
        viewModelScope.launch(Dispatchers.IO) {
            isScanning = true
            runCatching {
                val contextWrapper = context as ContextWrapper
                val smsDri = File(contextWrapper.obbDir.absolutePath, smsDirName)
                val logDir = File(contextWrapper.obbDir.absolutePath, callLogDir)
                if (smsDri.isDirectory) {
                    val files = smsDri.list()?.filter { it.contains(".json") }
                    _state.value = state.value.copy(smsBkpS = files ?: emptyList())
                }

                if (logDir.isDirectory) {
                    val files = logDir.list()?.filter { it.contains(".json") }
                    _state.value = state.value.copy(logBkpS = files ?: emptyList())
                }
            }.getOrElse {
                _state.value = state.value.copy(error = "Error saved getting Backups")
            }
            isScanning = false
        }
    }

    @Volatile
    private var deleting = false
    fun deleteBkp(bkpFile: String, context: Context) {
        if (deleting) return

        runCatching {
            viewModelScope.launch(Dispatchers.IO) {
                val contextWrapper = context as ContextWrapper
                val obbDir = contextWrapper.obbDir
                val fileDir =
                    File(
                        obbDir, if (bkpFile.contains("SMS", ignoreCase = true))
                            smsDirName else callLogDir
                    )

                val file = File(fileDir, bkpFile)
                if (file.delete()) {
                    _state.value = state.value.copy(message = "Deleted $bkpFile")

                } else {
                    _state.value = state.value.copy(error = "Can't Delete")
                }

                deleting = false
            }
        }.getOrElse {
            _state.value = state.value.copy(error = "Error deleting file")
        }
    }


}

data class CallLogCustom(
    val callType: String,
    val phoneNumber: String,
    val callDate: String,
    val callDuration: String
)

fun getCallLog(c: Cursor): CallLogCustom {
    val number = c.getColumnIndex(CallLog.Calls.NUMBER)
    val type = c.getColumnIndex(CallLog.Calls.TYPE)
    val date = c.getColumnIndex(CallLog.Calls.DATE)
    val duration = c.getColumnIndex(CallLog.Calls.DURATION)
    val callType = c.getString(type)
    val phNumber = c.getString(number)
    val callDate = c.getString(date)
    val callDuration = c.getString(duration)
    val dir = when (callType.toInt()) {
        CallLog.Calls.OUTGOING_TYPE -> "OUTGOING"
        CallLog.Calls.INCOMING_TYPE -> "INCOMING"
        CallLog.Calls.MISSED_TYPE -> "MISSED"
        else -> "NO TYPE"
    }
    return CallLogCustom(
        dir, phNumber, callDate, callDuration ?: "NO DURATION"
    )
}

data class SMSData(
    val id: String,
    val threadId: String,
    val address: String,
    val person: String,
    val date: String,
    val dateSent: String,
    val protocol: String,
    val read: String,
    val status: String,
    val type: String,
    val replyPathPresent: String,
    val subject: String,
    val body: String,
    val serviceCenter: String,
    val locked: String,
    val subId: String,
    val errorCode: String,
    val creator: String,
    val seen: String
) {
    override fun toString(): String {
        return "$id,$threadId,$address,$person,$date,$dateSent,$protocol,$read,$status,$type," +
                "$replyPathPresent,$subject,$body,$serviceCenter,$locked,$subId,$errorCode,$creator,$seen"
    }
}

fun getSMSData(c: Cursor): SMSData {
    val id: String = c.getString(c.getColumnIndexOrThrow(Telephony.Sms._ID))
    val threadId: String =
        c.getString(c.getColumnIndexOrThrow(Telephony.Sms.THREAD_ID))
    val address: String =
        c.getString(c.getColumnIndexOrThrow(Telephony.Sms.ADDRESS))
    val person: String =
        c.getString(c.getColumnIndexOrThrow(Telephony.Sms.PERSON)) ?: "NO PERSON"
    val date: String =
        c.getString(c.getColumnIndexOrThrow(Telephony.Sms.DATE))
    val dateSent: String =
        c.getString(c.getColumnIndexOrThrow(Telephony.Sms.DATE_SENT))
    val protocol: String =
        c.getString(c.getColumnIndexOrThrow(Telephony.Sms.PROTOCOL)) ?: "NO PROTOCOL"
    val read: String = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.READ))
    val status: String =
        c.getString(c.getColumnIndexOrThrow(Telephony.Sms.STATUS))
    val type: String = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.TYPE))
    val replyPathPresent: String =
        c.getString(c.getColumnIndexOrThrow(Telephony.Sms.REPLY_PATH_PRESENT)) ?: ""
    val subject: String =
        c.getString(c.getColumnIndexOrThrow(Telephony.Sms.SUBJECT)) ?: "NO SUBJECT"
    val body = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.BODY))
    val serviceCenter: String =
        c.getString(c.getColumnIndexOrThrow(Telephony.Sms.SERVICE_CENTER)) ?: ""
    val locked: String =
        c.getString(c.getColumnIndexOrThrow(Telephony.Sms.LOCKED))
    val subId: String =
        c.getString(c.getColumnIndexOrThrow(Telephony.Sms.SUBSCRIPTION_ID))
    val errorCode: String =
        c.getString(c.getColumnIndexOrThrow(Telephony.Sms.ERROR_CODE))
    val creator: String =
        c.getString(c.getColumnIndexOrThrow(Telephony.Sms.CREATOR))
    val seen: String = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.SEEN))

    return SMSData(
        id,
        threadId,
        address,
        person,
        date,
        dateSent,
        protocol,
        read,
        status,
        type,
        replyPathPresent,
        subject,
        body,
        serviceCenter,
        locked,
        subId,
        errorCode,
        creator,
        seen
    )
}