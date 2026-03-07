package net.canfar.verbinal.ui.theme

import androidx.compose.ui.graphics.Color

// Status badge colors (matching WinUI StatusToColorConverter)
val StatusRunning = Color(0xFF4CAF50)
val StatusPending = Color(0xFFFF9800)
val StatusFailed = Color(0xFFF44336)
val StatusTerminating = Color(0xFF9E9E9E)

// Session type badge colors
val TypeNotebook = Color(0xFF2196F3)
val TypeDesktop = Color(0xFF673AB7)
val TypeCarta = Color(0xFF009688)
val TypeContributed = Color(0xFFFF5722)
val TypeFirefly = Color(0xFFFF9800)
val TypeHeadless = Color(0xFF607D8B)
val TypeDefault = Color(0xFF9E9E9E)

// App background (from Package.appxmanifest)
val DarkNavy = Color(0xFF0F172A)

fun statusColor(status: String): Color = when (status) {
    "Running" -> StatusRunning
    "Pending" -> StatusPending
    "Failed", "Error" -> StatusFailed
    "Terminating" -> StatusTerminating
    else -> StatusTerminating
}

fun typeColor(type: String): Color = when (type) {
    "notebook" -> TypeNotebook
    "desktop" -> TypeDesktop
    "carta" -> TypeCarta
    "contributed" -> TypeContributed
    "firefly" -> TypeFirefly
    "headless" -> TypeHeadless
    else -> TypeDefault
}

fun typeLabel(type: String): String = when (type) {
    "notebook" -> "Notebook"
    "desktop" -> "Desktop"
    "carta" -> "CARTA"
    "contributed" -> "Contrib"
    "firefly" -> "Firefly"
    "headless" -> "Headless"
    else -> type
}
