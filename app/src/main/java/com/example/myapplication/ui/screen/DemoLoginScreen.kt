package com.example.myapplication.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Sms
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.example.myapplication.ui.theme.GlowSky
import com.example.myapplication.ui.theme.Linen
import com.example.myapplication.ui.theme.Mist

@Composable
fun DemoLoginScreen(
    uiState: DemoUiState,
    onRequestCode: (String) -> Unit,
    onLogin: (String, String) -> Unit,
    onRegister: (String, String) -> Unit
) {
    var phone by rememberSaveable { mutableStateOf("") }
    var code by rememberSaveable { mutableStateOf("") }
    val isBusy = uiState.isSendingCode || uiState.isVerifying

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.White,
                        Mist,
                        GlowSky.copy(alpha = 0.72f)
                    )
                )
            )
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Surface(
            modifier = Modifier
                .size(190.dp)
                .align(Alignment.TopEnd),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        ) {}

        Surface(
            modifier = Modifier
                .size(140.dp)
                .align(Alignment.BottomStart),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
        ) {}

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Xavfsiz kirish",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Telefon raqam va SMS kod bilan davom eting",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "API hali ulanmagan. Hozircha demo oqim ishlaydi va test kodi sifatida ${uiState.otpHint} dan foydalanish mumkin.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.96f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(56.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Hisobga kirish",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Telefon raqamingizni kiriting, so'ng SMS kod bilan tasdiqlang.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Telefon raqam") },
                        placeholder = { Text("+998 90 123 45 67") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        shape = RoundedCornerShape(20.dp)
                    )

                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("SMS kod") },
                        placeholder = { Text("2580") },
                        leadingIcon = { Icon(Icons.Default.Sms, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        shape = RoundedCornerShape(20.dp)
                    )

                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = Linen.copy(alpha = 0.65f),
                        tonalElevation = 0.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = if (uiState.otpRequested) {
                                        "Demo SMS kodi yuborildi"
                                    } else {
                                        "SMS kod olish"
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (uiState.otpRequested) {
                                        "Test kod: ${uiState.otpHint}"
                                    } else {
                                        "API tayyor bo'lguncha demo koddan foydalaniladi."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            TextButton(
                                onClick = { onRequestCode(phone) },
                                enabled = !isBusy
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sms,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (uiState.isSendingCode) "Yuborilmoqda" else "Kod olish",
                                    modifier = Modifier.padding(start = 6.dp)
                                )
                            }
                        }
                    }

                    uiState.loginError?.let { error ->
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.10f),
                            modifier = Modifier.border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.14f),
                                shape = RoundedCornerShape(18.dp)
                            )
                        ) {
                            Text(
                                text = error,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    if (isBusy) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = { onLogin(phone, code) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isBusy,
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Icon(Icons.Default.Key, contentDescription = null)
                        Text(
                            text = "Kirish",
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    OutlinedButton(
                        onClick = { onRegister(phone, code) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isBusy,
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null)
                        Text(
                            text = "Ro'yxatdan o'tish",
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }
    }
}
