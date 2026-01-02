package lt.arnastamasiunas.coachbooking.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(
    onLogin: suspend (email: String, password: String) -> Unit,
    onGoRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Prisijungimas", style = MaterialTheme.typography.headlineSmall)

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("El. paštas") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Slaptažodis") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        error?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = {
                error = null
                loading = true
            },
            enabled = !loading && email.isNotBlank() && password.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (loading) "Jungiamasi..." else "Prisijungti")
        }

        LaunchedEffect(loading) {
            if (!loading) return@LaunchedEffect
            try {
                onLogin(email.trim(), password)
            } catch (e: Exception) {
                error = e.message ?: "Prisijungimas nepavyko"
            } finally {
                loading = false
            }
        }

        Spacer(Modifier.height(8.dp))

        TextButton(
            onClick = onGoRegister,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Neturi paskyros? Registruotis")
        }
    }
}