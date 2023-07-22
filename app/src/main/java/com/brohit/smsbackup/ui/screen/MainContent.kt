package com.brohit.smsbackup.ui.screen

import android.Manifest
import android.content.Context
import android.content.ContextWrapper
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat


@OptIn(
    ExperimentalLayoutApi::class, ExperimentalFoundationApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
fun MainContent(
    modifier: Modifier = Modifier,
    state: SMSLogScreenState,
    onBackUpSms: () -> Unit,
    onBackUpContact: () -> Unit,
    onBackUpCall: () -> Unit,
    onDeleteItemClick: (String) -> Unit = {},
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {

        val context = LocalContext.current
        val permissions = arrayOf(
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_SMS,
            Manifest.permission.READ_CONTACTS
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
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {

                if (state.smsBkpS.isNotEmpty()) {
                    stickyHeader(key = "Saved SMS logs") {
                        ListItem(headlineText = { Text(text = "Saved SMS logs") })
                    }
                }
                items(state.smsBkpS, key = { it }) { bkp ->
                    ListItem(
                        headlineText = { Text(text = bkp) },
                        modifier = Modifier,
                        colors = ListItemDefaults.colors(),
                        shadowElevation = 5.dp,
                        tonalElevation = 2.dp,
                        trailingContent = {
                            IconButton(onClick = { onDeleteItemClick(bkp) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Bkp Icon"
                                )
                            }
                        }
                    )
                }
                if (state.logBkpS.isNotEmpty()) {
                    stickyHeader(key = "Saved Call logs") {
                        ListItem(headlineText = { Text(text = "Saved Call logs") })
                    }
                }
                items(state.logBkpS, key = { it }) { bkp ->
                    ListItem(
                        headlineText = { Text(text = bkp) },
                        modifier = Modifier,
                        colors = ListItemDefaults.colors(),
                        shadowElevation = 5.dp,
                        tonalElevation = 2.dp,
                        trailingContent = {
                            IconButton(onClick = { onDeleteItemClick(bkp) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Bkp Icon"
                                )
                            }
                        }
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
                    Text(text = "Backup CallLogs")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = {
                        checkAndRequestLocationPermissions(
                            context,
                            permissions,
                            launcherMultiplePermissions,
                            onBackUp = onBackUpContact
                        )
                    },
                    enabled = state.loading.not()
                ) {
                    Text(text = "Backup Contact")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffoldContent(
    state: SMSLogScreenState,
    onBackUpSms: () -> Unit,
    onBackUpContact: () -> Unit,
    onBackUpCall: () -> Unit,
    onDeleteItemClick: (String) -> Unit = {},
    refreshList: () -> Unit = {}
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    var showInfo by remember {
        mutableStateOf(false)
    }
    val context = LocalContext.current as ContextWrapper

    if (showInfo) {
        AlertDialog(
            onDismissRequest = {
                showInfo = false
            },
            icon = { Icon(Icons.Filled.Info, contentDescription = null) },
            title = {
                Text(text = "About App")
            },
            text = {
                Text(
                    "This application just backups sms and call logs into json format" +
                            "it does not upload any backup data into server\n" +
                            "SMS backup saved into Android/obb/${context.packageName}/${SmsLogViewModel.smsDirName}\n" +
                            "Call Log backup saved into Android/obb/${context.packageName}/${SmsLogViewModel.callLogDir}"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showInfo = false
                    }
                ) {
                    Text("Dismiss")
                }
            }
        )
    }


    Scaffold(
        topBar = {
            MediumTopAppBar(
                title = { Text(text = "Call & SMS Backup") },
                scrollBehavior = scrollBehavior,
                actions = {
                    IconButton(onClick = {
                        showInfo = true
                    }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Know about"
                        )
                    }
                    IconButton(onClick = refreshList) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                }
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        MainContent(
            modifier = Modifier.padding(paddingValues),
            state = state, onBackUpSms = onBackUpSms,
            onBackUpCall = onBackUpCall,
            onBackUpContact = onBackUpContact,
            onDeleteItemClick = onDeleteItemClick,
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

    MainScaffoldContent(
        state = state,
        onBackUpSms = { vm.saveAllSms(context) },
        onBackUpContact = { vm.saveContact(context) },
        onBackUpCall = { vm.saveCallLogs(context) },
        onDeleteItemClick = { vm.deleteBkp(it, context) },
        refreshList = { vm.getSavedBkpS(context) }
    )
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