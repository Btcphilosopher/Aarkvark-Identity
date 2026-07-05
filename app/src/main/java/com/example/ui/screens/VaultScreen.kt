package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.ClientKey
import com.example.ui.MainViewModel
import com.example.ui.theme.CardSlate
import com.example.ui.theme.CardSlateLight
import com.example.ui.theme.AardvarkCyan
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
    clientKeys: List<ClientKey>
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var showCreateDialog by remember { mutableStateOf(false) }
    var newKeyName by remember { mutableStateOf("") }
    
    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Visual Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSlate)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Background generated image
                        Image(
                            painter = painterResource(id = R.drawable.img_aardvark_hero),
                            contentDescription = "Enterprise Vault",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        // Tech gradient tint overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    androidx.compose.ui.graphics.Brush.verticalGradient(
                                        colors = listOf(
                                            androidx.compose.ui.graphics.Color.Transparent,
                                            androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.85f)
                                        )
                                    )
                                )
                        )
                        
                        // Text contents
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "OPENPGP VAULT",
                                style = MaterialTheme.typography.titleMedium,
                                color = AardvarkCyan,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Local private key storage. Secure and isolated. At no point do your private keys leave this device.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextGray,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Key List Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "My PGP Keyrings",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${clientKeys.size} local keys active",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextGray
                        )
                    }
                    Button(
                        onClick = { showCreateDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = AardvarkCyan),
                        modifier = Modifier.testTag("generate_key_fab")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add key", tint = CardSlate)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Generate PGP Key", color = CardSlate, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Key List empty state
            if (clientKeys.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        colors = CardDefaults.cardColors(containerColor = CardSlate),
                        border = BorderStroke(1.dp, CardSlateLight)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.VpnKey,
                                contentDescription = "No Keys",
                                tint = TextMuted,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No Cryptographic Keys Found",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextWhite,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Generate your first secure OpenPGP-compatible identity keypair to begin passwordless authentication.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextGray,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(clientKeys, key = { it.id }) { clientKey ->
                    KeyItemCard(
                        clientKey = clientKey,
                        onDelete = { viewModel.deleteClientKey(clientKey.id) },
                        onCopy = {
                            clipboardManager.setText(AnnotatedString(clientKey.publicKeyArmor))
                            Toast.makeText(context, "Armored Public Key copied to clipboard", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }

        // Generate Key Dialog
        if (showCreateDialog) {
            AlertDialog(
                onDismissRequest = { showCreateDialog = false },
                containerColor = CardSlate,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Key, contentDescription = "Key logo", tint = AardvarkCyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Generate PGP Identity", color = TextWhite, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Enter a name or identifier for your cryptographic keypair. This identifies your keys locally (e.g. Work Macbook, Backup Tablet).",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextGray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = newKeyName,
                            onValueChange = { newKeyName = it },
                            label = { Text("Key Nickname") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AardvarkCyan,
                                unfocusedBorderColor = CardSlateLight,
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("key_name_input")
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newKeyName.isNotBlank()) {
                                viewModel.generateNewClientKey(newKeyName)
                                newKeyName = ""
                                showCreateDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AardvarkCyan)
                    ) {
                        Text("Create Key", color = CardSlate, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateDialog = false }) {
                        Text("Cancel", color = TextGray)
                    }
                }
            )
        }
    }
}

@Composable
fun KeyItemCard(
    clientKey: ClientKey,
    onDelete: () -> Unit,
    onCopy: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }
    val createdStr = dateFormat.format(Date(clientKey.createdAt))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("client_key_item_${clientKey.fingerprint.replace(" ", "_")}"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = CardSlate),
        border = BorderStroke(1.dp, CardSlateLight)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = "PGP Key",
                        tint = AardvarkCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = clientKey.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Generated $createdStr",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextGray
                        )
                    }
                }
                
                Row {
                    IconButton(onClick = onCopy) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy key", tint = TextGray)
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.testTag("delete_key_button")) {
                        Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Delete key", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Fingerprint Display
            Text(
                text = "FINGERPRINT",
                style = MaterialTheme.typography.labelSmall,
                color = AardvarkCyan,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardSlateLight, RoundedCornerShape(16.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = clientKey.fingerprint,
                    style = MaterialTheme.typography.labelMedium,
                    color = TextWhite,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Expandable armored public key view
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Armored Public Key Block",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextGray,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = "Toggle key expansion",
                    tint = TextGray
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandVertically(animationSpec = spring()),
                exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkVertically(animationSpec = spring())
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CardSlateLight, RoundedCornerShape(16.dp))
                            .border(1.dp, CardSlateLight, RoundedCornerShape(16.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = clientKey.publicKeyArmor,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextGray,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
