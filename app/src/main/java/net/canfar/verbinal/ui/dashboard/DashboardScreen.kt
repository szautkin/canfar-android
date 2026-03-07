package net.canfar.verbinal.ui.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import net.canfar.verbinal.R
import net.canfar.verbinal.data.repository.RecentLaunchRepository
import net.canfar.verbinal.ui.dashboard.launch.LaunchFormSection
import net.canfar.verbinal.ui.dashboard.launch.SessionLaunchViewModel
import net.canfar.verbinal.ui.dashboard.platform.PlatformLoadSection
import net.canfar.verbinal.ui.dashboard.platform.PlatformLoadViewModel
import net.canfar.verbinal.ui.dashboard.recent.RecentLaunchesSection
import net.canfar.verbinal.ui.dashboard.sessions.SessionListSection
import net.canfar.verbinal.ui.dashboard.sessions.SessionListViewModel
import net.canfar.verbinal.ui.dashboard.storage.StorageSection
import net.canfar.verbinal.ui.dashboard.storage.StorageViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    username: String,
    onLogout: () -> Unit,
    sessionListVm: SessionListViewModel = hiltViewModel(),
    launchVm: SessionLaunchViewModel = hiltViewModel(),
    platformVm: PlatformLoadViewModel = hiltViewModel(),
    storageVm: StorageViewModel = hiltViewModel(),
    recentLaunchRepo: RecentLaunchRepository = hiltViewModel<DashboardRecentHelper>().repo,
) {
    val sessionState by sessionListVm.uiState.collectAsState()
    val launchState by launchVm.uiState.collectAsState()
    val platformState by platformVm.uiState.collectAsState()
    val storageState by storageVm.uiState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var showAbout by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<String?>(null) }
    var showEventsDialog by remember { mutableStateOf<String?>(null) }
    var recentLaunches by remember { mutableStateOf(recentLaunchRepo.load()) }

    // Wire up counters
    LaunchedEffect(Unit) {
        launchVm.setSessionCounter { type -> sessionState.sessions.count { it.sessionType == type } }
        launchVm.setTotalSessionCounter { sessionState.sessions.size }
        sessionListVm.onSessionsRefreshed = {
            launchVm.updateSessionLimit()
            recentLaunches = recentLaunchRepo.load()
        }
    }

    // Load data on first composition
    LaunchedEffect(Unit) {
        sessionListVm.loadSessions()
        launchVm.loadImagesAndContext()
        platformVm.loadStats()
        if (username.isNotEmpty()) storageVm.loadQuota(username)
    }

    // React to launch counters update when sessions change
    LaunchedEffect(sessionState.sessions.size) {
        launchVm.setSessionCounter { type -> sessionState.sessions.count { it.sessionType == type } }
        launchVm.setTotalSessionCounter { sessionState.sessions.size }
        launchVm.updateSessionLimit()
    }

    // Show snackbar on launch success/fail
    LaunchedEffect(launchState.launchSuccess) {
        if (launchState.launchSuccess) {
            scope.launch {
                snackbarHostState.showSnackbar("Session launched successfully!", duration = SnackbarDuration.Short)
            }
            recentLaunches = recentLaunchRepo.load()
            kotlinx.coroutines.delay(2000)
            sessionListVm.loadSessions()
            launchVm.clearLaunchResult()
        }
    }

    // Show snackbar on renew result
    LaunchedEffect(sessionState.renewMessage) {
        sessionState.renewMessage?.let { msg ->
            scope.launch {
                snackbarHostState.showSnackbar(msg, duration = SnackbarDuration.Short)
            }
            sessionListVm.clearRenewMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(R.drawable.verbinal_icon),
                            contentDescription = "Verbinal",
                            modifier = Modifier.size(32.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text("Verbinal", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "A CANFAR Science Companion",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                            )
                        }
                    }
                },
                actions = {
                    Text(
                        username,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                    )
                    IconButton(onClick = { showAbout = true }) {
                        Icon(Icons.Default.Info, "About")
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, "Logout")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SessionListSection(
                sessions = sessionState.sessions,
                isLoading = sessionState.isLoading,
                isPolling = sessionState.isPolling,
                pollCountdown = sessionState.pollCountdown,
                hasError = sessionState.hasError,
                errorMessage = sessionState.errorMessage,
                onRefresh = { sessionListVm.loadSessions() },
                onOpen = { id ->
                    sessionState.sessions.find { it.id == id }?.let {
                        sessionListVm.openSessionInBrowser(it, context)
                    }
                },
                onRenew = { sessionListVm.renewSession(it) },
                onEvents = { showEventsDialog = it },
                onDelete = { showDeleteDialog = it },
            )

            StorageSection(state = storageState)

            PlatformLoadSection(
                state = platformState,
                onRefresh = { platformVm.loadStats() },
            )

            RecentLaunchesSection(
                launches = recentLaunches,
                isAtSessionLimit = launchState.isAtSessionLimit,
                onRelaunch = { launch ->
                    launchVm.relaunch(launch)
                },
                onRemove = { launch ->
                    recentLaunchRepo.remove(launch)
                    recentLaunches = recentLaunchRepo.load()
                },
                onClear = {
                    recentLaunchRepo.clear()
                    recentLaunches = recentLaunchRepo.load()
                },
            )

            LaunchFormSection(
                state = launchState,
                onTypeChanged = launchVm::onTypeChanged,
                onProjectChanged = launchVm::onProjectChanged,
                onImageChanged = launchVm::onImageChanged,
                onSessionNameChanged = launchVm::onSessionNameChanged,
                onResourceTypeChanged = launchVm::onResourceTypeChanged,
                onCoresChanged = launchVm::onCoresChanged,
                onRamChanged = launchVm::onRamChanged,
                onGpusChanged = launchVm::onGpusChanged,
                onCustomImageUrlChanged = launchVm::onCustomImageUrlChanged,
                onRepositoryHostChanged = launchVm::onRepositoryHostChanged,
                onRepositoryUsernameChanged = launchVm::onRepositoryUsernameChanged,
                onRepositorySecretChanged = launchVm::onRepositorySecretChanged,
                onGenerateName = launchVm::generateSessionName,
                onLaunch = {
                    launchVm.onUseCustomImageChanged(false)
                    launchVm.launch()
                },
                onAdvancedLaunch = {
                    launchVm.onUseCustomImageChanged(true)
                    launchVm.launch()
                },
            )

            Spacer(Modifier.height(32.dp))
        }
    }

    // Delete confirmation dialog
    showDeleteDialog?.let { sessionId ->
        val sessionName = sessionState.sessions.find { it.id == sessionId }?.sessionName ?: sessionId
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Session") },
            text = {
                Column {
                    Text("Are you sure you want to delete session '$sessionName'?")
                    Spacer(Modifier.height(8.dp))
                    Text("This action cannot be undone.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    sessionListVm.deleteSession(sessionId)
                    showDeleteDialog = null
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) { Text("Cancel") }
            },
        )
    }

    // Session events & logs dialog
    showEventsDialog?.let { sessionId ->
        var selectedTab by remember { mutableStateOf(0) }
        var eventsText by remember { mutableStateOf<String?>(null) }
        var logsText by remember { mutableStateOf<String?>(null) }
        var eventsLoading by remember { mutableStateOf(true) }
        var logsLoading by remember { mutableStateOf(true) }
        LaunchedEffect(sessionId) {
            eventsText = sessionListVm.getSessionEvents(sessionId)
            eventsLoading = false
        }
        LaunchedEffect(sessionId) {
            logsText = sessionListVm.getSessionLogs(sessionId)
            logsLoading = false
        }
        AlertDialog(
            onDismissRequest = { showEventsDialog = null },
            title = { Text("Session Details") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 350.dp),
                ) {
                    TabRow(selectedTabIndex = selectedTab) {
                        Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Events") })
                        Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Logs") })
                    }
                    Spacer(Modifier.height(8.dp))
                    val isLoading = if (selectedTab == 0) eventsLoading else logsLoading
                    val content = if (selectedTab == 0) eventsText else logsText
                    val emptyMsg = if (selectedTab == 0) "No events available." else "No logs available."
                    if (isLoading) {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else {
                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                            Text(
                                text = content ?: emptyMsg,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showEventsDialog = null }) { Text("Close") }
            },
        )
    }

    // About dialog
    if (showAbout) {
        val uriHandler = LocalUriHandler.current
        AlertDialog(
            onDismissRequest = { showAbout = false },
            title = { Text("About Verbinal") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Image(
                        painter = painterResource(R.drawable.verbinal_icon),
                        contentDescription = "Verbinal",
                        modifier = Modifier.size(64.dp),
                    )
                    Spacer(Modifier.height(12.dp))
                    Text("Verbinal", style = MaterialTheme.typography.titleLarge)
                    Text("A CANFAR Science Portal Companion", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    Text("Version 1.0.0", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Verbinal is a companion for the CANFAR (Canadian Advanced Network for Astronomical Research) web portal at canfar.net.\n\n" +
                            "Launch, monitor, and manage your interactive computing sessions (Notebook, Desktop, CARTA, Firefly) directly from your device without needing a browser.\n\n" +
                            "CANFAR is operated by the Canadian Astronomy Data Centre (CADC) and the Digital Research Alliance of Canada.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Licensed under the GNU Affero General Public License v3.0 (AGPL-3.0). This is free software: you can redistribute it and/or modify it under the terms of the AGPL.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(8.dp))
                    Text("Framework: Jetpack Compose / Android", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                    Text("\u00A9 2025 Serhii Zautkin", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = { uriHandler.openUri("https://www.canfar.net") }) {
                        Text("Visit canfar.net")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAbout = false }) { Text("Close") }
            },
        )
    }
}
