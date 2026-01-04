package lt.arnastamasiunas.coachbooking.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun RegisterScreen(
    onRegister: suspend (
        email: String,
        password: String,
        role: String,
        firstName: String,
        lastName: String
    ) -> Unit,
    onGoLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("client") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Registracija", style = MaterialTheme.typography.headlineSmall)

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
            label = { Text("Slaptažodis (min 6)") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = firstName,
            onValueChange = { firstName = it },
            label = { Text("Vardas") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = lastName,
            onValueChange = { lastName = it },
            label = { Text("Pavardė") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(12.dp))
        Text("Rolė")

        Row {
            FilterChip(
                selected = role == "client",
                onClick = { role = "client" },
                label = { Text("Klientas") }
            )
            Spacer(Modifier.width(8.dp))
            FilterChip(
                selected = role == "trainer",
                onClick = { role = "trainer" },
                label = { Text("Treneris") }
            )
        }

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
            enabled = !loading && email.isNotBlank() && password.length >= 6,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (loading) "Kuriama..." else "Sukurti paskyrą")
        }

        LaunchedEffect(loading) {
            if (!loading) return@LaunchedEffect
            try {
                onRegister(email.trim(), password, role, firstName, lastName)
            } catch (e: Exception) {
                error = e.message ?: "Registracija nepavyko"
            } finally {
                loading = false
            }
        }

        Spacer(Modifier.height(8.dp))

        TextButton(
            onClick = onGoLogin,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Jau turi paskyrą? Prisijungti")
        }
    }
}