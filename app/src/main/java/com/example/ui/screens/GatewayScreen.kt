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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ClientKey
import com.example.data.PublicKeyEntity
import com.example.data.Session
import com.example.data.User
import com.example.ui.MainViewModel
import com.example.ui.theme.*

data class EnterpriseSystem(
    val id: String,
    val name: String,
    val description: String,
    val category: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GatewayScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
    users: List<User>,
    clientKeys: List<ClientKey>,
    publicKeys: List<PublicKeyEntity>
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val systems = remember {
        listOf(
            EnterpriseSystem("dev_portal", "Aardvark DevPortal", "Cloud console & Identity and Access Management for developer resources.", "IAM Platform"),
            EnterpriseSystem("git_vault", "GitVault Enterprise", "Highly secure Git hosting repository server with required signed commits.", "Git Hosting"),
            EnterpriseSystem("secure_wiki", "Enterprise Knowledge Base", "Private corporate knowledge base and research collaboration portal.", "Wikis & Docs")
        )
    }

    var selectedSystem by remember { mutableStateOf<EnterpriseSystem?>(null) }
    var selectedUserForLogin by remember { mutableStateOf<User?>(null) }
    var showLoginFlowDialog by remember { mutableStateOf(false) }
    var selectedSigningKey by remember { mutableStateOf<ClientKey?>(null) }

    Box(modifier = modifier.fillMaxSize().padding(16.dp)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Screen Header
            item {
                Column {
                    Text(
                        text = "Enterprise Gateway",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextWhite,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Simulate PGP passwordless sign-in for enterprise business applications.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextGray
                    )
                }
            }

            // Results from last authentication
            viewModel.authenticationSuccessSession?.let { session ->
                item {
                    AuthSuccessCard(
                        session = session,
                        users = users,
                        onDismiss = { viewModel.clearChallengeState() },
                        onCopyToken = {
                            clipboardManager.setText(AnnotatedString(session.token))
                            Toast.makeText(context, "JWT Token copied", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }

            viewModel.authenticationFailureReason?.let { reason ->
                item {
                    AuthFailureCard(
                        reason = reason,
                        onDismiss = { viewModel.clearChallengeState() }
                    )
                }
            }

            // Client applications list
            item {
                Text(
                    text = "Integrated Enterprise Clients",
                    style = MaterialTheme.typography.titleMedium,
                    color = AardvarkCyan,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(systems, key = { it.id }) { system ->
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("system_item_${system.id}"),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSlate),
                    border = BorderStroke(1.dp, CardSlateLight)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .background(CardSlateLight, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = system.category.uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AardvarkCyan,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = system.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextWhite,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.LockOpen,
                                contentDescription = "Secure login",
                                tint = TextMuted
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = system.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextGray
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                selectedSystem = system
                                showLoginFlowDialog = true
                                selectedUserForLogin = null
                                selectedSigningKey = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CardSlateLight),
                            border = BorderStroke(1.dp, AardvarkCyan),
                            modifier = Modifier.fillMaxWidth().testTag("sign_in_button_${system.id}")
                        ) {
                            Text(text = "PGP Passwordless Sign-In", color = AardvarkCyan, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Go", tint = AardvarkCyan)
                        }
                    }
                }
            }
        }

        // Authentication Dialog (Challenge-Response simulation flow)
        if (showLoginFlowDialog && selectedSystem != null) {
            AlertDialog(
                onDismissRequest = { showLoginFlowDialog = false },
                containerColor = CardSlate,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Security, contentDescription = "Secure SSO", tint = AardvarkCyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "PGP Login: ${selectedSystem?.name}",
                            color = TextWhite,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (selectedUserForLogin == null) {
                            // Step 1: Select Server User
                            Text(
                                text = "Select an enterprise user account to authenticate as:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextGray
                            )
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 240.dp)
                                    .border(1.dp, CardSlateLight, RoundedCornerShape(8.dp))
                                    .clip(RoundedCornerShape(8.dp))
                            ) {
                                LazyColumn {
                                    items(users) { user ->
                                        // Check if user has registered keys
                                        val hasKeys = publicKeys.any { it.userId == user.id && it.status == com.example.data.KeyStatus.ACTIVE }
                                        
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable(enabled = hasKeys) {
                                                    selectedUserForLogin = user
                                                    // Auto select first client key matching fingerprint if any
                                                    val userPubKey = publicKeys.find { it.userId == user.id && it.status == com.example.data.KeyStatus.ACTIVE }
                                                    if (userPubKey != null) {
                                                        selectedSigningKey = clientKeys.find { it.fingerprint == userPubKey.fingerprint }
                                                    }
                                                }
                                                .background(if (hasKeys) CardSlate else CardSlateLight.copy(alpha = 0.5f))
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text(
                                                    text = user.name,
                                                    color = if (hasKeys) TextWhite else TextMuted,
                                                    fontWeight = FontWeight.Bold,
                                                    style = MaterialTheme.typography.bodyLarge
                                                )
                                                Text(
                                                    text = "${user.email} (${user.role.name})",
                                                    color = TextGray,
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                            
                                            if (hasKeys) {
                                                Box(
                                                    modifier = Modifier
                                                        .background(StatusGreenBg, RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = "KEY REG",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = StatusGreen,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            } else {
                                                Box(
                                                    modifier = Modifier
                                                        .background(CardSlateLight, RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = "NO KEYS",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = TextMuted,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                        Divider(color = CardSlateLight, thickness = 1.dp)
                                    }
                                }
                            }
                            
                            Text(
                                text = "Note: Users must have registered PGP public keys in the Admin Console to use passwordless auth.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        } else {
                            // Step 2: Challenge-Response Signing Approval
                            val user = selectedUserForLogin!!
                            
                            // Load registered PGP Public Key for user
                            val userKey = publicKeys.find { it.userId == user.id && it.status == com.example.data.KeyStatus.ACTIVE }
                            
                            Text(
                                text = "Server generated a 256-bit challenge. Approve signing locally using your matching private key.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextGray
                            )
                            
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = CardSlateLight),
                                border = BorderStroke(1.dp, CardSlateLight)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "TARGET USER",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AardvarkCyan,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(text = user.name, color = TextWhite, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                    Text(text = user.email, color = TextGray, style = MaterialTheme.typography.bodySmall)
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Text(
                                        text = "REGISTERED FINGERPRINT",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AardvarkCyan,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(text = userKey?.fingerprint ?: "None", color = TextWhite, style = MaterialTheme.typography.labelSmall)
                                }
                            }

                            Text(
                                text = "Select Private Key from local Keychain:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextGray
                            )

                            // Select local signing key
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, CardSlateLight, RoundedCornerShape(8.dp))
                                    .clip(RoundedCornerShape(8.dp))
                            ) {
                                Column {
                                    clientKeys.forEach { key ->
                                        val isSelected = selectedSigningKey?.id == key.id
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(if (isSelected) CardSlateLight else CardSlate)
                                                .clickable { selectedSigningKey = key }
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text(text = key.name, color = TextWhite, fontWeight = FontWeight.Bold)
                                                Text(
                                                    text = "FP: ${key.fingerprint.take(19)}...",
                                                    color = if (key.fingerprint == userKey?.fingerprint) StatusGreen else TextGray,
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                            RadioButton(
                                                selected = isSelected,
                                                onClick = { selectedSigningKey = key },
                                                colors = RadioButtonDefaults.colors(selectedColor = AardvarkCyan)
                                            )
                                        }
                                        Divider(color = CardSlateLight, thickness = 1.dp)
                                    }
                                }
                            }

                            if (selectedSigningKey != null && userKey != null && selectedSigningKey?.fingerprint != userKey.fingerprint) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(imageVector = Icons.Default.Error, contentDescription = "Mismatch warning", tint = StatusOrange, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Selected private key does not match user's registered public key.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = StatusOrange
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    if (selectedUserForLogin == null) {
                        // User hasn't chosen target account yet
                        TextButton(onClick = { showLoginFlowDialog = false }) {
                            Text("Cancel", color = TextGray)
                        }
                    } else {
                        // In signing step
                        Button(
                            onClick = {
                                selectedSigningKey?.let { key ->
                                    viewModel.initiateAuthChallenge(selectedUserForLogin!!.id)
                                    viewModel.signAndVerifyChallenge(key)
                                    showLoginFlowDialog = false
                                }
                            },
                            enabled = selectedSigningKey != null,
                            colors = ButtonDefaults.buttonColors(containerColor = AardvarkCyan)
                        ) {
                            Text("Approve & Cryptographically Sign", color = CardSlate, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                dismissButton = {
                    if (selectedUserForLogin != null) {
                        TextButton(onClick = { selectedUserForLogin = null }) {
                            Text("Back", color = TextGray)
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun AuthSuccessCard(
    session: Session,
    users: List<User>,
    onDismiss: () -> Unit,
    onCopyToken: () -> Unit
) {
    val user = users.find { it.id == session.userId }
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().testTag("auth_success_card"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = StatusGreenBg.copy(alpha = 0.2f)),
        border = BorderStroke(1.dp, StatusGreen)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Granted", tint = StatusGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "ACCESS GRANTED", color = StatusGreen, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Dismiss", tint = TextWhite)
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Authenticated successfully as ${user?.name ?: "Unknown User"} (${user?.email ?: ""}). Secure JWT token issued.", color = TextWhite)
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(text = "SESSION METADATA", style = MaterialTheme.typography.labelSmall, color = AardvarkCyan, fontWeight = FontWeight.Bold)
            Text(text = "Session UUID: ${session.id}", color = TextGray, style = MaterialTheme.typography.bodySmall)
            Text(text = "Endpoint IP: ${session.ipAddress} | Platform: ${session.deviceModel}", color = TextGray, style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth().clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "View JWT Access Token", color = AardvarkCyan, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                IconButton(onClick = onCopyToken) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy JWT", tint = AardvarkCyan, modifier = Modifier.size(16.dp))
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CardSlate, RoundedCornerShape(16.dp))
                        .border(1.dp, CardSlateLight, RoundedCornerShape(16.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = session.token,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextGray,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun AuthFailureCard(
    reason: String,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("auth_failure_card"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = StatusRedBg.copy(alpha = 0.2f)),
        border = BorderStroke(1.dp, StatusRed)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Error, contentDescription = "Denied", tint = StatusRed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "AUTHENTICATION DENIED", color = StatusRed, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Dismiss", tint = TextWhite)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = reason, color = TextWhite)
        }
    }
}
