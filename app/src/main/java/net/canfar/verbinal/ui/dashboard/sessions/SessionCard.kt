package net.canfar.verbinal.ui.dashboard.sessions

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.OpenInBrowser
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.canfar.verbinal.data.model.Session
import net.canfar.verbinal.ui.components.sessionTypeImageRes
import net.canfar.verbinal.ui.theme.statusColor
import net.canfar.verbinal.ui.theme.typeColor
import net.canfar.verbinal.ui.theme.typeLabel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@Composable
fun SessionCard(
    session: Session,
    onOpen: () -> Unit,
    onRenew: () -> Unit,
    onEvents: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.width(340.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBF8)),
    ) {
        Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(sessionTypeImageRes(session.sessionType)),
                    contentDescription = session.sessionType,
                    modifier = Modifier.size(36.dp),
                    contentScale = ContentScale.Fit,
                )
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = session.sessionName,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(statusColor(session.status))
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                    ) {
                        Text(session.status, color = Color.White, fontSize = 11.sp)
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(typeColor(session.sessionType))
                        .padding(horizontal = 6.dp, vertical = 1.dp),
                ) {
                    Text(typeLabel(session.sessionType), color = Color.White, fontSize = 11.sp)
                }

                Text(
                    text = session.containerImage.substringAfterLast('/'),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Started: ${formatTime(session.startedTime)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        Text("Expires: ${formatTime(session.expiresTime)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }
                    Column {
                        Text("CPU: ${session.cpuAllocated.ifBlank { "n/a" }}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        Text("RAM: ${session.memoryAllocated.ifBlank { "n/a" }}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }
                }

                Spacer(Modifier.height(2.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.End)) {
                    FilledTonalIconButton(onClick = onOpen, enabled = session.status == "Running", modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Outlined.OpenInBrowser, "Open", modifier = Modifier.size(18.dp))
                    }
                    FilledTonalIconButton(onClick = onRenew, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Outlined.Refresh, "Extend", modifier = Modifier.size(18.dp))
                    }
                    FilledTonalIconButton(onClick = onEvents, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Outlined.Info, "Events", modifier = Modifier.size(18.dp))
                    }
                    FilledTonalIconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.error,
                        ),
                    ) {
                        Icon(Icons.Outlined.Delete, "Delete", modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

private fun formatTime(time: String): String {
    return try {
        val formats = listOf("yyyy-MM-dd'T'HH:mm:ss'Z'", "yyyy-MM-dd'T'HH:mm:ssXXX", "yyyy-MM-dd'T'HH:mm:ss")
        for (pattern in formats) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.getDefault())
                sdf.timeZone = TimeZone.getTimeZone("UTC")
                val date = sdf.parse(time) ?: continue
                return SimpleDateFormat("MMM dd HH:mm", Locale.getDefault()).format(date)
            } catch (_: Exception) {}
        }
        time
    } catch (_: Exception) {
        time
    }
}
