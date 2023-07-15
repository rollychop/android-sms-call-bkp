package com.brohit.smsbackup.ui.screen

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.Intent.CATEGORY_DEFAULT
import android.content.Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.Intent.FLAG_ACTIVITY_NO_HISTORY
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat


@OptIn(
    ExperimentalLayoutApi::class, ExperimentalFoundationApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
fun MainContent(state: SMSLogScreenState, onBackUpSms: () -> Unit, onBackUpCall: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {

        val context = LocalContext.current
        val permissions = arrayOf(
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_SMS
        )
        val launcherMultiplePermissions = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissionsMap ->
            val areGranted = permissionsMap.values.reduce { acc, next -> acc && next }
            if (areGranted.not()) {
                val intent = Intent(ACTION_APPLICATION_DETAILS_SETTINGS)
                with(intent) {
                    data = Uri.fromParts("package", context.packageName, null)
                    addCategory(CATEGORY_DEFAULT)
                    addFlags(FLAG_ACTIVITY_NEW_TASK)
                    addFlags(FLAG_ACTIVITY_NO_HISTORY)
                    addFlags(FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                }
                context.startActivity(intent)
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (state.logBkpS.isNotEmpty()) {
                    stickyHeader(key = "Saved Call logs") {
                        Text(text = "Saved Call logs")
                    }
                }
                items(state.logBkpS, key = { it }) { bkp ->
                    ListItem(
                        headlineText = { Text(text = bkp) },
                        modifier = Modifier,
                        colors = ListItemDefaults.colors(),
                        shadowElevation = 5.dp,
                        tonalElevation = 2.dp
                    )
                }

                if (state.smsBkpS.isNotEmpty()) {
                    stickyHeader(key = "Saved SMS logs") {
                        Text(text = "Saved SMS logs")
                    }
                }
                items(state.smsBkpS, key = { it }) { bkp ->
                    ListItem(
                        headlineText = { Text(text = bkp) },
                        modifier = Modifier,
                        colors = ListItemDefaults.colors(),
                        shadowElevation = 5.dp,
                        tonalElevation = 2.dp
                    )
                }


            }

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = {
                        checkAndRequestLocationPermissions(
                            context,
                            permissions,
                            launcherMultiplePermissions,
                            onBackUp = onBackUpSms
                        )
                    },
                    enabled = state.loading.not()
                ) {
                    Text(text = "Backup SMS")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = {
                        checkAndRequestLocationPermissions(
                            context,
                            permissions,
                            launcherMultiplePermissions,
                            onBackUp = onBackUpCall
                        )
                    },
                    enabled = state.loading.not()
                ) {
                    Text(text = "Backup Call Logs")
                }
            }
        }
        Text(
            text = state.error, color = MaterialTheme.colorScheme.error,
            modifier = Modifier.align(
                Alignment.BottomCenter
            )
        )
    }
}

@Composable
fun MainScreen(vm: SmsLogViewModel) {
    val context = LocalContext.current
    val state = vm.state.collectAsState().value
    LaunchedEffect(key1 = state.message) {
        vm.getSavedBkpS(context)
    }

    MainContent(state, onBackUpSms = { vm.saveAllSms(context) }, onBackUpCall = {
        vm.saveCallLogs(context)
    })
}

fun checkAndRequestLocationPermissions(
    context: Context,
    permissions: Array<String>,
    launcher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>,
    onBackUp: () -> Unit
) {
    if (permissions.all {
            ContextCompat.checkSelfPermission(
                context,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
    ) {
        onBackUp()
    } else {
        launcher.launch(permissions)
    }
}