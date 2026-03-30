package com.example.myapplication.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.model.DemoUiState
import com.example.myapplication.ui.component.InfoLine
import com.example.myapplication.ui.component.ProfileSwitchRow
import com.example.myapplication.ui.component.SectionTitle

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    uiState: DemoUiState,
    onRefresh: () -> Unit,
    onSignOut: () -> Unit,
    onAction: (String) -> Unit
) {
    var notificationsEnabled by rememberSaveable { mutableStateOf(true) }
    var autoCheckInEnabled by rememberSaveable { mutableStateOf(true) }
    var geofenceAlertsEnabled by rememberSaveable { mutableStateOf(true) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                androidx.compose.foundation.layout.Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = uiState.caregiverName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Family coordinator",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    InfoLine("Monitoring guruhi", uiState.familyLabel)
                    InfoLine("Ishonchli joylar", "${uiState.trustedPlacesCount} ta zona")
                    InfoLine("Keyingi check-in", uiState.nextCheckInLabel)
                }
            }
        }

        item {
            SectionTitle(
                title = "Demo sozlamalar",
                subtitle = "Qulay test uchun muhim boshqaruvlar shu yerga yig'ildi."
            )
        }

        item {
            Card(shape = MaterialTheme.shapes.large) {
                androidx.compose.foundation.layout.Column(modifier = Modifier.padding(20.dp)) {
                    ProfileSwitchRow(
                        title = "Push bildirishnomalar",
                        subtitle = "Ogohlantirish va check-in natijalari chiqadi.",
                        checked = notificationsEnabled,
                        onCheckedChange = {
                            notificationsEnabled = it
                            onAction("Bildirishnomalar ${if (it) "yoqildi" else "o'chirildi"}.")
                        }
                    )
                    ProfileSwitchRow(
                        title = "Auto check-in",
                        subtitle = "Belgilangan vaqtda oila a'zolaridan signal so'raladi.",
                        checked = autoCheckInEnabled,
                        onCheckedChange = { autoCheckInEnabled = it }
                    )
                    ProfileSwitchRow(
                        title = "Geozona alert",
                        subtitle = "Maktab yoki uy hududidan chiqishda ogohlantiradi.",
                        checked = geofenceAlertsEnabled,
                        onCheckedChange = { geofenceAlertsEnabled = it }
                    )
                }
            }
        }

        item {
            Card(shape = MaterialTheme.shapes.large) {
                androidx.compose.foundation.layout.Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Tez amallar",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Button(
                        onClick = onRefresh,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isRefreshing
                    ) {
                        Text(if (uiState.isRefreshing) "Demo yangilanmoqda..." else "Demo holatini yangilash")
                    }

                    OutlinedButton(
                        onClick = { onAction("Hisobot eksporti demo rejimida simulyatsiya qilindi.") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Kunlik hisobotni ko'rish")
                    }

                    TextButton(
                        onClick = onSignOut,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Demo hisobdan chiqish")
                    }
                }
            }
        }
    }
}
