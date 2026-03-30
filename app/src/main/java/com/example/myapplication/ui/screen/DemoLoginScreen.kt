package com.example.myapplication.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.model.DemoUiState

@Composable
fun DemoLoginScreen(
    uiState: DemoUiState,
    onRequestCode: (String) -> Unit,
    onLogin: (String, String) -> Unit,
    onQuickDemoLogin: () -> Unit
) {
    var phone by rememberSaveable { mutableStateOf("90 123 45 67") }
    var code by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(uiState.otpRequested) {
        if (uiState.otpRequested && code.isBlank()) {
            code = uiState.otpHint
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF7F3E8),
                        Color(0xFFE3F0EA),
                        Color(0xFFD6E6DF)
                    )
                )
            )
            .padding(20.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Text(
                    text = "G'amxo'r oila demo",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Telefon orqali kirib, oilangiz holatini jonli dashboard ko'rinishida kuzatib ko'ring.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Telefon raqam") },
                    supportingText = { Text("Demo uchun format erkin: 90 123 45 67") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true
                )

                if (uiState.otpRequested) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("SMS kod") },
                        supportingText = { Text("Demo kod: ${uiState.otpHint}") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }

                uiState.loginError?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                if (uiState.isSendingCode || uiState.isVerifying) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

                Button(
                    onClick = {
                        if (uiState.otpRequested) {
                            onLogin(phone, code)
                        } else {
                            onRequestCode(phone)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isSendingCode && !uiState.isVerifying
                ) {
                    Text(if (uiState.otpRequested) "Kirish" else "Kod yuborish")
                }

                OutlinedButton(
                    onClick = onQuickDemoLogin,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Tez demo kirish")
                }

                Text(
                    text = "Bu demo rejim. Kod yuborish va verifikatsiya ilova ichida simulyatsiya qilinadi.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
