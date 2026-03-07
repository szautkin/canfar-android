package net.canfar.verbinal.ui.components

import net.canfar.verbinal.R

fun sessionTypeImageRes(type: String): Int = when (type) {
    "notebook" -> R.drawable.session_notebook
    "desktop" -> R.drawable.session_desktop
    "carta" -> R.drawable.session_carta
    "contributed" -> R.drawable.session_contributed
    "firefly" -> R.drawable.session_firefly
    "headless" -> R.drawable.session_desktop
    else -> R.drawable.session_desktop
}
