package com.example.myapplication.ui.screen

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.model.BackendStatus
import com.example.myapplication.data.model.DemoActionResult
import com.example.myapplication.data.model.DemoUiState
import com.example.myapplication.ui.theme.GlowMint
import com.example.myapplication.ui.theme.GlowRose
import com.example.myapplication.ui.theme.GlowSand
import com.example.myapplication.ui.theme.GlowSky
import kotlinx.coroutines.launch

private enum class RegistrationStep(
    val title: String,
    val subtitle: String
) {
    Phone(
        title = "Telefon raqamingizni kiriting",
        subtitle = "Avval raqamingizga tasdiqlash kodi yuboramiz."
    ),
    Code(
        title = "SMS kodni tasdiqlang",
        subtitle = "Raqamingizga kelgan kodni kiriting."
    ),
    Name(
        title = "Ismingizni kiriting",
        subtitle = "Ro'yxatdan o'tishni yakunlash uchun ismingiz kerak."
    )
}

@Composable
fun DemoLoginScreen(
    uiState: DemoUiState,
    onRequestCode: (String) -> Unit,
    onVerifyCode: (String, String) -> Unit,
    onCompleteRegistration: (String, String) -> Unit,
    onUpdateServerUrl: suspend (String) -> DemoActionResult,
    onCheckBackend: () -> Unit,
    onBack: () -> Unit
) {
    var phone by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue("+998 "))
    }
    var code by rememberSaveable { mutableStateOf("") }
    var fullName by rememberSaveable { mutableStateOf("") }
    var step by rememberSaveable { mutableStateOf(RegistrationStep.Phone) }
    var serverUrl by rememberSaveable(uiState.backendConnection.baseUrl) {
        mutableStateOf(uiState.backendConnection.baseUrl)
    }
    LaunchedEffect(uiState.otpRequested) {
        if (uiState.otpRequested) {
            step = RegistrationStep.Code
        }
    }

    LaunchedEffect(uiState.isCodeVerified) {
        if (uiState.isCodeVerified) {
            step = RegistrationStep.Name
        }
    }

    val isBusy = uiState.isSendingCode || uiState.isVerifying
    val phoneReady = isUzPhoneComplete(phone.text)
    val codeReady = code.length >= 4
    val nameReady = uiState.hasExistingAccount || fullName.trim().length >= 2

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.White,
                        GlowSand,
                        GlowSky
                    )
                )
            )
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Surface(
            modifier = Modifier
                .size(180.dp)
                .align(Alignment.TopEnd),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        ) {}

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                ) {
                    Text(
                        text = when (step) {
                            RegistrationStep.Phone -> "1 / 3"
                            RegistrationStep.Code -> "2 / 3"
                            RegistrationStep.Name -> "3 / 3"
                        },
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                TextButton(onClick = onBack) {
                    Text("Orqaga")
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = step.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = step.subtitle,
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
                                imageVector = when (step) {
                                    RegistrationStep.Phone -> Icons.Default.Phone
                                    RegistrationStep.Code -> Icons.Default.Sms
                                    RegistrationStep.Name -> Icons.Default.Badge
                                },
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    when (step) {
                        RegistrationStep.Phone -> {
                            OutlinedTextField(
                                value = phone,
                                onValueChange = { phone = normalizeUzPhone(it) },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Telefon raqami") },
                                placeholder = { Text("+998 90 123 45 67") },
                                supportingText = {
                                    Text(
                                        if (phoneReady) "Raqam tayyor." else "9 xonali telefon raqamini to'liq kiriting."
                                    )
                                },
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                shape = RoundedCornerShape(20.dp)
                            )

                            InfoNote(
                                title = "Tasdiqlash kodi",
                                description = "Demo rejimida SMS kodi sifatida ${uiState.otpHint} ishlatiladi."
                            )

                            Button(
                                onClick = { onRequestCode(phone.text) },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = phoneReady && !isBusy,
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Icon(Icons.Default.Sms, contentDescription = null)
                                Text(
                                    text = if (uiState.isSendingCode) "Yuborilmoqda..." else "SMS kod olish",
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }

                        RegistrationStep.Code -> {
                            InfoNote(
                                title = "Kiritilgan raqam",
                                description = phone.text
                            )

                            OutlinedTextField(
                                value = code,
                                onValueChange = { code = it.filter(Char::isDigit).take(6) },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("SMS kod") },
                                placeholder = { Text(uiState.otpHint) },
                                supportingText = {
                                    Text("Demo rejimida 4 yoki 6 xonali kod ishlaydi.")
                                },
                                leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(20.dp)
                            )

                            InfoNote(
                                title = "Demo kodi",
                                description = "Tasdiqlash uchun ${uiState.otpHint} kodidan foydalaning."
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                TextButton(
                                    onClick = { step = RegistrationStep.Phone },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Raqamni o'zgartirish")
                                }

                                Button(
                                    onClick = {
                                        onVerifyCode(phone.text, code)
                                    },
                                    modifier = Modifier.weight(1f),
                                    enabled = codeReady && !isBusy,
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Icon(Icons.Default.VerifiedUser, contentDescription = null)
                                    Text(
                                        text = "Tasdiqlash",
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }
                            }
                        }

                        RegistrationStep.Name -> {
                            InfoNote(
                                title = "Tasdiqlangan raqam",
                                description = phone.text
                            )

                            OutlinedTextField(
                                value = fullName,
                                onValueChange = { fullName = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text(if (uiState.hasExistingAccount) "Ism (ixtiyoriy)" else "Ism") },
                                placeholder = {
                                    Text(
                                        if (uiState.hasExistingAccount) {
                                            "Agar hisob mavjud bo'lsa bo'sh qoldirishingiz mumkin"
                                        } else {
                                            "Masalan, Nodir Yusupov"
                                        }
                                    )
                                },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                                singleLine = true,
                                shape = RoundedCornerShape(20.dp)
                            )

                            Button(
                                onClick = { onCompleteRegistration(phone.text, fullName) },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = nameReady && !isBusy,
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Icon(Icons.Default.VerifiedUser, contentDescription = null)
                                Text(
                                    text = if (uiState.hasExistingAccount) "Ilovaga kirish" else "Ro'yxatdan o'tish",
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }

                    ServerSettingsBlock(
                        currentStatus = uiState.backendConnection.label,
                        backendDetail = uiState.backendConnection.detail,
                        serverUrl = serverUrl,
                        onServerUrlChange = { serverUrl = it },
                        onSaveServerUrl = {
                            val result = onUpdateServerUrl(serverUrl)
                            serverUrl = uiState.backendConnection.baseUrl.ifBlank { serverUrl }
                            result
                        },
                        onCheckBackend = onCheckBackend
                    )

                    uiState.loginError?.let { error ->
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.10f)
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

                    Spacer(modifier = Modifier.height(2.dp))
                }
            }
        }
    }
}

@Composable
private fun ServerSettingsBlock(
    currentStatus: String,
    backendDetail: String,
    serverUrl: String,
    onServerUrlChange: (String) -> Unit,
    onSaveServerUrl: suspend () -> DemoActionResult,
    onCheckBackend: () -> Unit
) {
    var lastActionMessage by rememberSaveable { mutableStateOf("") }
    val isOnline = currentStatus == BackendStatus.ONLINE.name || currentStatus == "Online"
    val scope = rememberCoroutineScope()

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFFF8F6F1)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isOnline) Icons.Default.CloudDone else Icons.Default.CloudOff,
                        contentDescription = null,
                        tint = if (isOnline) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "Server: $currentStatus",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                TextButton(onClick = onCheckBackend) {
                    Text("Tekshirish")
                }
            }

            Text(
                text = backendDetail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = serverUrl,
                onValueChange = onServerUrlChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Vaqtinchalik server manzili") },
                placeholder = { Text("http://10.0.2.2:8000/api/v1/") },
                leadingIcon = { Icon(Icons.Default.Link, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                singleLine = true,
                shape = RoundedCornerShape(18.dp)
            )

            Button(
                onClick = {
                    scope.launch {
                        lastActionMessage = onSaveServerUrl().message
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text("Server manzilini saqlash")
            }

            if (lastActionMessage.isNotBlank()) {
                Text(
                    text = lastActionMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isOnline) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun InfoNote(
    title: String,
    description: String
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = GlowSand
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
