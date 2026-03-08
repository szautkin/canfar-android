package net.canfar.verbinal

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.AndroidEntryPoint
import net.canfar.verbinal.ui.navigation.VerbinalNavHost
import net.canfar.verbinal.ui.theme.VerbinalTheme

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VerbinalTheme {
                VerbinalNavHost()
            }
        }
    }
}
