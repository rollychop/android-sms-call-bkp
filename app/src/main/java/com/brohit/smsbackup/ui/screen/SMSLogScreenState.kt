package com.brohit.smsbackup.ui.screen

data class SMSLogScreenState(
    val loading: Boolean = false,
    val error: String = "",
    val message: String = "",
    val smsBkpS: List<String> = emptyList(),
    val logBkpS: List<String> = emptyList()
)