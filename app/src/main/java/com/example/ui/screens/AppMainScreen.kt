package com.example.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.theme.AardvarkCyan
import com.example.ui.theme.CardSlate
import com.example.ui.theme.CardSlateLight
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppMainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val users by viewModel.users.collectAsState()
    val clientKeys by viewModel.clientKeys.collectAsState()
    val publicKeys by viewModel.publicKeys.collectAsState()
    val sessions by viewModel.sessions.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "Aardvark Identity Logo",
                                tint = AardvarkCyan,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "AARDVARK IDENTITY",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextWhite,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                        }

                        // Status pill (ONLINE - SECURE)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(CardSlateLight)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(StatusGreen)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ONLINE - SECURE",
                                style = MaterialTheme.typography.labelSmall,
                                color = StatusGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CardSlate)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = CardSlate,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = viewModel.selectedScreen == AppScreen.VAULT,
                    onClick = { viewModel.selectedScreen = AppScreen.VAULT },
                    label = { Text("PGP Vault") },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.VpnKey,
                            contentDescription = "PGP Keys Vault"
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CardSlate,
                        selectedTextColor = AardvarkCyan,
                        indicatorColor = AardvarkCyan,
                        unselectedIconColor = TextGray,
                        unselectedTextColor = TextGray
                    ),
                    modifier = Modifier.testTag("nav_item_vault")
                )

                NavigationBarItem(
                    selected = viewModel.selectedScreen == AppScreen.GATEWAY,
                    onClick = { viewModel.selectedScreen = AppScreen.GATEWAY },
                    label = { Text("Auth Gateway") },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.LockOpen,
                            contentDescription = "SSO Authentication Gateway"
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CardSlate,
                        selectedTextColor = AardvarkCyan,
                        indicatorColor = AardvarkCyan,
                        unselectedIconColor = TextGray,
                        unselectedTextColor = TextGray
                    ),
                    modifier = Modifier.testTag("nav_item_gateway")
                )

                NavigationBarItem(
                    selected = viewModel.selectedScreen == AppScreen.ADMIN,
                    onClick = { viewModel.selectedScreen = AppScreen.ADMIN },
                    label = { Text("Admin Console") },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = "Server Admin Console"
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CardSlate,
                        selectedTextColor = AardvarkCyan,
                        indicatorColor = AardvarkCyan,
                        unselectedIconColor = TextGray,
                        unselectedTextColor = TextGray
                    ),
                    modifier = Modifier.testTag("nav_item_admin")
                )
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Crossfade(
            targetState = viewModel.selectedScreen,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) { screen ->
            when (screen) {
                AppScreen.VAULT -> VaultScreen(
                    viewModel = viewModel,
                    clientKeys = clientKeys
                )
                AppScreen.GATEWAY -> GatewayScreen(
                    viewModel = viewModel,
                    users = users,
                    clientKeys = clientKeys,
                    publicKeys = publicKeys
                )
                AppScreen.ADMIN -> AdminScreen(
                    viewModel = viewModel,
                    users = users,
                    publicKeys = publicKeys,
                    sessions = sessions,
                    auditLogs = auditLogs,
                    clientKeys = clientKeys
                )
            }
        }
    }
}
