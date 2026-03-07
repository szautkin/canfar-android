package net.canfar.verbinal.ui.dashboard.launch

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Casino
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import net.canfar.verbinal.data.model.ParsedImage
import net.canfar.verbinal.ui.components.ResourceSelector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaunchFormSection(
    state: SessionLaunchUiState,
    onTypeChanged: (String) -> Unit,
    onProjectChanged: (String) -> Unit,
    onImageChanged: (ParsedImage?) -> Unit,
    onSessionNameChanged: (String) -> Unit,
    onResourceTypeChanged: (String) -> Unit,
    onCoresChanged: (Int) -> Unit,
    onRamChanged: (Int) -> Unit,
    onGpusChanged: (Int) -> Unit,
    onCustomImageUrlChanged: (String) -> Unit,
    onRepositoryHostChanged: (String) -> Unit,
    onRepositoryUsernameChanged: (String) -> Unit,
    onRepositorySecretChanged: (String) -> Unit,
    onGenerateName: () -> Unit,
    onLaunch: () -> Unit,
    onAdvancedLaunch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Card(modifier = modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Launch Session", style = MaterialTheme.typography.titleMedium)

            if (state.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            if (state.isAtSessionLimit) {
                Text(state.sessionLimitMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Standard") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Advanced") })
            }

            when (selectedTab) {
                0 -> StandardTab(
                    state, onTypeChanged, onProjectChanged, onImageChanged, onSessionNameChanged,
                    onResourceTypeChanged, onCoresChanged, onRamChanged, onGpusChanged, onGenerateName, onLaunch,
                    onRepositoryHostChanged,
                )
                1 -> AdvancedTab(
                    state, onTypeChanged, onSessionNameChanged, onResourceTypeChanged, onCoresChanged,
                    onRamChanged, onGpusChanged, onCustomImageUrlChanged, onRepositoryHostChanged,
                    onRepositoryUsernameChanged, onRepositorySecretChanged, onGenerateName, onAdvancedLaunch,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StandardTab(
    state: SessionLaunchUiState,
    onTypeChanged: (String) -> Unit,
    onProjectChanged: (String) -> Unit,
    onImageChanged: (ParsedImage?) -> Unit,
    onSessionNameChanged: (String) -> Unit,
    onResourceTypeChanged: (String) -> Unit,
    onCoresChanged: (Int) -> Unit,
    onRamChanged: (Int) -> Unit,
    onGpusChanged: (Int) -> Unit,
    onGenerateName: () -> Unit,
    onLaunch: () -> Unit,
    onRepositoryHostChanged: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        DropdownField("Session Type", state.selectedType, state.sessionTypes, onTypeChanged)
        DropdownField("Image Registry", state.repositoryHost, state.repositories, onRepositoryHostChanged)
        DropdownField("Project", state.selectedProject, state.projects, onProjectChanged)

        // Container Image
        DropdownField(
            label = "Container Image",
            selected = state.selectedImage?.label ?: "",
            options = state.images.map { it.label },
            onSelected = { label -> onImageChanged(state.images.find { it.label == label }) },
        )

        // Session Name
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = state.sessionName,
                onValueChange = onSessionNameChanged,
                label = { Text("Session Name") },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onGenerateName) {
                Icon(Icons.Outlined.Casino, "Generate name")
            }
        }

        // Resource Type
        Text("Resource Type", style = MaterialTheme.typography.bodyMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = state.resourceType == "flexible", onClick = { onResourceTypeChanged("flexible") })
            Text("Flexible", modifier = Modifier.padding(end = 16.dp))
            RadioButton(selected = state.resourceType == "fixed", onClick = { onResourceTypeChanged("fixed") })
            Text("Fixed")
        }

        AnimatedVisibility(visible = state.resourceType == "fixed") {
            ResourceSelector(
                coreOptions = state.coreOptions,
                cores = state.cores,
                onCoresChange = onCoresChanged,
                ramOptions = state.ramOptions,
                ram = state.ram,
                onRamChange = onRamChanged,
                gpuOptions = state.gpuOptions,
                gpus = state.gpus,
                onGpusChange = onGpusChanged,
            )
        }

        if (state.hasError && !state.isLaunching) {
            Text(state.errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Button(
            onClick = onLaunch,
            enabled = !state.isLaunching && !state.isAtSessionLimit,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (state.isLaunching) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(8.dp))
                Text("Launch")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdvancedTab(
    state: SessionLaunchUiState,
    onTypeChanged: (String) -> Unit,
    onSessionNameChanged: (String) -> Unit,
    onResourceTypeChanged: (String) -> Unit,
    onCoresChanged: (Int) -> Unit,
    onRamChanged: (Int) -> Unit,
    onGpusChanged: (Int) -> Unit,
    onCustomImageUrlChanged: (String) -> Unit,
    onRepositoryHostChanged: (String) -> Unit,
    onRepositoryUsernameChanged: (String) -> Unit,
    onRepositorySecretChanged: (String) -> Unit,
    onGenerateName: () -> Unit,
    onAdvancedLaunch: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        DropdownField("Session Type", state.selectedType, state.sessionTypes, onTypeChanged)

        // Container Image: Registry + URL
        Text("Container Image", style = MaterialTheme.typography.bodyMedium)
        DropdownField("Registry", state.repositoryHost, state.repositories, onRepositoryHostChanged)
        OutlinedTextField(
            value = state.customImageUrl,
            onValueChange = onCustomImageUrlChanged,
            label = { Text("project/image:tag") },
            placeholder = { Text("project/example-image:1.0.0") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        // Registry Auth
        Text("Registry Auth", style = MaterialTheme.typography.bodyMedium)
        OutlinedTextField(
            value = state.repositoryUsername,
            onValueChange = onRepositoryUsernameChanged,
            label = { Text("Username") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = state.repositorySecret,
            onValueChange = onRepositorySecretChanged,
            label = { Text("Token or password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
        )

        // Session Name
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = state.sessionName,
                onValueChange = onSessionNameChanged,
                label = { Text("Session Name") },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onGenerateName) {
                Icon(Icons.Outlined.Casino, "Generate name")
            }
        }

        // Resource Type
        Text("Resource Type", style = MaterialTheme.typography.bodyMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = state.resourceType == "flexible", onClick = { onResourceTypeChanged("flexible") })
            Text("Flexible", modifier = Modifier.padding(end = 16.dp))
            RadioButton(selected = state.resourceType == "fixed", onClick = { onResourceTypeChanged("fixed") })
            Text("Fixed")
        }

        AnimatedVisibility(visible = state.resourceType == "fixed") {
            ResourceSelector(
                coreOptions = state.coreOptions, cores = state.cores, onCoresChange = onCoresChanged,
                ramOptions = state.ramOptions, ram = state.ram, onRamChange = onRamChanged,
                gpuOptions = state.gpuOptions, gpus = state.gpus, onGpusChange = onGpusChanged,
            )
        }

        if (state.hasError && !state.isLaunching) {
            Text(state.errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Button(
            onClick = onAdvancedLaunch,
            enabled = !state.isLaunching && !state.isAtSessionLimit,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (state.isLaunching) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(8.dp))
                Text("Launch (Custom Image)")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownField(
    label: String,
    selected: String,
    options: List<String>,
    onSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    },
                )
            }
        }
    }
}
