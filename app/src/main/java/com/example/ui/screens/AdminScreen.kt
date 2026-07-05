package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.MainViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
    users: List<User>,
    publicKeys: List<PublicKeyEntity>,
    sessions: List<Session>,
    auditLogs: List<AuditLog>,
    clientKeys: List<ClientKey>
) {
    var adminTab by remember { mutableStateOf(0) } // 0: Users, 1: Keys, 2: Sessions, 3: Audits
    var showCreateUserDialog by remember { mutableStateOf(false) }

    var newUserName by remember { mutableStateOf("") }
    var newUserEmail by remember { mutableStateOf("") }
    var newUserRole by remember { mutableStateOf(UserRole.DEVELOPER) }

    val context = LocalContext.current

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        // Screen Header
        Text(
            text = "Administration Console",
            style = MaterialTheme.typography.titleLarge,
            color = TextWhite,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Real-time state viewer of the enterprise IAM database, active sessions, and immutable security audits.",
            style = MaterialTheme.typography.bodySmall,
            color = TextGray
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Tab Selector Row
        ScrollableTabRow(
            selectedTabIndex = adminTab,
            containerColor = CardSlate,
            contentColor = AardvarkCyan,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[adminTab]),
                    color = AardvarkCyan
                )
            },
            divider = {},
            edgePadding = 0.dp,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, CardSlateLight, RoundedCornerShape(24.dp))
        ) {
            Tab(
                selected = adminTab == 0,
                onClick = { adminTab = 0 },
                text = { Text("Users (${users.size})") },
                modifier = Modifier.testTag("admin_tab_users")
            )
            Tab(
                selected = adminTab == 1,
                onClick = { adminTab = 1 },
                text = { Text("Keys (${publicKeys.size})") },
                modifier = Modifier.testTag("admin_tab_keys")
            )
            Tab(
                selected = adminTab == 2,
                onClick = { adminTab = 2 },
                text = { Text("Sessions (${sessions.size})") },
                modifier = Modifier.testTag("admin_tab_sessions")
            )
            Tab(
                selected = adminTab == 3,
                onClick = { adminTab = 3 },
                text = { Text("Audits (${auditLogs.size})") },
                modifier = Modifier.testTag("admin_tab_audits")
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tab Content Display
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (adminTab) {
                0 -> UsersManagementTab(
                    users = users,
                    publicKeys = publicKeys,
                    clientKeys = clientKeys,
                    onAddUserClick = { showCreateUserDialog = true },
                    onToggleStatus = { user, active ->
                        viewModel.updateUserStatus(user.id, if (active) AccountStatus.ACTIVE else AccountStatus.DISABLED)
                    },
                    onRegisterKey = { userId, clientKey ->
                        viewModel.registerKeyOnServer(userId, clientKey)
                        Toast.makeText(context, "Registered ${clientKey.name} on server", Toast.LENGTH_SHORT).show()
                    }
                )
                1 -> KeysManagementTab(
                    publicKeys = publicKeys,
                    users = users,
                    onRevokeKey = { viewModel.revokeServerKey(it) },
                    onDeleteKey = { viewModel.deleteServerKey(it) }
                )
                2 -> SessionsManagementTab(
                    sessions = sessions,
                    users = users,
                    onRevokeSession = { viewModel.revokeServerSession(it) }
                )
                3 -> AuditLogTab(auditLogs = auditLogs)
            }
        }
    }

    // Create User Dialog
    if (showCreateUserDialog) {
        AlertDialog(
            onDismissRequest = { showCreateUserDialog = false },
            containerColor = CardSlate,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.PersonAdd, contentDescription = "Add", tint = AardvarkCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Register Server User", color = TextWhite, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = newUserName,
                        onValueChange = { newUserName = it },
                        label = { Text("Full Name") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AardvarkCyan,
                            unfocusedBorderColor = CardSlateLight,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("new_user_name_input")
                    )

                    OutlinedTextField(
                        value = newUserEmail,
                        onValueChange = { newUserEmail = it },
                        label = { Text("Enterprise Email Address") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AardvarkCyan,
                            unfocusedBorderColor = CardSlateLight,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("new_user_email_input")
                    )

                    Text(text = "Designated Role:", color = TextGray, style = MaterialTheme.typography.bodyMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        UserRole.values().forEach { role ->
                            val isSelected = newUserRole == role
                            FilterChip(
                                selected = isSelected,
                                onClick = { newUserRole = role },
                                label = { Text(text = role.name) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AardvarkCyan,
                                    selectedLabelColor = CardSlate,
                                    containerColor = CardSlateLight,
                                    labelColor = TextGray
                                )
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newUserName.isNotBlank() && newUserEmail.isNotBlank()) {
                            viewModel.createServerUser(newUserName, newUserEmail, newUserRole)
                            newUserName = ""
                            newUserEmail = ""
                            newUserRole = UserRole.DEVELOPER
                            showCreateUserDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AardvarkCyan)
                ) {
                    Text("Register User", color = CardSlate, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateUserDialog = false }) {
                    Text("Cancel", color = TextGray)
                }
            }
        )
    }
}

@Composable
fun UsersManagementTab(
    users: List<User>,
    publicKeys: List<PublicKeyEntity>,
    clientKeys: List<ClientKey>,
    onAddUserClick: () -> Unit,
    onToggleStatus: (User, Boolean) -> Unit,
    onRegisterKey: (String, ClientKey) -> Unit
) {
    var showRegisterKeyForUserId by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Server Users Database", style = MaterialTheme.typography.titleMedium, color = TextWhite, fontWeight = FontWeight.Bold)
            Button(
                onClick = onAddUserClick,
                colors = ButtonDefaults.buttonColors(containerColor = AardvarkCyan),
                modifier = Modifier.testTag("admin_register_user_button")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add user", tint = CardSlate)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Add User", color = CardSlate)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(users, key = { it.id }) { user ->
                val userKey = publicKeys.find { it.userId == user.id && it.status == KeyStatus.ACTIVE }
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSlate),
                    border = BorderStroke(1.dp, CardSlateLight)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = user.name, color = TextWhite, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .background(AardvarkSteel, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(text = user.role.name, style = MaterialTheme.typography.labelSmall, color = TextWhite)
                                    }
                                }
                                Text(text = user.email, color = TextGray, style = MaterialTheme.typography.bodySmall)
                            }

                            Switch(
                                checked = user.status == AccountStatus.ACTIVE,
                                onCheckedChange = { onToggleStatus(user, it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = StatusGreen,
                                    checkedTrackColor = StatusGreenBg
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Key status row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Key,
                                    contentDescription = "Key",
                                    tint = if (userKey != null) StatusGreen else TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (userKey != null) "PGP Fingerprint: ${userKey.fingerprint.take(19)}..." else "No public key uploaded",
                                    color = if (userKey != null) TextWhite else TextMuted,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            
                            if (userKey == null && clientKeys.isNotEmpty()) {
                                TextButton(
                                    onClick = { showRegisterKeyForUserId = user.id },
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("Upload PGP Key", color = AardvarkCyan, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Register Public Key from local list Dialog
    if (showRegisterKeyForUserId != null) {
        AlertDialog(
            onDismissRequest = { showRegisterKeyForUserId = null },
            containerColor = CardSlate,
            title = { Text("Select Key to Upload", color = TextWhite, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Select a local keyring public key to upload to this server user account:", color = TextGray)
                    
                    clientKeys.forEach { key ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onRegisterKey(showRegisterKeyForUserId!!, key)
                                    showRegisterKeyForUserId = null
                                },
                            colors = CardDefaults.cardColors(containerColor = CardSlateLight)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(text = key.name, color = TextWhite, fontWeight = FontWeight.Bold)
                                Text(text = "FP: ${key.fingerprint}", color = TextGray, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showRegisterKeyForUserId = null }) {
                    Text("Cancel", color = TextGray)
                }
            }
        )
    }
}

@Composable
fun KeysManagementTab(
    publicKeys: List<PublicKeyEntity>,
    users: List<User>,
    onRevokeKey: (String) -> Unit,
    onDeleteKey: (String) -> Unit
) {
    if (publicKeys.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "No public keys registered on server records", color = TextMuted)
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(publicKeys, key = { it.id }) { key ->
                val user = users.find { it.id == key.userId }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSlate),
                    border = BorderStroke(1.dp, CardSlateLight)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = user?.name ?: "Unknown User", color = TextWhite, fontWeight = FontWeight.Bold)
                                Text(text = "Fingerprint: ${key.fingerprint}", color = TextGray, style = MaterialTheme.typography.labelSmall)
                            }
                            
                            // Key status badge
                            Box(
                                modifier = Modifier
                                    .background(
                                        when (key.status) {
                                            KeyStatus.ACTIVE -> StatusGreenBg
                                            KeyStatus.REVOKED -> StatusRedBg
                                            KeyStatus.ROTATED -> StatusOrangeBg
                                        },
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = key.status.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = when (key.status) {
                                        KeyStatus.ACTIVE -> StatusGreen
                                        KeyStatus.REVOKED -> StatusRed
                                        KeyStatus.ROTATED -> StatusOrange
                                    },
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (key.status == KeyStatus.ACTIVE) {
                                TextButton(
                                    onClick = { onRevokeKey(key.id) },
                                    colors = ButtonDefaults.textButtonColors(contentColor = StatusOrange)
                                ) {
                                    Icon(imageVector = Icons.Default.Block, contentDescription = "Revoke", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Revoke Key", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            TextButton(
                                onClick = { onDeleteKey(key.id) },
                                colors = ButtonDefaults.textButtonColors(contentColor = StatusRed)
                            ) {
                                Icon(imageVector = Icons.Default.RemoveCircleOutline, contentDescription = "Delete", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Delete", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SessionsManagementTab(
    sessions: List<Session>,
    users: List<User>,
    onRevokeSession: (String) -> Unit
) {
    if (sessions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "No active authenticated sessions", color = TextMuted)
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(sessions, key = { it.id }) { session ->
                val user = users.find { it.id == session.userId }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSlate),
                    border = BorderStroke(1.dp, CardSlateLight)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = user?.name ?: "Unknown User", color = TextWhite, fontWeight = FontWeight.Bold)
                                Text(text = user?.email ?: "", color = TextGray, style = MaterialTheme.typography.bodySmall)
                            }
                            
                            Button(
                                onClick = { onRevokeSession(session.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = StatusRedBg),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("Revoke", color = StatusRed, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        
                        Divider(color = CardSlateLight, modifier = Modifier.padding(vertical = 8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "DEVICE", style = MaterialTheme.typography.labelSmall, color = AardvarkCyan)
                                Text(text = session.deviceModel, color = TextWhite, style = MaterialTheme.typography.bodySmall)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "IP ADDRESS", style = MaterialTheme.typography.labelSmall, color = AardvarkCyan)
                                Text(text = session.ipAddress, color = TextWhite, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AuditLogTab(auditLogs: List<AuditLog>) {
    val dateFormat = remember { SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Immutable Security Trail",
                style = MaterialTheme.typography.titleMedium,
                color = TextWhite,
                fontWeight = FontWeight.Bold
            )
            Icon(
                imageVector = Icons.Default.Terminal,
                contentDescription = "Logs Terminal",
                tint = AardvarkCyan
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(CardSlate, RoundedCornerShape(24.dp))
                .border(1.dp, CardSlateLight, RoundedCornerShape(24.dp))
                .padding(12.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(auditLogs, key = { it.id }) { log ->
                    val timestampStr = dateFormat.format(Date(log.timestamp))
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "[$timestampStr]",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            modifier = Modifier.width(90.dp)
                        )
                        
                        // Action color indicator
                        Text(
                            text = log.action.name.padEnd(16).take(16),
                            style = MaterialTheme.typography.labelSmall,
                            color = when (log.action) {
                                AuditAction.REGISTRATION -> StatusBlue
                                AuditAction.AUTH_CHALLENGE_GEN -> TextGray
                                AuditAction.AUTH_SUCCESS -> StatusGreen
                                AuditAction.AUTH_FAILED -> StatusRed
                                AuditAction.KEY_UPLOAD -> AardvarkCyan
                                AuditAction.KEY_ROTATION -> StatusOrange
                                AuditAction.KEY_REVOCATION -> StatusRed
                                AuditAction.SESSION_REVOKED -> StatusOrange
                                AuditAction.ACCOUNT_DISABLED -> StatusRed
                                AuditAction.ACCOUNT_ENABLED -> StatusGreen
                                AuditAction.ROLE_CHANGE -> AardvarkCyan
                                AuditAction.ADMIN_ACTION -> StatusBlue
                            },
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(130.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        Text(
                            text = log.details,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextWhite,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}
