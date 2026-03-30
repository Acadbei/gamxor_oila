package com.example.myapplication.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.SendToMobile
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
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
import com.example.myapplication.ui.component.SymbolChip
import com.example.myapplication.ui.theme.GlowRose
import com.example.myapplication.ui.theme.GlowSand
import com.example.myapplication.ui.theme.GlowSky

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
                        Color(0xFFF8F4EA),
                        Color(0xFFE7F3ED),
                        Color(0xFFDCE9F6)
                    )
                )
            )
            .padding(20.dp)
    ) {
        Surface(
            modifier = Modifier
                .size(150.dp)
                .align(Alignment.TopEnd),
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.22f)
        ) {}

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(34.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(60.dp),
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.16f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                            imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Family Care",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Text(
                                text = "Live • SOS • Invite",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.82f)
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SymbolChip(
                            icon = Icons.Default.Security,
                            label = "OTP",
                            accent = GlowSand
                        )
                        SymbolChip(
                            icon = Icons.Default.Bolt,
                            label = "Quick",
                            accent = GlowRose
                        )
                        SymbolChip(
                            icon = Icons.AutoMirrored.Filled.SendToMobile,
                            label = "Demo",
                            accent = GlowSky
                        )
                    }
                }
            }

            Card(
                shape = RoundedCornerShape(34.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.94f))
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Kirish",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Telefon orqali kirib, jonli demo oqimini ko'ring.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Telefon") },
                        placeholder = { Text("90 123 45 67") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        shape = RoundedCornerShape(22.dp)
                    )

                    if (uiState.otpRequested) {
                        OutlinedTextField(
                            value = code,
                            onValueChange = { code = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Kod") },
                            placeholder = { Text(uiState.otpHint) },
                            leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(22.dp)
                        )
                    }

                    if (uiState.otpRequested) {
                        SymbolChip(
                            icon = Icons.Default.Security,
                            label = "Demo kod: ${uiState.otpHint}",
                            accent = MaterialTheme.colorScheme.primary
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
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
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
                        Icon(
                            imageVector = if (uiState.otpRequested) Icons.Default.Key else Icons.AutoMirrored.Filled.SendToMobile,
                            contentDescription = null
                        )
                        SpacerWidth()
                        Text(if (uiState.otpRequested) "Kirish" else "Kod olish")
                    }

                    OutlinedButton(
                        onClick = onQuickDemoLogin,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = null)
                        SpacerWidth()
                        Text("Tez demo")
                    }
                }
            }
        }
    }
}

@Composable
private fun SpacerWidth() {
    Box(modifier = Modifier.width(8.dp))
}
