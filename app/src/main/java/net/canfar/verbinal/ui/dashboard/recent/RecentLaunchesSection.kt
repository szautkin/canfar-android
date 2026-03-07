package net.canfar.verbinal.ui.dashboard.recent

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.canfar.verbinal.data.model.RecentLaunch
import net.canfar.verbinal.ui.components.sessionTypeImageRes
import net.canfar.verbinal.ui.theme.typeColor
import net.canfar.verbinal.ui.theme.typeLabel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RecentLaunchesSection(
    launches: List<RecentLaunch>,
    isAtSessionLimit: Boolean,
    onRelaunch: (RecentLaunch) -> Unit,
    onRemove: (RecentLaunch) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var filter by remember { mutableStateOf("") }

    val filtered = if (filter.isBlank()) {
        launches
    } else {
        launches.filter {
            it.name.contains(filter, ignoreCase = true) ||
                it.type.contains(filter, ignoreCase = true) ||
                it.imageLabel.contains(filter, ignoreCase = true)
        }
    }

    Card(modifier = modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Recent Launches", style = MaterialTheme.typography.titleMedium)
                if (launches.isNotEmpty()) {
                    IconButton(onClick = onClear) {
                        Icon(Icons.Outlined.DeleteSweep, "Clear history")
                    }
                }
            }

            if (launches.isNotEmpty()) {
                OutlinedTextField(
                    value = filter,
                    onValueChange = { filter = it },
                    placeholder = { Text("Filter by name or type...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            if (filtered.isEmpty()) {
                Text(
                    if (launches.isEmpty()) "No recent launches" else "No matches",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                )
            }

            filtered.forEach { launch ->
                RecentLaunchCard(
                    launch = launch,
                    isAtSessionLimit = isAtSessionLimit,
                    onRelaunch = { onRelaunch(launch) },
                    onRemove = { onRemove(launch) },
                )
            }
        }
    }
}

@Composable
private fun RecentLaunchCard(
    launch: RecentLaunch,
    isAtSessionLimit: Boolean,
    onRelaunch: () -> Unit,
    onRemove: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(8.dp),
    ) {
        Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(sessionTypeImageRes(launch.type)),
                    contentDescription = launch.type,
                    modifier = Modifier.size(36.dp),
                    contentScale = ContentScale.Fit,
                )
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(launch.name, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(typeColor(launch.type))
                            .padding(horizontal = 6.dp, vertical = 1.dp),
                    ) {
                        Text(typeLabel(launch.type), color = Color.White, fontSize = 11.sp)
                    }
                }

                val imageInfo = if (launch.project.isNotEmpty()) "${launch.project} / ${launch.imageLabel}" else launch.imageLabel
                Text(imageInfo, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), maxLines = 1, overflow = TextOverflow.Ellipsis)

                val resourceText = if (launch.resourceType != "fixed") {
                    "Flexible resources"
                } else {
                    buildString {
                        append("CPU: ${launch.cores}  \u00B7  RAM: ${launch.ram}GB")
                        if (launch.gpus > 0) append("  \u00B7  GPU: ${launch.gpus}")
                    }
                }
                Text(resourceText, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(launch.launchedAt)),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    )
                    Row {
                        TextButton(onClick = onRelaunch, enabled = !isAtSessionLimit) {
                            Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Relaunch", fontSize = 12.sp)
                        }
                        IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Outlined.Delete, "Remove", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }
    }
}
