package net.canfar.verbinal.ui.login

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import net.canfar.verbinal.R

@Composable
fun BiometricUnlockScreen(
    onSuccess: () -> Unit,
    onUsePassword: () -> Unit,
) {
    val context = LocalContext.current
    val activity = context as FragmentActivity
    var promptTrigger by remember { mutableIntStateOf(0) }
    var dismissed by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(promptTrigger) {
        dismissed = false
        errorMessage = ""

        val executor = ContextCompat.getMainExecutor(context)
        val callback =
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onSuccess()
                }

                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence,
                ) {
                    dismissed = true
                    errorMessage =
                        when (errorCode) {
                            BiometricPrompt.ERROR_USER_CANCELED,
                            BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                            -> ""
                            BiometricPrompt.ERROR_LOCKOUT,
                            BiometricPrompt.ERROR_LOCKOUT_PERMANENT,
                            -> "Too many attempts. Use your password."
                            else -> errString.toString()
                        }
                }
            }

        val promptInfo =
            BiometricPrompt.PromptInfo
                .Builder()
                .setTitle("Unlock Verbinal")
                .setSubtitle("Verify your identity to continue")
                .setNegativeButtonText("Use password")
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK)
                .build()

        BiometricPrompt(activity, executor, callback).authenticate(promptInfo)
    }

    Scaffold { padding ->
        Column(
            modifier =
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(R.drawable.verbinal_icon),
                contentDescription = "Verbinal",
                modifier = Modifier.size(80.dp),
            )

            Spacer(Modifier.height(16.dp))

            Text("Verbinal", style = MaterialTheme.typography.headlineMedium)

            Spacer(Modifier.height(32.dp))

            Icon(
                Icons.Default.Fingerprint,
                contentDescription = "Biometric",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary,
            )

            Spacer(Modifier.height(16.dp))

            if (dismissed) {
                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Spacer(Modifier.height(16.dp))
                }

                Button(onClick = { promptTrigger++ }) {
                    Text("Try again")
                }
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = onUsePassword) {
                    Text("Use password")
                }
            } else {
                Text(
                    "Unlock to continue",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )
            }
        }
    }
}
